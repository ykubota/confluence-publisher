package org.sahli.asciidoc.confluence.publisher.cli;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AsciidocValidator {
    private static final String ADOC_FILE_EXTENSION = ".adoc";
    private static final String INCLUDE_FILE_PREFIX = "_";

    private AsciidocValidator() {}

    static boolean validate(Path path) {
        return Files.isRegularFile(path) &&
               path.toString().endsWith(ADOC_FILE_EXTENSION) &&
               !path.getFileName().toString().startsWith(INCLUDE_FILE_PREFIX);
    }
}
