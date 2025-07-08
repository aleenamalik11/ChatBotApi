package com.chatbot.api.engine;

import java.util.Scanner;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.stereotype.Component;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.models.WorkflowInput;
import com.chatbot.api.utils.RuntimeTypeConverter;
import com.chatbot.api.utils.SpringBeanProvider;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("input")
@Component
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class InputNode extends WorkflowNode {
	
    private String prompt;
    
    @Override
	public String performExecution(Workflow workflow) {
    	if (prompt == null || prompt.isBlank()) {
            return "failure";
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);

        try {        	
        
	        for (WorkflowInput variable : workflow.inputVariables) {
	            if (prompt.toLowerCase().contains(variable.name.toLowerCase())) {
	                System.out.print(variable.name + ": ");
	                String input = scanner.nextLine();
	
	                RuntimeTypeConverter converterUtils = SpringBeanProvider.getConverterUtils();
	                
	                Object convertedInput = converterUtils.castToRuntimeType(input, variable.type);
	                workflow.inputs.put(variable.name, convertedInput);
	            }
	        }
	        
	        scanner.close();
        }
        catch(Exception ex) {
        	 return "failure";
        }
        return "success";
	}
}
