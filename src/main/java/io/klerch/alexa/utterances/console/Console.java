package io.klerch.alexa.utterances.console;

import io.klerch.alexa.utterances.formatter.PlainFormatter;
import io.klerch.alexa.utterances.formatter.Formatter;
import io.klerch.alexa.utterances.formatter.JsonFormatter;
import io.klerch.alexa.utterances.output.ConsoleOutputWriter;
import io.klerch.alexa.utterances.output.FileOutputWriter;
import io.klerch.alexa.utterances.output.OutputWriter;
import io.klerch.alexa.utterances.processor.Generator;
import org.apache.commons.lang3.Validate;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;

/**
 * Console application. This is the entry point for the JAR.
 */
@CommandLine.Command(name = "alexa-generate.jar", mixinStandardHelpOptions = true)
public class Console implements Runnable {
    /**
     * File reference for grammar specification. Mandatory.
     */
    @CommandLine.Parameters(arity = "1", index = "0", paramLabel = "FILE.grammar", description = "Path to your grammar file.")
    private File grammarFile;

    /**
     * File reference for JSON output. Optional. If not set it will save the output to the grammarFile location and file name is generated from skill invocation name plus timestamp
     */
    @CommandLine.Parameters(arity = "0..1", index = "1", paramLabel = "OUTPUT.json", description = "Optionally, name a file for saving the generated schema. Existing files will be overwritten. If no output file is specified the schema will be written to the same folder the grammar file is in. Don't forget to enable writing to file with the -w flag.")
    private File outputFile;

    /**
     * Path reference for values specification. Optional. If not set it will look up values files in the grammarFile location
     */
    @CommandLine.Option(names = { "-v", "--values" }, paramLabel = "PATH/to/*.values", description = "Point to  They hold values filling up the utterance placeholders in your grammar file.")
    private Path valuesFilePath;

    /**
     * Prints JSON schema to console rather than writing it to file
     */
    @CommandLine.Option(names = { "-d", "--dry-run" }, description = "Writes schema to console only. No file will be created.")
    private boolean dryRun = false;

    /**
     * Prints output in plain format rather than printing as JSON schema
     */
    @CommandLine.Option(names = { "-p", "--plain" }, description = "Writes plain output instead of JSON schema. Good for reviewing output.")
    private boolean plainFormat = false;

    @Override
    public void run() {
        Validate.isTrue(grammarFile.exists(), "Grammar file does not exist at " + grammarFile.getAbsolutePath());
        Validate.isTrue(grammarFile.canRead(), "Grammar file cannot be read at " + grammarFile.getAbsolutePath() + " due to insufficient permissions.");

        final Path grammarAbsoluteFilePath = grammarFile.toPath().toAbsolutePath().getParent();
        final Path valuesAbsoluteFilePath = (valuesFilePath != null) ? valuesFilePath.toAbsolutePath() : grammarAbsoluteFilePath;
        final OutputWriter outputWriter = !dryRun ?
                (outputFile != null ? new FileOutputWriter(outputFile) : new FileOutputWriter(grammarAbsoluteFilePath)) : new ConsoleOutputWriter();

        final Formatter formatter = !plainFormat ?
                JsonFormatter.create(outputWriter).build() :
                PlainFormatter.create(outputWriter)
                        .displayIntent(PlainFormatter.INTENT_DISPLAY_OPTION.ONCE_ON_TOP)
                        .displaySlotType(PlainFormatter.SLOT_TYPE_DISPLAY_OPTION.ID_AND_VALUES)
                        .displayInvocationName(true)
                        .build();

        Generator.create()
                .withGrammarFile(grammarFile)
                .withFormatter(formatter)
                .withValuesFilePath(valuesAbsoluteFilePath)

                .build().generate();
    }

    /**
     * Entrance
     * @param args commandline arguments. Set -help flag to learn more.
     */
    public static void main(final String [] args) {
        CommandLine.run(new Console(), System.out, args);
    }
}
