package io.klerch.alexa.utterances.format;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeightedSegmentsFormatter implements Formatter {
    private int weight;
    private String valueSeparator = ";";
    private boolean prependPriority = false;
    private final List<String> samples = new ArrayList<>();

    public WeightedSegmentsFormatter(final String[] args) {
        if (args != null) {
            prependPriority = ArrayUtils.contains(args, "-pp") || ArrayUtils.contains(args, "-prependPriority");

            int index1 = ArrayUtils.indexOf(args, "-weight");
            if (index1 < args.length - 1) {
                final String weightString = args[index1 + 1];
                Validate.isTrue(NumberUtils.isParsable(weightString), "Please provide a numeric value for weight.");
                this.weight = Integer.parseInt(weightString);
            }

            int index2 = ArrayUtils.indexOf(args, "-separator");
            if (index2 < args.length - 1) {
                valueSeparator = args[index2 + 1];
            }
        }
    }

    public WeightedSegmentsFormatter(final int weight) {
        this(new String[] { "weight", String.valueOf(weight)});
    }

    public WeightedSegmentsFormatter(final int weight, final boolean prependPriority) {
        this(new String[] { "weight", String.valueOf(weight), prependPriority ? "-pp" : ""});
    }

    public WeightedSegmentsFormatter(final int weight, final boolean prependPriority, final String valueSeparator) {
        this(new String[] { "weight", String.valueOf(weight), prependPriority ? "-pp" : "", "-separator", valueSeparator});
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
