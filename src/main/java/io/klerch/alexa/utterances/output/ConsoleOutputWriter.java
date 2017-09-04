package io.klerch.alexa.utterances.output;

public class ConsoleOutputWriter extends AbstractOutputWriter {
    @Override
    public void print() {
        System.out.println(this.formatter.generateSchema());
        System.out.println("Generated " + this.numOfSamples + " utterances.");
    }
}
