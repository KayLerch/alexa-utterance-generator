package io.klerch.alexa.utterances.console;

import io.klerch.alexa.utterances.formatter.PlainFormatter;
import io.klerch.alexa.utterances.formatter.Formatter;
import io.klerch.alexa.utterances.formatter.JsonFormatter;
import io.klerch.alexa.utterances.model.Generation;
import io.klerch.alexa.utterances.output.ConsoleOutputWriter;
import io.klerch.alexa.utterances.output.FileOutputWriter;
import io.klerch.alexa.utterances.output.OutputWriter;
import io.klerch.alexa.utterances.processor.Generator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Console application. This is the entry point for the JAR.
 */
@CommandLine.Command(name = "alexa-generate.jar", mixinStandardHelpOptions = true, version= { "----------------------", "Alexa Schema Generator v2.0.0", "Kay Lerch (2018)", "https://github.com/KayLerch/alexa-utterance-generator", "----------------------" })
public class Console implements Runnable {
    /**
     * File reference for grammar specification. Mandatory.
     */
    @CommandLine.Parameters(arity = "0..1", index = "0", paramLabel = "FILE.grammar", description = "Path to your grammar file.")
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

    /**
     * Prints output in plain format rather than printing as JSON schema
     */
    @CommandLine.Option(names = { "-r", "--repl" }, description = "Write down grammar specification line by line in your console. Type 'generate!' to generate the schema.")
    private boolean repl = false;

    @Override
    public void run() {
        final Path grammarAbsoluteFilePath = grammarFile != null ? grammarFile.toPath().toAbsolutePath().getParent() : Paths.get("").toAbsolutePath();
        final Path valuesAbsoluteFilePath = (valuesFilePath != null) ? valuesFilePath.toAbsolutePath() : grammarAbsoluteFilePath;
        final OutputWriter outputWriter = !dryRun && !repl ?
                (outputFile != null ? new FileOutputWriter(outputFile) : new FileOutputWriter(grammarAbsoluteFilePath)) : new ConsoleOutputWriter();

        final Formatter formatter = !plainFormat ?
                JsonFormatter.create(outputWriter).build() :
                PlainFormatter.create(outputWriter)
                        .displayIntent(PlainFormatter.INTENT_DISPLAY_OPTION.ONCE_ON_TOP)
                        .displaySlotType(PlainFormatter.SLOT_TYPE_DISPLAY_OPTION.ID_AND_VALUES)
                        .displayInvocationName(true)
                        .build();

        if (repl) {
            Validate.isTrue(grammarFile == null, "Input grammar file references are not allowed in REPL input mode.");
            System.out.println("----------------------");
            System.out.println("Start writing down grammar specification line by line. Type 'generate!' to generate the schema.");
            System.out.println("----------------------");
            final List<String> lines = new ArrayList<>();
            Integer lineNumber = 1;
            do {
                System.out.print(String.format("%02d:> ", lineNumber++));
                lines.add(System.console().readLine());
            } while (!StringUtils.equalsIgnoreCase("generate!", lines.get(lines.size() - 1)));

            final Generation generation = Generator.create()
                    .withFormatter(formatter)
                    .withValuesFilePath(valuesAbsoluteFilePath)
                    .build()
                    .generate(lines.subList(0, Math.max(0, lines.size() - 1)));

            System.out.print("Do you want to save the output as JSON schema to your file system? (Y/n): ");
            if (System.console().readLine().equals("Y")) {
                final OutputWriter outputWriter1 = (outputFile != null ? new FileOutputWriter(outputFile) : new FileOutputWriter(grammarAbsoluteFilePath));
                JsonFormatter.create(outputWriter1).build().print(generation);
            }
        }
        else {
            Validate.isTrue(grammarFile != null, "Input grammar file required. Call -h, --help to get more details.");
            Validate.isTrue(grammarFile.exists(), "Grammar file does not exist at " + grammarFile.getAbsolutePath());
            Validate.isTrue(grammarFile.canRead(), "Grammar file cannot be read at " + grammarFile.getAbsolutePath() + " due to insufficient permissions.");

            Generator.create()
                    .withGrammarFile(grammarFile)
                    .withFormatter(formatter)
                    .withValuesFilePath(valuesAbsoluteFilePath)
                    .build()
                    .generate();
        }
    }

    /**
     * Entrance
     * @param args commandline arguments. Set -help flag to learn more.
     */
    public static void main(final String [] args) {
        CommandLine.run(new Console(), System.out, (args.length == 0) ? new String[]{ "--help" } : args);
    }
}
