package com.chatbot.api.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
import com.chatbot.api.models.WorkflowInput;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("input")
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
	                System.out.print(variable + ": ");
	                String input = scanner.nextLine();
	
	                ConversionService conversionService = new DefaultConversionService();
	                Object convertedInput = conversionService.convert(input, Class.forName(variable.type));
	                workflow.inputs.put(variable.name, convertedInput);
	            }
	        }
        }
        catch(Exception ex) {
        	
        }
        return "success";
	}
}
