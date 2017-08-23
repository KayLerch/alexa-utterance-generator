package io.klerch.alexa.utterances.output;

import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

public class FileUtteranceWriter implements UtteranceWriter {
    private final BufferedWriter outputWriter;
    private final FileOutputStream outputStream;
    private Integer i = 0;

    public FileUtteranceWriter() {
        this(null);
    }

    public FileUtteranceWriter(final String fileName) {
        final String filePath = "/" + new Date().getTime() + "_" + Optional.ofNullable(fileName).orElse("utterances") + ".txt";
        final File file = new File(Paths.get("src/main/resources/output").toUri().getPath() + filePath);

        if (!file.exists()) {
            try {
                Validate.isTrue(file.createNewFile(), "Could not create output file '" + file.getAbsoluteFile() + "'.");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        Validate.isTrue(file.canWrite(), "Cannot write to file '" + file.getAbsoluteFile() + "'. Permissions?");

        try {
            this.outputStream = new FileOutputStream(file, false);
            this.outputWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
            System.out.println("File prepared for writing at '" + file.getAbsoluteFile() + "'");
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final String utterance) {
        try {
            outputWriter.write((i > 0 ? "\n" : "") + utterance);
            i += 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.outputWriter.close();
            this.outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Generated " + i + " utterances.");
    }
}
