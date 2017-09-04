package io.klerch.alexa.utterances;

import io.klerch.alexa.utterances.format.UtteranceListFormatter;
import io.klerch.alexa.utterances.output.FileOutputWriter;
import io.klerch.alexa.utterances.output.OutputWriter;
import io.klerch.alexa.utterances.format.InteractionModelFormatter;
import io.klerch.alexa.utterances.format.Formatter;
import io.klerch.alexa.utterances.util.Resolver;
import io.klerch.alexa.utterances.util.ResourceReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtteranceGenerator {
    // 1) Set the key of a file with utterances you created in the utterances-folder
    private final static String utteranceFileKey = "booking"; // e.g. "booking" for using "/resources/output/utterances/booking.grammar"
    // 2) choose one of  the output writers
    private static final OutputWriter OUTPUT_WRITER = new FileOutputWriter(utteranceFileKey);
    // 3) choose formatter
    private static final Formatter FORMATTER = new UtteranceListFormatter();
    // 4) run and done
    public static void main(final String [] args) {
        generateUtterances(Arrays.stream(args).findFirst().orElse(utteranceFileKey));
        OUTPUT_WRITER.beforeWrite(FORMATTER);
        try {
            utterances.values().stream().sorted(String::compareToIgnoreCase).forEach(OUTPUT_WRITER::write);
        } finally {
            OUTPUT_WRITER.print();
        }
    }

    // stores the list of all generated utterances to avoid duplicates
    private static Map<String, String> utterances = new HashMap<>();
    // stores the list of values contained in slots of utterances
    private static final Map<String, List<String>> placeholderValues = new HashMap<>();
    // set true if the first word per line is the name of an intent. it will be considered when looking for duplicates
    private static final boolean FIRST_WORD_IS_INTENT_NAME = true;

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

    private static void store(final String utterance) {
        final String utteranceKey = (FIRST_WORD_IS_INTENT_NAME ? utterance.substring(utterance.indexOf(" ") + 1) : utterance).toLowerCase();
        if (!utterances.containsKey(utteranceKey)) {
            utterances.put(utteranceKey, utterance);
            //System.out.println(utterance);
        } else {
            final String intent = utterances.get(utteranceKey).split(" ")[0];
            final String intentCurrent = utterance.split(" ")[0];
            // duplicated utterances across different intents are not accepted and must be avoided
            Validate.isTrue(!FIRST_WORD_IS_INTENT_NAME || StringUtils.equalsIgnoreCase(intent, intentCurrent), "The utterance '" + utteranceKey + "' of intent '" + intentCurrent + "' already exists for intent '" + intent + "'.");
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
