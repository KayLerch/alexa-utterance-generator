package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Intent {
    @JsonIgnore
    private static List<String> slotSuffix = Arrays.asList("A","B","C","D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y");

    @JsonProperty
    private final String name;
    @JsonProperty
    private final List<String> samples = new ArrayList<>();
    @JsonProperty
    private final List<Slot> slots = new ArrayList<>();

    public Intent(final String name, final String sampleUtterance) {
        this.name = name;
        addSample(sampleUtterance);
    }

    public String getName() {
        return name;
    }

    public void addSample(final String sample) {
        final List<String> slotsInUtterance2 = new ArrayList<>();

        final StringBuffer buffer = new StringBuffer();
        final Matcher slotsInUtterance = Pattern.compile("\\{(.*?)\\}").matcher(sample);

        while (slotsInUtterance.find()) {
                final String slotType = slotsInUtterance.group(1);
                final String slotNameNormalized = slotType.replaceAll("[^a-zA-Z\\s]", "_");
                // remove special chars and numbers (for e.g. builtin-slot types)
                String slotName = slotNameNormalized;
                int i = 0;
                // within one sample-utterance slots must not have equal names
                while (slotsInUtterance2.contains(slotName)) {
                    slotName = slotNameNormalized + "_" + slotSuffix.get(i++);
                }
                final String slotNameFinal = slotName;
                // keep track of what slot-names are already taken in this sample-utterance
                slotsInUtterance2.add(slotNameFinal);

                // add slot to reference if not already existent in this intent
                if (slots.stream().noneMatch(s -> s.getName().equals(slotNameFinal))) {
                    slots.add(new Slot(slotNameFinal, slotType));

                }
                slotsInUtterance.appendReplacement(buffer, Matcher.quoteReplacement("{" + slotNameFinal + "}"));
            }
            slotsInUtterance.appendTail(buffer);

            final String sampleUtterance = buffer.toString();

            if (StringUtils.isNotBlank(sampleUtterance)) {
                samples.add(sampleUtterance);
            }

    }
}
