package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.format.Formatter;

public interface OutputWriter {
    void beforeWrite(final Formatter formatter);
    void write(final String utterance);
    void print();
}
