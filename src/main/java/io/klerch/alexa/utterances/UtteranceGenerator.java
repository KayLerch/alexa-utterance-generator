package io.klerch.alexa.utterances;

import io.klerch.alexa.utterances.output.FileUtteranceWriter;
import io.klerch.alexa.utterances.output.UtteranceWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtteranceGenerator {
    // 1) Set the key of a file with utterances you created in the utterances-folder
    private final static String utteranceFileKey = "booking"; // e.g. "booking" for using "/resources/output/utterances/booking.txt"
    // 2) set true if the first word per line is the name of an intent. it will be considered when looking for duplicates
    private static final boolean FIRST_WORD_IS_INTENT_NAME = false;
    // 3) choose one of  the utterance writers to set the output strategy
    private static final UtteranceWriter utteranceWriter = new FileUtteranceWriter(utteranceFileKey);
    // 4) run and done
    public static void main(final String [] args) {
        generateUtterances(Arrays.stream(args).findFirst().orElse(utteranceFileKey));
        utteranceWriter.beforeWrite();
        try {
            utterances.values().stream().sorted(String::compareToIgnoreCase).forEach(utteranceWriter::write);
        } finally {
            utteranceWriter.afterWrite();
        }
    }

    // stores the list of all generated utterances to avoid duplicates
    private static Map<String, String> utterances = new HashMap<>();
    // stores the list of values contained in placeholders of utterances
    private static final Map<String, List<String>> placeholderValues = new HashMap<>();

    private static void generateUtterances(final String utteranceResourceId) {
        getUtteranceList(utteranceResourceId).forEach(utterance -> {
            final Map<String, String> placeholders = new HashMap<>();
            final StringBuffer buffer = new StringBuffer();
            // extract all the placeholders found in the utterance
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
                placeholderValues.put(placeholderName, getPlaceholderValueList(placeholderOrigin).orElse(resolvePlaceholderValue(placeholderOrigin)));
            }
            placeholderWithValues.add(new ImmutablePair<>(placeholderName, placeholderValues.get(placeholderName)));
        });
        return placeholderWithValues;
    }

    private static List<String> resolvePlaceholderValue(final String placeHolderName) {
        final List<String> values = new ArrayList<>();

        Arrays.stream(placeHolderName.split("\\|")).forEach(value -> {
            final String[] ranges = value.split("-");
            if (ranges.length == 2 && NumberUtils.isParsable(ranges[0]) && NumberUtils.isParsable(ranges[1])) {
                final int r1 = Integer.parseInt(ranges[0]);
                final int r2 = Integer.parseInt(ranges[1]);
                final int min = Integer.min(r1, r2);
                final int max = Integer.max(r1, r2);

                for (Integer i = min; i <= max; i++) {
                    values.add(i.toString());
                }
            } else {
                values.add(value);
            }
        });
        return values;
    }

    private static Optional<List<String>> getPlaceholderValueList(final String valueResource) {
        final List<String> lines = getList(String.format("/placeholders/%s.txt", valueResource));
        return lines.isEmpty() ? Optional.empty() : Optional.of(lines);
    }

    private static List<String> getUtteranceList(final String utteranceResource) {
        return getList(String.format("/utterances/%s.txt", utteranceResource));
    }

    private static List<String> getList(final String fileName) {
        final List<String> lines = new ArrayList<>();

        Optional.ofNullable(UtteranceGenerator.class.getResource(fileName)).ifPresent(url -> {
            final File file = new File(url.getFile());

            try (final Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }
                scanner.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
        // eliminate empty lines
        lines.removeIf(StringUtils::isBlank);
        return lines;
    }
}
