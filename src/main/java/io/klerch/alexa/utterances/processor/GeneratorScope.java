package io.klerch.alexa.utterances.processor;

/**
 * Generator will break down grammar specification into individual entities called scopes
 */
public enum GeneratorScope {
    /**
     * Representing an intent specification (e.g. MyIntent: sample utterance)
     */
    INTENT,
    /**
     * Representing an invocation name specification (e.g. Invocation: my skill invocation name)
     */
    INVOCATION,
    /**
     * Representing a placeholder specification within a sample utterance (e.g. {{placeholder}})
     */
    PLACEHOLDER,
    /**
     * Representing variant phrase specification within a sample utterance (e.g. {text1|text2|text3})
     */
    VARIANT_PHRASES
}
