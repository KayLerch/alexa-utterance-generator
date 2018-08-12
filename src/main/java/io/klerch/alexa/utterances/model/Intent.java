package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON object representing an intent in the skill interaction model
 */
public class Intent {
    @JsonProperty
    private final String name;
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> samples = new ArrayList<>();
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Slot> slots = new ArrayList<>();

    /**
     * New Intent with name
     * @param name intent name
     */
    public Intent(final String name) {
        this.name = name;
    }

    /**
     * Get name of intent
     * @return name of intent
     */
    public String getName() {
        return name;
    }

    /**
     * Get list of sample utterances
     * @return list of sample utterances
     */
    public List<String> getSamples() {
        return samples;
    }

    /**
     * Add new sample utterance to intent
     * @param sample sample utterance
     */
    public void addSample(final String sample) {
        samples.add(sample);
    }

    /**
     * Add new slot reference to intent
     * @param slot slot object with reference name and slot type
     */
    public void addSlot(final Slot slot) {
        slots.add(slot);
    }

    /**
     * Removes duplicate sample utterances and sorts alphabetically
     */
    @JsonIgnore
    public void deduplicateAndSortSamples() {
        samples = samples.stream().distinct().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    /**
     * Returns true if intent got any sample utterances
     * @return true if intent got any sample utterances. otherwise returns false.
     */
    @JsonIgnore
    public boolean hasSamples() {
        return !samples.isEmpty();
    }

    /**
     * Counts the number of sample utterances contained in the intent
     * @return Count of sample utterances contained in the intent
     */
    @JsonIgnore
    public int countSamples() {
        return samples.size();
    }

    /**
     * Counts the number of slot references contained in the intent
     * @return Count of slot references contained in the intent
     */
    @JsonIgnore
    public int countSlots() {
        return slots.size();
    }

    /**
     * Compares sample utterances of this intent with sample utterances with the intent given to this method.
     * Returns the list of duplicate sample utterances contained in both intents
     * @param intent intent whose sample utterances to compare with sample utterance of this intent
     * @return list of duplicate sample utterances contained in both intents
     */
    @JsonIgnore
    public List<String> getDuplicateSamplesWith(final Intent intent) {
        final Intent intentWithLessSamples = intent.samples.size() > this.samples.size() ? this : intent;
        final Intent intentWithMoreSamples = intentWithLessSamples.equals(this) ? intent : this;
        // create copy of samples
        final List<String> samples = new ArrayList<>(intentWithLessSamples.samples);
        samples.retainAll(intentWithMoreSamples.samples);
        return samples;
    }
}
