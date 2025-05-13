package com.chatbot.api.engine;

import java.util.Map;
import java.util.Scanner;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.models.Workflow;
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
    private Map<String, Object> inputs;
    
    @Override
	public String performExecution(Workflow workflow) {
    	if (prompt == null || prompt.isBlank()) {
            return "failure";
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);

        for (String variable : workflow.inputVariables) {
            if (prompt.toLowerCase().contains(variable.toLowerCase())) {
                System.out.print(variable + ": ");
                String input = scanner.nextLine();
                inputs.put(variable, input);
            }
        }

        return "success";
	}
}
