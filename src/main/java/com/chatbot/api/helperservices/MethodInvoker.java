package com.chatbot.api.helperservices;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.MethodResult;
import com.chatbot.api.utils.RuntimeTypeConverter;
import com.chatbot.api.utils.SpringBeanProvider;

@Service
public class MethodInvoker {
    
    // Instance caches - can be managed by Spring
    private final Map<String, List<MethodDetails>> methodCache = new ConcurrentHashMap<>();
    
    // Thread-local scanner to avoid creating new instances
    private final ThreadLocal<ClassPathScanningCandidateComponentProvider> scannerCache = 
        ThreadLocal.withInitial(() -> {
            ClassPathScanningCandidateComponentProvider scanner = 
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
            return scanner;
        }); 
    
    
    public MethodResult invoke(Map<String, Object> inputs, String function, String packageName) {
        try {
            // Get all methods with the same name first
            List<MethodDetails> methods = getMethodsFromCache(function, packageName);
            
            if (methods.isEmpty()) {
                return new MethodResult(null, "Method not found: " + function);
            }
            
            // Find the method with the most matching parameter names
            MethodDetails bestMatch = findBestMethodMatch(methods, inputs);
            if (bestMatch != null) {
                try {
                    MethodResult result = invokeMethod(bestMatch, inputs, function);
                    if ("success".equals(result.getResult())) {
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Error invoking best match method " + bestMatch.getMethodSignature() + ": " + e.getMessage());
                    // Continue to try all methods if best match fails
                }
            }
            
            // Fallback: Try each method with the same name, prioritizing by matching parameter names
            methods.sort((m1, m2) -> {
                int matches1 = countMatchingParameterNames(m1, inputs);
                int matches2 = countMatchingParameterNames(m2, inputs);
                
                // Primary sort: more parameter name matches first
                int result = Integer.compare(matches2, matches1);
                if (result != 0) return result;
                
                // Secondary sort: prefer methods with parameter count closer to input size
                int diff1 = Math.abs(m1.getParameters().length - inputs.size());
                int diff2 = Math.abs(m2.getParameters().length - inputs.size());
                return Integer.compare(diff1, diff2);
            });
            
            for (MethodDetails methodInfo : methods) {
                try {
                    MethodResult result = invokeMethod(methodInfo, inputs, function);
                    if ("success".equals(result.getResult())) {
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Error invoking method " + methodInfo.getMethodSignature() + ": " + e.getMessage());
                    // Continue to try next method
                }
            }
            
            // If we get here, all methods failed
            return new MethodResult(null, "failure");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return new MethodResult(null, "failure");
        }
    }
    
    private MethodDetails findBestMethodMatch(List<MethodDetails> methods, Map<String, Object> inputs) {
        if (methods.isEmpty()) {
            return null;
        }
        
        MethodDetails bestMatch = null;
        int maxMatches = -1;
        
        for (MethodDetails method : methods) {
            int matches = countMatchingParameterNames(method, inputs);
            
            // If this method has more matching parameter names, it's our new best match
            if (matches > maxMatches) {
                maxMatches = matches;
                bestMatch = method;
            } else if (matches == maxMatches && bestMatch != null) {
                // If tied on matching parameter names, prefer the one with parameter count closer to input size
                int currentDiff = Math.abs(method.getParameters().length - inputs.size());
                int bestDiff = Math.abs(bestMatch.getParameters().length - inputs.size());
                
                if (currentDiff < bestDiff) {
                    bestMatch = method;
                }
            }
        }
        
        return bestMatch;
    }
    
    private MethodResult invokeMethod(MethodDetails methodInfo, Map<String, Object> inputs, String function) throws Exception {
        Object instance = SpringBeanProvider.getBean(methodInfo.getDeclaringClass());
        Object[] args = prepareMethodArguments(methodInfo, inputs);
        Object output = methodInfo.getMethod().invoke(instance, args);
        
        System.out.println("Successfully invoked " + methodInfo.getMethodSignature());
        return new MethodResult(output, "success");
    }
    
    private List<MethodDetails> getMethodsFromCache(String function, String packageName) {
        String cacheKey = packageName + ":" + function;
        
        return methodCache.computeIfAbsent(cacheKey, key -> {
            try {
                return scanAndCacheMethods(function, packageName);
            } catch (Exception e) {
                System.err.println("Error scanning for methods: " + e.getMessage());
                return Collections.emptyList();
            }
        });
    }
    
    private List<MethodDetails> scanAndCacheMethods(String function, String packageName) throws Exception {
         ClassPathScanningCandidateComponentProvider scanner = scannerCache.get();
         Set<BeanDefinition> candidates = scanner.findCandidateComponents(packageName);
        
        List<MethodDetails> methods = new ArrayList<>();
        String cacheKey = packageName + ":" + function;
        
        for (BeanDefinition bean : candidates) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            
            // Find all methods with the target name
            Method[] classMethods = clazz.getDeclaredMethods();
            for (Method method : classMethods) {
                if (method.getName().equals(function)) {
                    MethodDetails methodInfo = new MethodDetails(method, clazz);
                    methods.add(methodInfo);
                }
            }
        }
        
        // Cache the methods list for this function and package
        methodCache.put(cacheKey, methods);
        
        return methods;
    }
    
    private int countMatchingParameterNames(MethodDetails methodInfo, Map<String, Object> inputs) {
        int matches = 0;
        Set<String> inputKeys = inputs.keySet();
        
        for (Parameter param : methodInfo.getParameters()) {
            String paramName = param.getName();
            if (inputKeys.contains(paramName)) {
                matches++;
            }
        }
        
        return matches;
    }
    
    private Object[] prepareMethodArguments(MethodDetails methodInfo, Map<String, Object> inputs) {
        Parameter[] parameters = methodInfo.getParameters();
        Object[] args = new Object[parameters.length];
        
        try {
            RuntimeTypeConverter converterUtils = SpringBeanProvider.getConverterUtils();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                String paramName = param.getName();
                Class<?> paramType = param.getType();
                
                // Try to get value from inputs map
                Object value = inputs.get(paramName);
                
                // If value is null and type is a complex object, try converting the whole map
                if (value == null && !isSimple(paramType)) {
                    value = converterUtils.castToRuntimeType(inputs, paramType);
                } else {
                    value = converterUtils.castToRuntimeType(value, paramType);
                }
                args[i] = value;
            }
        } catch (Exception ex) {
            System.err.println("Error preparing method arguments: " + ex.getMessage());
            throw new RuntimeException("Failed to prepare method arguments", ex);
        }
        
        return args;
    }
    
    private boolean isSimple(Class<?> type) {
        return type.isPrimitive() || 
               type.equals(String.class) || 
               Number.class.isAssignableFrom(type) ||
               type.equals(Boolean.class) ||
               type.equals(Character.class);
    }
    
    // Method to clear cache if needed (useful for testing or dynamic reloading)
    public void clearCache() {
        methodCache.clear();
    }
    
    // Method to get cache statistics (useful for monitoring)
    public Map<String, Integer> getCacheStats() {
        return Map.of(
            "methodCacheSize", methodCache.size()
        );
    }
    
    // Method to get detailed cache information for debugging
    public void printCacheInfo() {
        System.out.println("=== Method Cache Info ===");
        methodCache.forEach((key, methods) -> {
            System.out.println("Key: " + key);
            methods.forEach(method -> 
                System.out.println("  - " + method.getMethodSignature()));
        });
    }
}