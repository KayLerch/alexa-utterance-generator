package io.klerch.alexa.utterances.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.utterances.model.LanguageModel;

public class SMAPIFormatter implements Formatter {
    private final LanguageModel model;

    public SMAPIFormatter(final String invocatioName) {
        this.model = new LanguageModel(invocatioName);
    }

    @Override
    public void before() { }

    @Override
    public boolean write(final String sample) {
        model.addSample(sample);
        return true;
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String generateSchema() {
        try {
            return new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(model);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
