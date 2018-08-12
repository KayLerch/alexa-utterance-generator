package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.model.Generation;

/**
 * Prints output of generator to console
 */
public class ConsoleOutputWriter extends AbstractOutputWriter {
    /**
     * Prints output of generator to console
     * @param output formatted output string
     * @param generation result set object
     */
    @Override
    public void print(final String output, final Generation generation) {
        if (verbose || !(this instanceof FileOutputWriter)) {
            System.out.println(output);
            System.out.println("--------------");
        }
        System.out.println(String.format("Output generated (%.2f KB)", output.getBytes().length / 1024.00));
        System.out.println("Created " + generation.getNumberOfUtterances() + " utterances with " + generation.getNumberOfSlots() + " slots in " + generation.getNumberOfIntents() + " intents.");
        System.out.println("Created " + generation.getNumberOfSlotTypes() + " slot types with " + generation.getNumberOfSlotValues() + " values (" + generation.getNumberOfSlotValuesWithSynonyms() + " including synonyms).");
    }
}
