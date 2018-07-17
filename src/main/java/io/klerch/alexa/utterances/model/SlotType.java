package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.klerch.alexa.utterances.util.Resolver;
import io.klerch.alexa.utterances.util.ResourceReader;
import org.apache.commons.lang3.StringUtils;

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
            this.id = name.id;
        }

        public String getId() {
            return id;
        }

        @JsonInclude
        private class SlotName {
            @JsonIgnore
            private String id;
            @JsonProperty
            private String value;
            @JsonProperty
            private final List<String> synonyms = new ArrayList<>();

            private SlotName(final String value) {
                final String[] idValues = value.split(":");



                final String valuesString = idValues[idValues.length > 1 ? 1 : 0];

                // first eliminate the brackets
                final List<String> values = Resolver.resolveSlotValue(valuesString.replace("{","").replace("}", ""));

                if (!values.isEmpty()) {
                    if (idValues.length > 1 && StringUtils.isNotBlank(idValues[0])) {
                        this.id = idValues[0].trim();
                    }
                    if (values.size() > 1) {
                        this.addSynonyms(values.subList(1, values.size()));
                    }
                    this.value = values.get(0).trim();
                }
            }

            private void addSynonym(final String synonym) {
                if (!this.synonyms.contains(synonym)) {
                    this.synonyms.add(synonym.trim());
                }
            }

            private void addSynonyms(final List<String> synonyms) {
                synonyms.forEach(this::addSynonym);
            }
        }
    }
}
