package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.format.Formatter;

abstract class AbstractOutputWriter implements OutputWriter {
    Formatter formatter;
    Integer numOfSamples = 0;

    @Override
    public void beforeWrite(Formatter formatter) {
        // set formatter and do some initial prep
        this.formatter = formatter;
        this.formatter.before();
    }

    @Override
    public void write(String utterance) {
        // let formatter write the sample
        if (this.formatter.write(utterance)) {
            numOfSamples++;
        }
    }

    @Override
    public abstract void print();
}
