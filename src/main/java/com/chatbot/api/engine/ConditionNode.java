package com.chatbot.api.engine;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.chatbot.api.models.Workflow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("condition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ConditionNode extends WorkflowNode
{

	private String expression;
	
	@Override
	public String performExecution(Workflow workflow) {
		
		ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariables(workflow.inputs);
        
        boolean result = parser.parseExpression(expression).getValue(context, Boolean.class);
		
        return result ?	"success" : "failure";
	}
}
