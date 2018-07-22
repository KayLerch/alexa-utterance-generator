package io.klerch.alexa.utterances.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonRootName("languageModel")
public class SkillBuilderModel {
    @JsonIgnore
    private static String DEFAULT_INTENT_NAME = "MyIntent";
    @JsonProperty
    private final List<Intent> intents = new ArrayList<>();
    @JsonProperty
    private final List<SlotType> types = new ArrayList<>();
    @JsonProperty
    private final String invocationName;

    public SkillBuilderModel(final String invocationName) {
        this.invocationName = StringUtils.lowerCase(invocationName);
    }

    public void addSample(final String sample) {
        final String[] words = sample.split(" ");
        final String intentName = words.length < 1 || StringUtils.isBlank(words[0]) ? DEFAULT_INTENT_NAME : words[0];
        final String sampleUtterance = words.length > 1 ? sample.substring(sample.indexOf(" ") + 1).trim() : "";

        // skip blank utterances except for builtin-intents that can exist without samples
        if (StringUtils.isBlank(sampleUtterance) && !StringUtils.startsWithIgnoreCase(intentName, "AMAZON.")) return;

        final Optional<Intent> intent = intents.stream().filter(i -> i.getName().equals(intentName)).findFirst();
        if (intent.isPresent()) {
            intent.get().addSample(sampleUtterance);
        } else {
            intents.add(new Intent(intentName, sampleUtterance));
        }

        final Matcher slotsInUtterance = Pattern.compile("\\{(.*?)\\}").matcher(sampleUtterance);
        // for any of the placeholder ...
        while (slotsInUtterance.find()) {
            // split by custom slot name and slot type reference
            final String[] slot = slotsInUtterance.group(1).split(":");
            // if no slot name given then type reference is equal to slot name
            final String slotRef = slot.length > 1 ? slot[1] : slot[0];
            final String slotName = slot[0];
            // do not add slot type definition for builtins and any types already added and used in previous utterances
            if (!StringUtils.startsWithIgnoreCase(slotRef,"AMAZON.") && types.stream().noneMatch(t -> t.getName().equals(slotName))) {
                types.add(new SlotType(slotName, slotRef));
            }
        }
    }
}
