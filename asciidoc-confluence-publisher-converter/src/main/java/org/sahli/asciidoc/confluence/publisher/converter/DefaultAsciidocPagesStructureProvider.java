package org.sahli.asciidoc.confluence.publisher.converter;

import static java.util.Collections.unmodifiableList;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DefaultAsciidocPagesStructureProvider implements AsciidocPagesStructureProvider {

    private static final String ADOC_FILE_EXTENSION = ".adoc";
    private static final String INCLUDE_FILE_PREFIX = "_";

    final AsciidocPagesStructure structure;
    final Charset sourceEncoding;

    DefaultAsciidocPagesStructureProvider(){
        this(null, null);
    }

    DefaultAsciidocPagesStructureProvider(AsciidocPagesStructure structure, Charset sourceEncoding) {
        this.structure= structure;
        this.sourceEncoding = sourceEncoding;
    }

    @Override
    public AsciidocPagesStructure structure() {
        return structure;
    }

    @Override
    public Charset sourceEncoding() {
        return sourceEncoding;
    }

    static Path removeExtension(Path path) {
        return Paths.get(path.toString().substring(0, path.toString().lastIndexOf('.')));
    }

    static boolean validateAdocFile(Path path) {
        return Files.isRegularFile(path) && isAdocFile(path) && !isIncludeFile(path);
    }

    private static boolean isAdocFile(Path file) {
        return file.toString().endsWith(ADOC_FILE_EXTENSION);
    }

    private static boolean isIncludeFile(Path file) {
        return file.getFileName().toString().startsWith(INCLUDE_FILE_PREFIX);
    }

    public static class DefaultAsciidocPagesStructure implements AsciidocPagesStructure {

        private final List<AsciidocPage> asciidocPages;

        DefaultAsciidocPagesStructure(List<AsciidocPage> asciidocPages) {
            this.asciidocPages = asciidocPages;
        }

        @Override
        public List<AsciidocPage> pages() {
            return asciidocPages;
        }

    }

    public static class DefaultAsciidocPage implements AsciidocPage {

        private final Path path;
        private final List<AsciidocPage> children;

        DefaultAsciidocPage(Path path) {
            this.path = path;
            children = new ArrayList<>();
        }

        void addChild(AsciidocPage child) {
            children.add(child);
        }

        @Override
        public Path path() {
            return path;
        }

        @Override
        public List<AsciidocPage> children() {
            return unmodifiableList(children);
        }

    }
}
