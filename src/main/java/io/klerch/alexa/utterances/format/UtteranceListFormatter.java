package io.klerch.alexa.utterances.format;

public class UtteranceListFormatter implements Formatter {
    private StringBuilder sb;
    private int numOfSamples = 0;

    @Override
    public void before() {
        sb = new StringBuilder();
    }

    @Override
    public boolean write(final String sample) {
        sb.append(numOfSamples++ > 0 ? "\n" : "").append(sample);
        return true;
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
