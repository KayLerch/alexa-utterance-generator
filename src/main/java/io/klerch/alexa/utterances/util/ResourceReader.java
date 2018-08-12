package io.klerch.alexa.utterances.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Reads grammar and value specification from local file system
 */
public class ResourceReader {
    /**
     * Looking up value specification file in a given path for a given file key
     * @param path where to look up for values files
     * @param valueResource file key
     * @return if file found in path it returns the file content as a list of strings (one element per line)
     */
    public static Optional<List<String>> getPlaceholderValueList(final Path path, final String valueResource) {
        final Path pathToFile = path.resolve(valueResource + ".values");
        if (Files.exists(pathToFile)) {
            final File valuesFiles = new File(pathToFile.toUri());
            Validate.isTrue(valuesFiles.canRead(), "Could not obtain read access to referenced values file " + pathToFile.toAbsolutePath().toString());
            return Optional.ofNullable(getLines(valuesFiles));
        }
        return Optional.empty();
    }

    /**
     * Reads file contents to list of strings (one element per line). Cleans up comments and blank lines as well.
     * @param file file reference
     * @return list of strings representing lines in the file
     */
    public static List<String> getLines(final File file) {
        final List<String> lines = new ArrayList<>();

        Optional.ofNullable(file).ifPresent(f -> {
            try (final Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine().split("//")[0]);
                }
                scanner.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        // eliminate empty lines
        lines.removeIf(StringUtils::isBlank);
        // eliminate commentary
        lines.removeIf(line -> line.startsWith("//"));
        return lines;
    }
}
