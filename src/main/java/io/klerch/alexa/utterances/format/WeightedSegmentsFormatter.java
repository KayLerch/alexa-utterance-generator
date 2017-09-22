package io.klerch.alexa.utterances.format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeightedSegmentsFormatter implements Formatter {
    private final static String DEFAULT_SEPARATOR = ";";
    private final int weight;
    private final String valueSeparator;
    private final boolean prependPriority;
    private final List<String> samples = new ArrayList<>();

    public WeightedSegmentsFormatter(final int weight) {
        this(weight, false);
    }

    public WeightedSegmentsFormatter(final int weight, final boolean prependPriority) {
        this(weight, prependPriority, DEFAULT_SEPARATOR);
    }

    public WeightedSegmentsFormatter(final int weight, final boolean prependPriority, final String valueSeparator) {
        this.weight = weight;
        this.valueSeparator = valueSeparator;
        this.prependPriority = prependPriority;
    }

    @Override
    public void before() { }

    @Override
    public boolean write(final String sample) {
        int weightRest = weight;
        int weightMax = 0;
        final StringBuffer buffer = new StringBuffer();
        // extract all the slots found in the utterance
        final Matcher placeholdersInUtterance = Pattern.compile("\\[(-?\\d+)]").matcher(sample);
        // for any of the placeholder ...
        while (placeholdersInUtterance.find()) {
            final int weighted = Integer.parseInt(placeholdersInUtterance.group(1));
            weightRest = weightRest - weighted;
            weightMax = Math.max(weightMax, weighted);
            placeholdersInUtterance.appendReplacement(buffer, Matcher.quoteReplacement(""));
        }
        placeholdersInUtterance.appendTail(buffer);

        if (weightRest >= 0) {
            final String finalSample =
                    (prependPriority ? "P" + weightMax + valueSeparator : "") +
                            buffer.toString().trim().replaceAll("\\s+"," ");
            this.samples.add(finalSample);
            return true;
        }
        return false;
    }

    @Override
    public String getFormat() {
        return "csv";
    }

    @Override
    public String generateSchema() {
        final StringBuilder sb = new StringBuilder();
        final AtomicBoolean isFirst = new AtomicBoolean(true);

        this.samples.stream().sorted().forEachOrdered(sample -> {
            sb.append(isFirst.compareAndSet(true, false) ? "" : "\n").append(sample);
        });

        return sb.toString();
    }
}
