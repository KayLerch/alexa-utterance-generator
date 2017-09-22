package io.klerch.alexa.utterances.format;

public interface Formatter {
    void before();
    boolean write(final String sample);
    String getFormat();
    String generateSchema();
}
