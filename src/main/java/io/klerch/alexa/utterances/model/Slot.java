package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON object representing a slot reference in the intent object of a skill interaction model
 */
public class Slot {
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String type;
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> samples = new ArrayList<>();

    /**
     * New Slot with name and type
     * @param name slot name
     * @param type slot type name
     */
    public Slot(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Adds a new sample to the slot
     * @param sample value
     */
    public void addSample(final String sample) {
        this.samples.add(sample);
    }

    /**
     * Get slot name
     * @return slot name
     */
    public String getName() {
        return name;
    }

    /**
     * Get slot type
     * @return slot type
     */
    public String getType() {
        return type;
    }
}
