package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("interactionModel")
public class SMAPIModel {
    @JsonProperty
    private final LanguageModel languageModel;

    public SMAPIModel(final String invocationName) {
        languageModel = new LanguageModel(invocationName);
    }

    @JsonIgnore
    public LanguageModel getModel() {
        return this.languageModel;
    }

    public class LanguageModel extends SkillBuilderModel {
        public LanguageModel(final String invocationName) {
            super(invocationName);
        }
    }
}
