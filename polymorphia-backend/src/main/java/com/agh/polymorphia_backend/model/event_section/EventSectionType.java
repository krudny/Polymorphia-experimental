package com.agh.polymorphia_backend.model.event_section;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventSectionType {
    ASSIGNMENT("assignment"),
    TEST("test"),
    PROJECT("project"),
    TASK("task");

    private final String jsonValue;

    EventSectionType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
}
