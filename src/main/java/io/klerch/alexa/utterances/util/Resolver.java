package io.klerch.alexa.utterances.util;

import io.klerch.alexa.utterances.model.Slot;
import io.klerch.alexa.utterances.model.SlotType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver provides a bunch of methods used by Generator to resolve artifacts in grammar specification
 */
public class Resolver {
    /**
     * Regex pattern to extract invocation name specification (e.g. Invocation: my skill invocation name)
     */
    public static final Pattern invocationDefintion = Pattern.compile("^(invocation:)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    /**
     * Regex pattern to extract intent specification (e.g. MyIntent: sample utterance)
     */
    public static final Pattern intentDefinition = Pattern.compile("^([^{]+[:])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    /**
     * Regex pattern to extract slot type definition (e.g. {MySlotType}: value, value1, value2)
     */
    public static final Pattern slotTypeDefinition = Pattern.compile("^(\\{(.+?)}[:])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    /**
     * Regex pattern to extract slot placeholder in a sample utterance (e.g. my {{slot:type}})
     */
    public static final Pattern slotInUtteranceDefinition = Pattern.compile("(\\{\\{(.+?)}})", Pattern.CASE_INSENSITIVE);
    /**
     * Regex pattern to extract remaining placeholders not already substituted with an internal reference during an interim step of the generation process (e.g. {placeholder} but not {!hashcode})
     */
    public static final Pattern placeholderInUtteranceExcludingResolved = Pattern.compile("\\{[^!](.+?)}", Pattern.CASE_INSENSITIVE);
    /**
     * Regex pattern to extract placeholders that were substituted with an internal reference during an interim step of the generation process (e.g. {!hashcode})
     */
    public static final Pattern resolvedPlaceholders = Pattern.compile("(\\{!(.+?)})", Pattern.CASE_INSENSITIVE);
    /**
     * List of name appendices for slots with duplicate names within one sample utterance
     */
    private static List<String> slotNameAppendices = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    /**
     * Extracts invocation name specification (e.g. Invocation: my skill invocation name)
     * @param line sample
     * @return if sample represents an invocation name specification it returns the extracted key and value (invocation name)
     */
    public static Optional<ImmutablePair<String, String>> resolveInvocationDefinition(final String line) {
        final Optional<ImmutablePair<String, String>> pair = resolveDefinition(invocationDefintion, line);
        pair.ifPresent((invocation) -> {
            // apply rules
            Validator.validateInvocationName(invocation.left);
        });
        return pair;
    }

    /**
     * Extracts intent specification (e.g. MyIntent: sample utterance)
     * @param line sample
     * @return if sample represents an intent specification it returns the extracted key (intent name) and value (first sample utterance)
     */
    public static Optional<ImmutablePair<String, String>> resolveIntentDefinition(final String line) {
        final Optional<ImmutablePair<String, String>> pair = resolveDefinition(intentDefinition, line);
        pair.ifPresent((intentUtterance) -> {
            // apply rules
            Validator.validateIntentName(intentUtterance.left);
        });
        return pair;
    }

    /**
     * Extracts slot type definition (e.g. {MySlotType}: value, value1, value2)
     * @param line sample
     * @return if sample represents a slot type specification it returns the extracted key (slot type name) and value (first line of slot values)
     */
    public static Optional<ImmutablePair<String, String>> resolveSlotTypeDefinition(final String line) {
        final Optional<ImmutablePair<String, String>> pair = resolveDefinition(slotTypeDefinition, line);
        pair.ifPresent((slotValues) -> {
            // apply rules
            Validator.validateSlotName(slotValues.left);
        });
        return pair;
    }

    /**
     * Generic resolver which extracts information based on the the pattern given
     * @param pattern pattern to apply on sample
     * @param line sample
     * @return if sample matches pattern it returns a key value pair
     */
    private static Optional<ImmutablePair<String, String>> resolveDefinition(final Pattern pattern, final String line) {
        final Matcher intentDefMatch = pattern.matcher(line);
        if (intentDefMatch.find()) {
            final String intentName = intentDefMatch.group(1);
            final String utterance = intentDefMatch.replaceFirst("");
            return Optional.of(new ImmutablePair<>(intentName.replace(":", "").replace("{", "").replace("}", "").trim(), utterance.trim()));
        }
        return Optional.empty();
    }

    /**
     * Resolves a list of slot values by normalizing format and splitting up multiple values in one line to multiple values
     * @param values list of samples with value specifications
     * @return list of values in the format of key:value
     */
    public static List<String> resolveSlotValues(final List<String> values) {
        final List<String> resolvedValues = new ArrayList<>();
        values.forEach(valueLine -> {
            // hold slot id in case it is defined in order to apply it to all values in the line
            final AtomicReference<String> valueKey = new AtomicReference<>("");
            // separate values
            Arrays.stream(valueLine.replaceAll("[{}]", "").split("[|,;]", -1)).distinct().forEach(value -> {
                if (StringUtils.isNotBlank(value)) {
                    final String[] keyValue = value.split(":");
                    // key as defined otherwise key as defined for preceding value otherwise key is value itself
                    final String key = keyValue.length > 1 && StringUtils.isNotBlank(keyValue[0]) ? keyValue[0] :
                            StringUtils.isNotBlank(valueKey.get()) ? valueKey.get() : value.replace(":", "");
                    valueKey.set(key);
                    resolvedValues.add(key + ":" + (keyValue.length > 1 ? keyValue[1] : value));
                } else {
                    resolvedValues.add("");
                }
            });
        });
        return resolvedValues;
    }

    /**
     * Given a set of already existing slot names within an utterance and another slot name this method takes care of returning a unique slot name not duplicating a name of the existing ones
     * @param existingSlots list of existing slot names within a sample utterance
     * @param slotName next slot name within the sample utterance
     * @return unique name for slotName
     */
    public static String resolveToUniqueSlotName(final List<Slot> existingSlots, final String slotName) {
        final AtomicReference<String> newSlotName = new AtomicReference<>(slotName);
        final AtomicInteger index = new AtomicInteger(0);
        final Integer maxIndex = slotNameAppendices.size();
        while(existingSlots.stream().anyMatch(s -> s.getName().equals(newSlotName.get())) && index.get() < maxIndex) {
            newSlotName.set(slotName + "_" + slotNameAppendices.get(index.getAndIncrement()));
        }
        return newSlotName.get();
    }

    /**
     * Given a slot type name and a list of values in the format of key:value this method creates a new SlotType object with slot id, slot value and synonyms
     * @param slotTypeName name for the slot type
     * @param values list of values for the slot type
     * @return slot type object
     */
    public static SlotType resolveSlotType(final String slotTypeName, List<String> values) {
        final SlotType slotType = new SlotType(slotTypeName);
        final Map<String, List<String>> resolvedById = new HashMap<>();
        values.forEach(value -> {
            final String[] keyValue = value.split(":");
            final String key = keyValue[0];
            final String val = keyValue.length > 1 ? keyValue[1] : key;
            resolvedById.putIfAbsent(key, new ArrayList<>());
            resolvedById.get(key).add(val);
        });
        resolvedById.forEach(slotType::addValues);
        return slotType;
    }
}