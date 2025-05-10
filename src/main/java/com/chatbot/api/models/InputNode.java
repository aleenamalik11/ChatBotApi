package com.chatbot.api.models;

import org.springframework.data.annotation.TypeAlias;

import com.chatbot.api.interfaces.WorkflowNode;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("input")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class InputNode implements WorkflowNode {
    private String prompt;
    // other fields as needed
}
