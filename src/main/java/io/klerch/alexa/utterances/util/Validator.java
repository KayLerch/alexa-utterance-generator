package io.klerch.alexa.utterances.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.regex.Pattern;

/**
 * Validates naming conventions
 */
public class Validator {
    /**
     * regex patterns covering the naming convention for intents in ASK
     */
    private static final Pattern intentNamePattern = Pattern.compile("(AMAZON.)*([A-Z_]+)", Pattern.CASE_INSENSITIVE);
    /**
     * regex patterns covering the naming convention for slots in ASK
     */
    private static final Pattern slotNamePattern = Pattern.compile("(AMAZON.)*([A-Z_]+)", Pattern.CASE_INSENSITIVE);
    /**
     * regex patterns covering the naming convention for invocation names in ASK (not working, not used at the moment)
     */
    private static final Pattern invocationNamePattern = Pattern.compile("^[\\pL]+$", Pattern.UNICODE_CASE);

    /**
     * Validates intent name against ASK conventions. Throws an exception in case the convention is not met
     * @param intentName name of an intent
     */
    public static void validateIntentName(final String intentName) {
        // look for reserved intent keyword "invocation" which indicates invocation name definition
        if (StringUtils.equalsIgnoreCase("invocation", intentName)) {
            // validate invocation name requirements
            validateInvocationName(intentName);
        } else {
            // validate intent name requirements
            Validate.isTrue(intentNamePattern.matcher(intentName).matches(), "Your intent " + intentName + " does not meet intent name conventions. The name of an intent can only contain case-insensitive alphabetical characters and underscores.");
        }
    }

    /**
     * Validates slot name against ASK conventions. Throws an exception in case the convention is not met
     * @param slotName name of a slot
     */
    public static void validateSlotName(final String slotName) {
        // validate slot name requirements
        Validate.isTrue(slotNamePattern.matcher(slotName).matches(), "Your slot type " + slotName + " does not meet the naming conventions. The name of a slot type can only contain case-insensitive alphabetical characters and underscores.");
    }

    /**
     * Validates invocation name against ASK conventions. Throws an exception in case the convention is not met
     * @param invocationName invocation name
     */
    public static void validateInvocationName(final String invocationName) {
        // not working for now, need to consider all special characters in languages like German and French
        //Validate.isTrue(invocationNamePattern.matcher(invocationName).matches(), "Your invocation name " + invocationName + " does not meet the requirements. The name can only contain case-insensitive alphabetical characters and spaces.");
    }

    /**
     * Validates sample utterance against ASK conventions. Throws an exception in case the convention is not met
     * @param utterance sample utterance
     */
    public static void validateSampleUtterance(final String utterance) {
        // no specific rules for now
    }
}
