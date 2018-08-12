package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * JSON object representing a skill interaction model
 */
@JsonRootName("interactionModel")
public class InteractionModel {
    @JsonProperty
    private final LanguageModel languageModel;

    public InteractionModel(final String invocationName) {
        languageModel = new LanguageModel(invocationName);
    }

    /**
     * Get underlying language model JSON object
     * @return language model JSON object
     */
    @JsonIgnore
    public LanguageModel getModel() {
        return this.languageModel;
    }

    /**
     * Set invocation name of underlying language model JSON object
     * @param invocationName invocation name for the skill
     */
    @JsonIgnore
    public void setInvocationName(final String invocationName) {
        languageModel.invocationName = invocationName;
    }

    /**
     * Get invocation name of underlying language model JSON object
     * @return invocation name for the skill
     */
    @JsonIgnore
    public String getInvocationName() {
        return languageModel.invocationName;
    }

    /**
     * Adds a new intent to the underlying language model JSON object
     * @param intentName name for new intent
     * @return intent JSON object
     */
    public Intent addIntent(final String intentName) {
        final Intent intent = new Intent(intentName);
        languageModel.intents.add(intent);
        return intent;
    }

    /**
     * Adds a new intent to the underlying language model JSON object
     * @param intent intent JSON object
     */
    public void addIntent(final Intent intent) {
        languageModel.intents.add(intent);
    }

    /**
     * Adds a new slot type to the underlying language model JSON object
     * @param slotType new slot type
     */
    public void addSlotType(final SlotType slotType) {
        languageModel.types.add(slotType);
    }

    /**
     * Checks if slot type already exists in the underlying language model JSON object
     * @param slotTypeName name of slot type
     * @return true if name of slot type is already represented by an existing slot type in the underlying language model JSON object
     */
    @JsonIgnore
    private boolean hasSlotType(final String slotTypeName) {
        return languageModel.types.stream().anyMatch(type -> type.getName().equals(slotTypeName));
    }

    /**
     * Checks if slot type of a given slot object already exists in the underlying language model JSON object
     * @param slot slot with reference to slot type
     * @return true if name of the referenced slot type is already represented by an existing slot type in the underlying language model JSON object
     */
    @JsonIgnore
    public boolean hasSlotType(final Slot slot) {
        return this.hasSlotType(slot.getType());
    }

    /**
     * Does some validation of the model. Throws validation exceptions.
     */
    public void validate() {
        // ensure all intents got samples except for builtins
        Validate.isTrue(languageModel.intents.stream().allMatch(intent -> intent.getName().startsWith("AMAZON.") || intent.hasSamples()), "At least one of you custom intents does not have any sample utterance.");
        // ensure all slots got values
        Validate.isTrue(languageModel.types.stream().allMatch(type -> type.getName().startsWith("AMAZON.") || type.hasValues()), "At least one of your custom slot types does not have any value.");
        // needs intents
        Validate.isTrue(!languageModel.intents.isEmpty(), "There is no intent defined in your grammar.");
        // ensure no duplicates exists across two intents
        findDuplicateSamples(0, 1);
    }

    /**
     * Does some cleanup in the model.
     */
    public void cleanUp() {
        // sort intents by name
        languageModel.intents.sort(Comparator.comparing(Intent::getName));
        // sort slot types by name
        languageModel.types.sort(Comparator.comparing(SlotType::getName));
        // deduplicate and sort utterances for each intent
        languageModel.intents.forEach(Intent::deduplicateAndSortSamples);
    }

    /**
     * Compares sample utterances of two given intents and throws and exception in case of exact duplicates
     * @param intentId1 intent 1
     * @param intentId2 intent 2
     */
    private void findDuplicateSamples(final int intentId1, final int intentId2) {
        if (intentId1 >= languageModel.intents.size() - 1) {
            return;
        } else if (intentId2 >= languageModel.intents.size()) {
            findDuplicateSamples(intentId1 + 1, intentId1 + 2);
        } else {
            // compare two intents and get their duplicate samples
            final List<String> duplicates = languageModel.intents.get(intentId1).getDuplicateSamplesWith(languageModel.intents.get(intentId2));
            // should never have any duplicates
            Validate.isTrue(duplicates.isEmpty(), "You got " + duplicates.size() + " overlapping sample utterances in your intents " + languageModel.intents.get(intentId1).getName() + " and " + languageModel.intents.get(intentId2).getName() + " (e.g. " + (duplicates.isEmpty() ? "" : duplicates.get(0)) + ")");
            // continue with next pair
            findDuplicateSamples(intentId1, intentId2 + 1);
        }
    }

    /**
     * JSON object representing the language model in a skill interaction model
     */
    public class LanguageModel {
        @JsonProperty
        private final List<Intent> intents = new ArrayList<>();
        @JsonProperty
        private final List<SlotType> types = new ArrayList<>();
        @JsonProperty
        private String invocationName;

        /**
         * New language model
         */
        public LanguageModel() {
        }

        /**
         * New language model with invocation name
         * @param invocationName invocation name
         */
        public LanguageModel(final String invocationName) {
            this.invocationName = StringUtils.lowerCase(invocationName);
        }

        /**
         * Get list of intents defined in the model
         * @return list of intents defined in the model
         */
        @JsonIgnore
        public List<Intent> getIntents() {
            return intents;
        }

        /**
         * Get list of slot types defined in the model
         * @return list of slot types defined in the model
         */
        @JsonIgnore
        public List<SlotType> getSlotTypes() {
            return types;
        }
    }
}
