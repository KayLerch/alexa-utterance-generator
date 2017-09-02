package io.klerch.alexa.utterances.output;

public interface UtteranceWriter {
    void beforeWrite();
    void write(final String utterance);
    void afterWrite();
}
