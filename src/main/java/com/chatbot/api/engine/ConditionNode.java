package com.chatbot.api.engine;

import java.util.Map;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.chatbot.api.models.Workflow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("condition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ConditionNode extends WorkflowNode
{

	private String expression;
	private Map<String, Object> inputs;
	
	@Override
	public String performExecution(Workflow workflow) {
		
		ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariables(inputs);
        
        boolean result = parser.parseExpression(expression).getValue(context, Boolean.class);
		
        return result ?	"success" : "failure";
	}
}
