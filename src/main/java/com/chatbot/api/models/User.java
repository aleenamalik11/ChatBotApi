package com.chatbot.api.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "User", description = "A user in the system")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {
	@Schema(description = "User ID", example = "abc123")
    private String id;

    @Schema(description = "Full name", example = "Alice Smith")
    private String name;

    @Schema(description = "Age of the user", example = "25")
    private int age;

}
