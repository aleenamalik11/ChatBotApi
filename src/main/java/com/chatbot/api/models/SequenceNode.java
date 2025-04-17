package com.chatbot.api.models;

import java.util.List;

class SequenceNode implements WorkflowNode {
    private List<WorkflowNode> children;
    public void execute() {
        for (WorkflowNode child : children) {
            child.execute();
        }
    }
}

