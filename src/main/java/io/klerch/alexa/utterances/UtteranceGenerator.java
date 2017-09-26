package io.klerch.alexa.utterances;

import io.klerch.alexa.utterances.format.*;
import io.klerch.alexa.utterances.format.Formatter;
import io.klerch.alexa.utterances.model.LanguageModel;
import io.klerch.alexa.utterances.output.ConsoleOutputWriter;
import io.klerch.alexa.utterances.output.FileOutputWriter;
import io.klerch.alexa.utterances.output.OutputWriter;
import io.klerch.alexa.utterances.util.Resolver;
import io.klerch.alexa.utterances.util.ResourceReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UtteranceGenerator {
    // 1) Set the key of a file with utterances you created in the utterances-folder
    private final static String utteranceFileKey = "harmony"; // e.g. "booking" for using "/resources/output/utterances/booking.grammar"

    // 2) choose one of  the output writers
    private static final OutputWriter OUTPUT_WRITER = new FileOutputWriter(utteranceFileKey);
    //private static final OutputWriter OUTPUT_WRITER = new ConsoleOutputWriter();

    // 3) choose formatter
    //private static final Formatter FORMATTER = new SMAPIFormatter("my invocation name");
    //private static final Formatter FORMATTER = new SkillBuilderFormatter();
    private static final Formatter FORMATTER = new UtteranceListFormatter();
    //private static final Formatter FORMATTER = new WeightedSegmentsFormatter(1); // use booking2 as utteranceFileKey for an example

    // 4) run and done
    public static void main(final String [] args) {
        generateUtterances(Arrays.stream(args).findFirst().orElse(utteranceFileKey));
        OUTPUT_WRITER.beforeWrite(FORMATTER);
        try {
            final List<String> utterances = new ArrayList<>();
            intentsAndUtterances.forEach((intent, utterancesOfIntent) -> {
                utterances.addAll(utterancesOfIntent.stream().map(utterance -> intent + " " + utterance).collect(Collectors.toList()));
            });
            utterances.stream().sorted(String::compareToIgnoreCase).distinct().forEach(OUTPUT_WRITER::write);
        } finally {
            OUTPUT_WRITER.print();
        }
    }

    // stores the list of values contained in slots of utterances
    private static final Map<String, List<String>> placeholderValues = new HashMap<>();

    private static void generateUtterances(final String utteranceResourceId) {
        ResourceReader.getUtteranceList(utteranceResourceId).forEach(utterance -> {
            final Map<String, String> placeholders = new HashMap<>();
            final StringBuffer buffer = new StringBuffer();
            // extract all the slots found in the utterance
            final Matcher placeholdersInUtterance = Pattern.compile("\\{(.*?)\\}").matcher(utterance);
            // for any of the placeholder ...
            while (placeholdersInUtterance.find()) {
                final String placeholderName = placeholdersInUtterance.group(1);
                // generate unique placeholder-key
                final String placeholderKey = "{" + UUID.randomUUID().toString() + "}";
                placeholders.put(placeholderKey, placeholderName);
                placeholdersInUtterance.appendReplacement(buffer, Matcher.quoteReplacement(placeholderKey));
            }
            placeholdersInUtterance.appendTail(buffer);

            final String finalUtterance = buffer.toString();
            generatePermutations(getPlaceholders(placeholders), 0, finalUtterance);
        });
    }

    private static void generatePermutations(final List<Pair<String, List<String>>> lists, final int depth, final String utterance)
    {
        if(depth == lists.size()) {
            store(utterance.trim().replaceAll("\\s+"," "));
            return;
        }

        for(int i = 0; i < lists.get(depth).getValue().size(); ++i) {
            final Pair<String, List<String>> placeholder = lists.get(depth);
            final String placeholderValue = placeholder.getValue().get(i);
            final String placeholderName = placeholder.getKey();
            generatePermutations(lists, depth + 1, utterance.replace(placeholderName, placeholderValue));
        }
    }

    private static Map<String, List<String>> intentsAndUtterances = new HashMap<>();
    private static String lastIntent = "";

    private static void store(final String utterance) {
        final String intent = (utterance.contains(":") ? utterance.split(":")[0].trim() : "").trim();
        final String text = (utterance.contains(":") ? utterance.substring(utterance.indexOf(":") + 1) : utterance).trim();

        // update last intent if current line defined a new one
        lastIntent = StringUtils.isBlank(intent) ? lastIntent : intent;
        intentsAndUtterances.putIfAbsent(lastIntent, new ArrayList<>());

        final AtomicBoolean utteranceContainedInAnotherIntent = new AtomicBoolean(false);
        final AtomicBoolean utteranceContainedInCurrentIntent = new AtomicBoolean(false);
        // look for this utterance in other intents
        intentsAndUtterances.forEach((intentName, utterances) -> {
            if (utterances.stream().anyMatch(u -> StringUtils.equalsIgnoreCase(u, text))) {
                if (StringUtils.equalsIgnoreCase(lastIntent, intentName)) {
                    utteranceContainedInCurrentIntent.set(true);
                } else {
                    utteranceContainedInAnotherIntent.set(true);
                }
            }
        });

        Validate.isTrue(StringUtils.isBlank(text) || !utteranceContainedInAnotherIntent.get(), "The utterance '" + text + "' of intent '" + lastIntent + "' is already part of another intent.");

        if (!utteranceContainedInCurrentIntent.get()) {
            intentsAndUtterances.get(lastIntent).add(text);
        }
    }

    private static List<Pair<String, List<String>>> getPlaceholders(final Map<String, String> placeholders) {
        final List<Pair<String, List<String>>> placeholderWithValues = new ArrayList<>();

        placeholders.forEach((placeholderName, placeholderOrigin) -> {
            if (!placeholderValues.containsKey(placeholderName)) {
                placeholderValues.put(placeholderName, ResourceReader.getPlaceholderValueList(placeholderOrigin).orElse(Resolver.resolveSlotValue(placeholderOrigin)));
            }
            placeholderWithValues.add(new ImmutablePair<>(placeholderName, placeholderValues.get(placeholderName)));
        });
        return placeholderWithValues;
    }
}
