package io.klerch.alexa.utterances.output;

import io.klerch.alexa.utterances.format.Formatter;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

public class FileOutputWriter extends AbstractOutputWriter {
    private final String fileName;
    private BufferedWriter outputWriter;
    private FileOutputStream outputStream;

    public FileOutputWriter() {
        this(null);
    }

    public FileOutputWriter(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void beforeWrite(final Formatter formatter) {
        super.beforeWrite(formatter);

        final String filePath = "/" + new Date().getTime() + "_" + Optional.ofNullable(fileName).orElse("utterances") + "." + formatter.getFormat();
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
    public void print() {
        try {
            outputWriter.write(this.formatter.generateSchema());
            this.outputWriter.close();
            this.outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Generated " + this.numOfSamples + " utterances.");
    }
}
