package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.model.Generation;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;

/**
 * Prints output of generator to file
 */
public class FileOutputWriter extends ConsoleOutputWriter {
    private BufferedWriter outputWriter;
    private FileOutputStream outputStream;
    private Path destinationPath;
    private File destinationFile;

    /**
     * Creates a new FileOutputWriter
     * @param destinationPath destination to write JSON file to. Filename gets generated from invocation name and timestamp
     */
    public FileOutputWriter(final Path destinationPath) {
        this.destinationPath = destinationPath;
    }

    /**
     * Creates a new FileOutputWriter
     * @param destinationFile file reference for saving the JSON output to. If file does not exist it will be created. If it exists it will be overwritten.
     */
    public FileOutputWriter(final File destinationFile) {
        this.destinationFile = destinationFile;
    }

    /**
     * Prints output of generator to file. If file does not exist it will be created. If it exists it will be overwritten.
     * @param output formatted output string
     * @param generation result set object
     */
    @Override
    public void print(final String output, final Generation generation) {
        if (destinationFile == null) {
            final String fileEnding = output.startsWith("{") ? ".json" : ".txt";
            final String fileName = new Date().getTime() + "_" + generation.getModel().getInvocationName().replaceAll("\\s+", "_") + fileEnding;
            destinationFile = new File(destinationPath.resolve(fileName).toUri().getPath());
        }

        if (!destinationFile.exists()) {
            try {
                Validate.isTrue(destinationFile.createNewFile(), "Could not create output file '" + destinationFile.getAbsolutePath() + "'.");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        Validate.isTrue(destinationFile.canWrite(), "Cannot write to file '" + destinationFile.getAbsolutePath() + "'. Permissions?");

        try {
            this.outputStream = new FileOutputStream(destinationFile, false);
            this.outputWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            super.print(output, generation);

            outputWriter.write(output);
            this.outputWriter.close();
            this.outputStream.close();

            System.out.println("--------------");
            System.out.println("Schema saved to " + destinationFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
