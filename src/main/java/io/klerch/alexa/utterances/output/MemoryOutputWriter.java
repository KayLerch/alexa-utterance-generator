package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.model.Generation;

/**
 * Prints output of generator to memory
 */
public class MemoryOutputWriter extends AbstractOutputWriter {
    private Generation generation;
    private String output;

    /**
     * Prints output of generator to memory
     * @param output formatted output string
     * @param generation result set object
     */
    @Override
    public void print(final String output, final Generation generation) {
        this.generation = generation;
        this.output = output;
    }

    /**
     * Returns the output stored in memory
     * @return result set of schema generation
     */
    public Generation getGeneration() {
        return generation;
    }

    /**
     * Returns the output string stored in memory
     * @return formatted output string
     */
    public String getOutput() {
        return output;
    }
}
