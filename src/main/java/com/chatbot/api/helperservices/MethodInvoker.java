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
    private final Map<String, MethodDetails> methodSignatureCache = new ConcurrentHashMap<>();
    private final Map<String, Set<BeanDefinition>> packageCache = new ConcurrentHashMap<>();
    
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
            // First, try to find exact method match by signature
            MethodDetails exactMatch = findExactMethodMatch(inputs, function, packageName);
            if (exactMatch != null) {
                return invokeMethod(exactMatch, inputs, function);
            }
            
            // Fallback: Get cached methods or scan and cache
            List<MethodDetails> methods = getMethodsFromCache(function, packageName);
            
            if (methods.isEmpty()) {
                return new MethodResult(null, "Method not found: " + function);
            }
            
            // Try each method with the same name, prioritizing by matching parameter names
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
            
            Exception lastException = null;
            for (MethodDetails methodInfo : methods) {
                try {
                    MethodResult result = invokeMethod(methodInfo, inputs, function);
                    if ("success".equals(result.getResult())) {
                        // Cache successful method for future calls
                        cacheMethodByInputs(methodInfo, inputs, function, packageName);
                        return result;
                    }
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("Error invoking method " + methodInfo.getMethodSignature() + ": " + e.getMessage());
                    // Continue to try next method
                }
            }
            
            // If we get here, all methods failed
            String errorMsg = lastException != null ? lastException.getMessage() : "Unknown error";
            return new MethodResult(null, "All method invocations failed. Last error: " + errorMsg);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return new MethodResult(null, "failure: " + ex.getMessage());
        }
    }
    
    private MethodDetails findExactMethodMatch(Map<String, Object> inputs, String function, String packageName) {
        // Try exact input signature match first
        String inputSignatureKey = createSignatureKey(inputs, function, packageName);
        MethodDetails exactMatch = methodSignatureCache.get(inputSignatureKey);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // Try parameter count match as fallback
        String paramCountKey = packageName + ":" + function + ":paramCount:" + inputs.size();
        return methodSignatureCache.get(paramCountKey);
    }
    
    private String createSignatureKey(Map<String, Object> inputs, String function, String packageName) {
        // Try to match by parameter count and types if possible
        StringBuilder sb = new StringBuilder();
        sb.append(packageName).append(":").append(function).append("(");
        
        // Sort input keys for consistent signature generation
        List<String> sortedKeys = new ArrayList<>(inputs.keySet());
        sortedKeys.sort(String::compareTo);
        
        for (int i = 0; i < sortedKeys.size(); i++) {
            if (i > 0) sb.append(",");
            Object value = inputs.get(sortedKeys.get(i));
            if (value != null) {
                sb.append(value.getClass().getSimpleName());
            } else {
                sb.append("null");
            }
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    private void cacheMethodByInputs(MethodDetails methodInfo, Map<String, Object> inputs, String function, String packageName) {
        // Cache by input signature for faster future lookups with same input types
        String inputSignatureKey = createSignatureKey(inputs, function, packageName);
        methodSignatureCache.put(inputSignatureKey, methodInfo);
        
        // Also cache by parameter count for fallback matching
        String paramCountKey = packageName + ":" + function + ":paramCount:" + methodInfo.getParameters().length;
        methodSignatureCache.put(paramCountKey, methodInfo);
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
        Set<BeanDefinition> candidates = packageCache.computeIfAbsent(packageName, pkg -> {
            ClassPathScanningCandidateComponentProvider scanner = scannerCache.get();
            return scanner.findCandidateComponents(pkg);
        });
        
        List<MethodDetails> methods = new ArrayList<>();
        
        for (BeanDefinition bean : candidates) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            
            // Find all methods with the target name
            Method[] classMethods = clazz.getDeclaredMethods();
            for (Method method : classMethods) {
                if (method.getName().equals(function)) {
                    MethodDetails methodInfo = new MethodDetails(method, clazz);
                    methods.add(methodInfo);
                    
                    // Cache individual method by their actual signature
                    String methodSignatureKey = packageName + ":" + methodInfo.getMethodSignature();
                    methodSignatureCache.put(methodSignatureKey, methodInfo);
                    
                    // Also cache by parameter count for quick lookup
                    String paramCountKey = packageName + ":" + function + ":paramCount:" + methodInfo.getParameters().length;
                    methodSignatureCache.put(paramCountKey, methodInfo);
                }
            }
        }
        
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
        methodSignatureCache.clear();
        packageCache.clear();
    }
    
    // Method to get cache statistics (useful for monitoring)
    public Map<String, Integer> getCacheStats() {
        return Map.of(
            "methodCacheSize", methodCache.size(),
            "methodSignatureCacheSize", methodSignatureCache.size(),
            "packageCacheSize", packageCache.size()
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
        
        System.out.println("\n=== Method Signature Cache Info ===");
        methodSignatureCache.forEach((key, method) -> 
            System.out.println("Key: " + key + " -> " + method.getMethodSignature()));
    }
}