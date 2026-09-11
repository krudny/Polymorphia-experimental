package com.agh.polymorphia_backend.service.task;

import org.springframework.stereotype.Component;

@Component
public class TaskOutputTruncator {

    public static final int MAX_OUTPUT_LENGTH = 32_000;
    private static final String TRUNCATION_SUFFIX = "\n...(obcięto)";

    public String truncate(String output) {
        if (output == null || output.length() <= MAX_OUTPUT_LENGTH) {
            return output;
        }

        return output.substring(0, MAX_OUTPUT_LENGTH) + TRUNCATION_SUFFIX;
    }
}
