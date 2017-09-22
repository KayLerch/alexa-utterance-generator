package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("languageModel")
public class LanguageModel extends InteractionModel {
    @JsonProperty
    private final String invocationName;

    public LanguageModel(final String invocationName) {
        this.invocationName = invocationName;
    }
}
