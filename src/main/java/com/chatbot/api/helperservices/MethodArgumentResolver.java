package com.chatbot.api.helperservices;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.chatbot.api.dto.MethodDetails;
import com.chatbot.api.dto.ParameterMetadata;
import com.chatbot.api.utils.RuntimeTypeConverter;

@Service
public class MethodArgumentResolver {

    private final RuntimeTypeConverter converter;

    public MethodArgumentResolver(RuntimeTypeConverter converter) {
        this.converter = converter;
    }

    public Object[] resolve(MethodDetails methodDetails, Map<String, Object> inputs) {
        ParameterMetadata[] parameters = methodDetails.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            ParameterMetadata parameter = parameters[i];
            if (inputs == null || !inputs.containsKey(parameter.getInputName())) {
                throw new IllegalArgumentException(
                    "Missing input '" + parameter.getInputName() +
                    "' for parameter '" + parameter.getMethodParameterName() + "'."
                );
            }

            args[i] = converter.castToRuntimeType(
                inputs.get(parameter.getInputName()),
                parameter.getGenericType()
            );
        }

        return args;
    }
}
