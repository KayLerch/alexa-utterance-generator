package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("interactionModel")
public class SMAPIModel {
    @JsonProperty
    private final LanguageModel languageModel = new LanguageModel();

    public SMAPIModel(final String invocationName) {
        languageModel.invocationName = invocationName;
    }

    @JsonIgnore
    public LanguageModel getModel() {
        return this.languageModel;
    }

    public class LanguageModel extends InteractionModel {
        @JsonProperty
        private String invocationName;
    }
}
