package io.klerch.alexa.utterances;

import io.klerch.alexa.utterances.formatter.PlainFormatter;
import io.klerch.alexa.utterances.formatter.Formatter;
import io.klerch.alexa.utterances.formatter.JsonFormatter;
import io.klerch.alexa.utterances.output.ConsoleOutputWriter;
import io.klerch.alexa.utterances.output.FileOutputWriter;
import io.klerch.alexa.utterances.output.OutputWriter;
import io.klerch.alexa.utterances.processor.Generator;

import java.nio.file.Paths;

/**
 * Entry point for running Generator in an IDE
 * Demonstrates use of Generator in Jave code as a reference.
 */
public class UtteranceGenerator {
    /**
     * Set the key of a file with utterances you created in the utterances-folder
     * e.g. "booking" for using "/resources/output/utterances/booking.grammar"
     */
    private final static String GRAMMAR_FILE_KEY_IN_UTTERANCES_FOLDER = "booking";

    /**
     * Set true in order to write to file. Set to false to just print out to console
     */
    private final static Boolean WRITE_TO_FILE = true;

    /**
     * Set true to get output as an Alexa skill JSON schema. Set to false to receive plain output for validation purpose
     */
    private final static Boolean PRINT_AS_SCHEMA = true;

    /**
     * Set true to cleanup output (e.g. sorting an deduplication of utterances and slots)
     */
    private final static Boolean CLEANUP_OUTPUT = true;

    /**
     * Set true to validate output (e.g. throws exception on duplicate utterances in two or more intents)
     */
    private final static Boolean VALIDATE_OUTPUT = false;

    /**
     * Run in your Java IDE
     * @param args list of arguments (not required nor used when running in the IDE)
     */
    public static void main(final String [] args) {
        // first init an output writer setting the persistence strategy (where to print)
        final OutputWriter outputWriter = WRITE_TO_FILE ?
                new FileOutputWriter(Paths.get(output_folder)).beVerbose(true) :
                new ConsoleOutputWriter();

        // secondly, init formatter setting the formatting strategy (how to print)
        final Formatter formatter = PRINT_AS_SCHEMA ?
                JsonFormatter.create(outputWriter).build() :
                PlainFormatter.create(outputWriter)
                        .displayIntent(PlainFormatter.INTENT_DISPLAY_OPTION.ONCE_ON_TOP)
                        .displaySlotType(PlainFormatter.SLOT_TYPE_DISPLAY_OPTION.ID_AND_VALUES)
                        .displayInvocationName(true)
                        .build();

        // finally, init the generator with reference to grammar and values input and formatter
        final Generator generator = Generator.create()
                .withGrammarFile(Paths.get(utterances_folder).resolve(GRAMMAR_FILE_KEY_IN_UTTERANCES_FOLDER + ".grammar").toFile())
                .withFormatter(formatter)
                .withValuesFilePath(Paths.get(slots_folder))
                .disableCleanup(!CLEANUP_OUTPUT)
                .disableValidation(!VALIDATE_OUTPUT)
                .build();

        // generate will create the output and hands it over to formatter and output writer to print it out
        generator.generate();
    }

    private static final String utterances_folder = "src/main/resources/utterances";
    private static final String slots_folder = "src/main/resources/slots";
    private static final String output_folder = "src/main/resources/output";
}
