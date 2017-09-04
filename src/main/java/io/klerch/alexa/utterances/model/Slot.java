package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Slot {
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String type;
    @JsonProperty
    private final List<String> samples = new ArrayList<>();

    public Slot(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    public void addSample(final String sample) {
        this.samples.add(sample);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
