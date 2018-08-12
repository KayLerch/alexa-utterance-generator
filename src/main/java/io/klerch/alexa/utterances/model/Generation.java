package io.klerch.alexa.utterances.model;

/**
 * Result set object representing output of Generator
 */
public class Generation {
    private final InteractionModel model;
    private final long numberOfUtterances;
    private final long numberOfCustomIntents;
    private final long numberOfBuiltinIntents;
    private final long numberOfIntents;
    private final long numberOfSlots;
    private final long numberOfSlotTypes;
    private final long numberOfSlotValues;
    private final long numberOfSlotValuesWithSynonyms;

    /**
     * Create new result set from interaction model
     * @param model skill interaction model object
     */
    public Generation(final InteractionModel model) {
        this.model = model;
        this.numberOfUtterances = model.getModel().getIntents().stream().mapToLong(Intent::countSamples).sum();
        this.numberOfIntents = (long) model.getModel().getIntents().size();
        this.numberOfCustomIntents = (model.getModel().getIntents().stream().filter(intent -> !intent.getName().startsWith("AMAZON.")).count());
        this.numberOfBuiltinIntents = this.numberOfIntents - this.numberOfCustomIntents;
        this.numberOfSlots = model.getModel().getIntents().stream().mapToLong(Intent::countSlots).sum();
        this.numberOfSlotTypes = (long) model.getModel().getSlotTypes().size();
        this.numberOfSlotValues = model.getModel().getSlotTypes().stream().mapToLong(SlotType::countValues).sum();
        this.numberOfSlotValuesWithSynonyms = this.numberOfSlotValues + model.getModel().getSlotTypes().stream().mapToLong(SlotType::countValuesWithSynonyms).sum();
    }

    /**
     * Get skill interaction model
     * @return skill interaction model
     */
    public InteractionModel getModel() {
        return model;
    }

    /**
     * Returns number of utterances in the skill interaction model
     * @return number of utterances in the skill interaction model
     */
    public long getNumberOfUtterances() {
        return numberOfUtterances;
    }

    /**
     * Returns number of intents in the skill interaction model
     * @return number of intents in the skill interaction model
     */
    public long getNumberOfIntents() {
        return numberOfIntents;
    }

    /**
     * Returns number of custom intents in the skill interaction model
     * @return number of custom intents in the skill interaction model
     */
    public long getNumberOfCustomIntents() {
        return numberOfCustomIntents;
    }

    /**
     * Returns number of builtin intents in the skill interaction model
     * @return number of builtin intents in the skill interaction model
     */
    public long getNumberOfBuiltinIntents() {
        return numberOfBuiltinIntents;
    }

    /**
     * Returns number of slot types in the skill interaction model
     * @return number of slot types in the skill interaction model
     */
    public long getNumberOfSlotTypes() {
        return numberOfSlotTypes;
    }

    /**
     * Returns number of referenced slots in utterances in the skill interaction model
     * @return number of referenced slots in utterances in the skill interaction model
     */
    public long getNumberOfSlots() {
        return numberOfSlots;
    }

    /**
     * Returns number of slot values in the skill interaction model excluding synonyms
     * @return number of slot values in the skill interaction model excluding synonyms
     */
    public long getNumberOfSlotValues() {
        return numberOfSlotValues;
    }

    /**
     * Returns number of slot values in the skill interaction model including synonyms
     * @return number of slot values in the skill interaction model including synonyms
     */
    public long getNumberOfSlotValuesWithSynonyms() {
        return numberOfSlotValuesWithSynonyms;
    }
}
