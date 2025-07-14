package com.chatbot.api.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.chatbot.api.models.MethodResult;
import com.chatbot.api.utils.MethodInvoker;

public class DSLInterpretor {

	@Autowired
	private MethodInvoker methodInvoker;
	@Autowired
	private WorkflowEngine workflowEngine;
	
    private final Map<String, Object> variables = new HashMap<>();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final Map<String, Object> contextInputs;

    public DSLInterpretor(Map<String, Object> inputs) {
        this.contextInputs = inputs;
    }

    public void execute(String dsl) {
        try {
            String[] lines = dsl.split("\\r?\\n");
            for (String rawLine : lines) {
                String line = rawLine.trim();

                if (line.startsWith("if")) {
                    if (!evaluateCondition(line)) {
                        continue; // Skip next line(s) if condition fails
                    }
                } else if (line.startsWith("result = call")) {
                    String methodName = line.split("call")[1].trim();
                    Object result = invokeFunction(methodName);
                    variables.put("result", result);
                } else if (line.startsWith("call")) {
                    String methodName = line.split("call")[1].trim();
                    invokeFunction(methodName);
                } else if (line.startsWith("print")) {
                    String var = line.split("print")[1].trim();
                    Object value = variables.getOrDefault(var, var);
                    System.out.println(value);
                } else if (line.startsWith("start workflow")) {
                    String workflowName = extractQuotedText(line);
                    if (workflowName != null) {
                        startWorkflow(workflowName);
                    } else {
                        throw new RuntimeException("Workflow name missing or malformed.");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String extractQuotedText(String line) {
        Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void startWorkflow(String name) {
        // You can integrate your existing engine here
        workflowEngine.startEngine(name); // Example call
    }


    private boolean evaluateCondition(String line) {
        try {
            // Create evaluation context with your contextInputs
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(contextInputs);
            
            // Parse and evaluate the SpEL expression
            Expression expression = parser.parseExpression(line);
            Object result = expression.getValue(context);
            
            // Convert result to boolean
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            
            // Handle other types that might be truthy/falsy
            return result != null && !result.equals("");
            
        } catch (Exception e) {
            // Log the error or handle as needed
            System.err.println("Error evaluating condition: " + line + " - " + e.getMessage());
            return false;
        }
    }


    @SuppressWarnings("unlikely-arg-type")
	private Object invokeFunction(String methodName) throws NoSuchMethodException {
    	MethodResult result = methodInvoker.invoke(contextInputs, methodName, "com.chatbot.customservices");
        
        if(result.equals("Method not found")) {
        	throw new NoSuchMethodException("No suitable method found: " + methodName);
        }
        if(result.equals("failure")) {
        	throw new RuntimeException("Error invoking method: " + methodName);
        }
        
        return result.getOutput();
    }
}

