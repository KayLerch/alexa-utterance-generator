package io.klerch.alexa.utterances.output;


import io.klerch.alexa.utterances.model.Generation;

/**
 * Interface specifying OutputWriter. OutputWriters implement logic on how to print generator output
 */
public interface OutputWriter {
    /**
     * Sets verbosity of the printer
     * @param verbose true for verbosity
     * @return output writer
     */
    OutputWriter beVerbose(final boolean verbose);

    /**
     * Prints output of generator
     * @param output formatted output string
     * @param generation result set object
     */
    void print(final String output, final Generation generation);
}
