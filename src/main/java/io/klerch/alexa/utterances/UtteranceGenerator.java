package io.klerch.alexa.utterances;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtteranceGenerator {
    // Set this variable to the name of the file with utterances you created in the utterances-folder!
    private final static String utteranceFileKey = "booking";

    // stores the list of all generated utterances to avoid duplicates
    private static List<String> utterances = new ArrayList<>();
    // stores the list of values contained in placeholders of utterances
    private static final Map<String, List<String>> placeholderValues = new HashMap<>();

    public static void main(final String [] args) {
        // get file-key from console
        generateUtterances(Arrays.stream(args).findFirst().orElse(utteranceFileKey));

        utterances.stream().sorted().forEach(System.out::println);
    }

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
            printOut(utterance.trim().replaceAll("\\s+"," "));
            return;
        }

        for(int i = 0; i < lists.get(depth).getValue().size(); ++i) {
            final Pair<String, List<String>> placeholder = lists.get(depth);
            final String placeholderValue = placeholder.getValue().get(i);
            final String placeholderName = placeholder.getKey();
            generatePermutations(lists, depth + 1, utterance.replace(placeholderName, placeholderValue));
        }
    }

    private static void printOut(final String utterance) {
        if (!utterances.contains(utterance)) {
            utterances.add(utterance);
            //System.out.println(utterance);
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
