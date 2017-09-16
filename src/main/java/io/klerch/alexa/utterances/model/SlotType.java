package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.klerch.alexa.utterances.util.Resolver;
import io.klerch.alexa.utterances.util.ResourceReader;

import java.util.ArrayList;
import java.util.List;

@JsonInclude
public class SlotType {
    @JsonProperty
    private String name;
    @JsonProperty
    private final List<SlotValue> values = new ArrayList<>();

    public SlotType(final String name) {
        this.name = name;
        ResourceReader.getPlaceholderValueList(name).ifPresent(this::addValues);
    }

    public String getName() {
        return name;
    }

    public void addValue(final String value) {
        values.add(new SlotValue(value));
    }

    public void addValues(final List<String> values) {
        values.forEach(this::addValue);
    }

    @JsonInclude
    private class SlotValue {
        @JsonProperty
        private final String id;
        @JsonProperty
        private SlotName name;

        public SlotValue(final String value) {
            this.name = new SlotName(value);
            this.id = name.synonyms.isEmpty() ? null : name.value;
        }

        public String getId() {
            return id;
        }

        @JsonInclude
        private class SlotName {
            @JsonProperty
            private String value;
            @JsonProperty
            private final List<String> synonyms = new ArrayList<>();

            private SlotName(final String value) {
                // first eliminate the brackets
                final List<String> values = Resolver.resolveSlotValue(value.replace("{","").replace("}", ""));

                if (!values.isEmpty()) {
                    this.value = values.get(0);
                }

                if (values.size() > 1) {
                    this.addSynonyms(values.subList(1, values.size()));
                }
            }

            private void addSynonym(final String synonym) {
                if (!this.synonyms.contains(synonym)) {
                    this.synonyms.add(synonym);
                }
            }

            private void addSynonyms(final List<String> synonyms) {
                synonyms.forEach(this::addSynonym);
            }
        }
    }
}
