package io.klerch.alexa.utterances.format;

public interface Formatter {
    void before();
    void write(final String sample);
    String getFormat();
    String generateSchema();
}
