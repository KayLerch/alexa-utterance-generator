package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonInclude
public class InteractionModel {
    @JsonProperty
    private final List<Intent> intents = new ArrayList<>();
    @JsonProperty
    private final List<SlotType> types = new ArrayList<>();

    public void addSample(final String sample) {
        final String[] words = sample.split(" ");
        final String intentName = words[0];
        final String sampleUtterance = words.length > 1 ? sample.substring(sample.indexOf(" ") + 1).trim() : "";

        final Optional<Intent> intent = intents.stream().filter(i -> i.getName().equals(intentName)).findFirst();
        if (intent.isPresent()) {
            intent.get().addSample(sampleUtterance);
        } else {
            intents.add(new Intent(intentName, sampleUtterance));
        }

        final Matcher slotsInUtterance = Pattern.compile("\\{(.*?)\\}").matcher(sampleUtterance);
        // for any of the placeholder ...
        while (slotsInUtterance.find()) {
            final String slotName = slotsInUtterance.group(1);
            if (!StringUtils.startsWithIgnoreCase(slotName,"AMAZON.") && types.stream().noneMatch(t -> t.getName().equals(slotName))) {
                types.add(new SlotType(slotName));
            }
        }
    }
}
