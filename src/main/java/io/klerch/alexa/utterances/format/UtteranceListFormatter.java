package io.klerch.alexa.utterances.format;

public class UtteranceListFormatter implements Formatter {
    private StringBuilder sb;

    @Override
    public void before() {
        sb = new StringBuilder();
    }

    @Override
    public void write(final String sample) {
        sb.append(sample).append("\n");
    }

    @Override
    public String getFormat() {
        return "txt";
    }

    @Override
    public String generateSchema() {
        return sb.toString();
    }
}
