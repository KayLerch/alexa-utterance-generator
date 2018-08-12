package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.model.Generation;

/**
 * Abstract OutputWriter. OutputWriters implement logic on how to print generator output
 */
abstract class AbstractOutputWriter implements OutputWriter {
    boolean verbose = false;

    /**
     * Sets verbosity of the printer
     * @param verbose true for verbosity
     * @return output writer
     */
    public OutputWriter beVerbose(final boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * Prints output of generator
     * @param output formatted output string
     * @param generation result set object
     */
    @Override
    public abstract void print(final String output, final Generation generation);
}
