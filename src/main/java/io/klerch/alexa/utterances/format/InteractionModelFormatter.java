package io.klerch.alexa.utterances.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.utterances.model.InteractionModel;

public class InteractionModelFormatter implements Formatter {
    private InteractionModel model;

    @Override
    public void before() {
        model = new InteractionModel();
    }

    @Override
    public void write(final String sample) {
        model.addSample(sample);
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String generateSchema() {
        try {
            return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(model);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
