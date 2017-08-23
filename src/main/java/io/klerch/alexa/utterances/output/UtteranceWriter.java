package io.klerch.alexa.utterances.output;

public interface UtteranceWriter {
    void write(final String utterance);
    void close();
}
