package org.sahli.asciidoc.confluence.publisher.converter;

import static java.util.Collections.unmodifiableList;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefaultAsciidocPagesStructureProvider implements AsciidocPagesStructureProvider {

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
