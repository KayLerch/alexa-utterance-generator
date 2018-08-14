package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON object representing a slot type definition in a skill interaction model
 */
@JsonInclude
public class SlotType {
    @JsonProperty
    private String name;
    @JsonProperty
    private final List<SlotValue> values = new ArrayList<>();

    /**
     * New slot type with name
     * @param name slot type name
     */
    public SlotType(final String name) {
        this.name = name;
    }

    /**
     * Get slot type name
     * @return slot type name
     */
    public String getName() {
        return name;
    }

    public List<SlotValue> getValues() {
        return values;
    }

    /**
     * Add new value to slot type. Ignores blank values
     * @param value slot value, ignores blanks
     */
    @JsonIgnore
    public void addValue(final String value) {
        if (StringUtils.isNotBlank(value)) values.add(new SlotValue(value));
    }

    /**
     * Adds a group of values where the first one gets the default value and the trailing rest get synonyms. Default value also gets the slot id
     * @param values group of values
     */
    @JsonIgnore
    public void addValues(final List<String> values) {
        values.forEach(this::addValue);
    }

    /**
     * Add new value with custom slot id
     * @param id slot id
     * @param value slot value
     */
    @JsonIgnore
    public void addValue(final String id, final String value) {
        this.values.add(new SlotValue(id, value));
    }

    /**
     * Counts values excluding synonyms
     * @return Count of values excluding synonyms
     */
    @JsonIgnore
    public Long countValues() {
        return values.stream().filter(v -> v.name != null && StringUtils.isNotBlank(v.name.value)).count();
    }

    /**
     * Counts values including synonyms
     * @return Count of values including synonyms
     */
    @JsonIgnore
    public Long countValuesWithSynonyms() {
        return (long) values.stream().filter(v -> v.name != null && StringUtils.isNotBlank(v.name.value)).mapToInt(v -> v.name.synonyms.size()).sum();
    }

    /**
     * Adds a group of values under given slot id where the first value of that list gets the default value and the trailing rest get synonyms.
     * @param id slot id
     * @param values group of values
     */
    @JsonIgnore
    public void addValues(final String id, final List<String> values) {
        this.values.add(new SlotValue(id, values));
    }

    /**
     * Returns true if slot type got any values
     * @return true if slot type got any values, otherwise false
     */
    @JsonIgnore
    boolean hasValues() {
        return !values.isEmpty();
    }

    /**
     * JSON object representing a slot value definition in a slot type defintion
     */
    @JsonInclude
    public class SlotValue {
        @JsonProperty
        private final String id;
        @JsonProperty
        private SlotName name;

        /**
         * New slot value with value. Slot id will be the value as well.
         * @param value slot value
         */
        public SlotValue(final String value) {
            this.id = value;
            this.name = new SlotName(value);
        }

        /**
         * New slot value with custom id and value
         * @param id slot id
         * @param value slot value
         */
        public SlotValue(final String id, final String value) {
            this.id = id;
            this.name = new SlotName(id, value);
        }

        /**
         * New slot value with custom id and set of values. First value of that list gets the default value and the trailing rest get synonyms.
         * @param id slot id
         * @param values group of values (includes synonyms)
         */
        public SlotValue(final String id, final List<String> values) {
            this.id = id;
            this.name = new SlotName(id, values);
        }

        /**
         * Get slot id
         * @return slot id
         */
        public String getId() {
            return id;
        }

        /**
         * Get slot name object
         * @return slot name object
         */
        @JsonIgnore
        public SlotName getSlotName() {
            return name;
        }

        /**
         * JSON object representing a slot name definition in a slot value defintion
         */
        @JsonInclude
        public class SlotName {
            @JsonIgnore
            private String id;
            @JsonProperty
            private String value;
            @JsonProperty
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            private final List<String> synonyms = new ArrayList<>();

            /**
             * New SlotName object with value. Slot id will be the value as well.
             * @param value slot value
             */
            private SlotName(final String value) {
                this.value = value;
                this.id = value;
            }

            /**
             * New SlotName object with custom id and value.
             * @param id slot id
             * @param value slot value
             */
            private SlotName(final String id, final String value) {
                this.value = value;
                this.id = id;
            }

            /**
             * New SlotName object with custom id and set of values (1st one gets the default value, trailing rest will be synonyms)
             * @param id slot id
             * @param values set of values
             */
            private SlotName(final String id, final List<String> values) {
                this.value = values.isEmpty() ? "" : values.get(0).trim();
                this.id = id;
                if (values.size() > 1) {
                    values.subList(1, values.size()).forEach(this::addSynonym);
                }
            }

            private SlotName(final String id, final String value, final List<String> synonyms) {
                this.value = value;
                this.id = id;
                this.synonyms.addAll(synonyms);
            }

            @JsonIgnore
            private void addSynonym(final String synonym) {
                if (!this.synonyms.contains(synonym)) {
                    this.synonyms.add(synonym.trim());
                }
            }

            @JsonIgnore
            private void addSynonyms(final List<String> synonyms) {
                synonyms.forEach(this::addSynonym);
            }

            /**
             * Get id
             * @return id
             */
            public String getId() {
                return id;
            }

            /**
             * Get value
             * @return value
             */
            public String getValue() {
                return value;
            }

            /**
             * Get synonyms
             * @return synonyms
             */
            public List<String> getSynonyms() {
                return synonyms;
            }
        }
    }
}
