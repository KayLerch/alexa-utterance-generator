package io.klerch.alexa.utterances.formatter;

import io.klerch.alexa.utterances.model.Generation;

public interface Formatter {
    void print(final Generation generation);
}
