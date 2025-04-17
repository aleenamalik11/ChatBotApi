package com.chatbot.api.models;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

class ConditionNode implements WorkflowNode {
    private String condition;  // e.g. "user.balance > 1000"
    private WorkflowNode trueBranch;
    private WorkflowNode falseBranch;

    public void execute() {
    	ExpressionParser parser = new SpelExpressionParser();
        boolean result = parser.parseExpression(condition).getValue();
        if (result) trueBranch.execute();
        else falseBranch.execute();
    }
}

