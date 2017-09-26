package io.klerch.alexa.utterances.format;

import org.apache.commons.lang3.StringUtils;

public class UtteranceListFormatter implements Formatter {
    private StringBuilder sb;
    private int numOfSamples = 0;

    @Override
    public void before() {
        sb = new StringBuilder();
    }

    @Override
    public boolean write(final String sample) {
        if (!StringUtils.startsWithIgnoreCase(sample, "AMAZON.") || sample.split(" ").length > 1) {
            sb.append(numOfSamples++ > 0 ? "\n" : "").append(sample);
            return true;
        }
        return false;
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
