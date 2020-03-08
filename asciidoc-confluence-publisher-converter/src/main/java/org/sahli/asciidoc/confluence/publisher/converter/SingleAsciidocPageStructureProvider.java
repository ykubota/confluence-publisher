package org.sahli.asciidoc.confluence.publisher.converter;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;

import org.sahli.asciidoc.confluence.publisher.converter.FolderBasedAsciidocPagesStructureProvider.DefaultAsciidocPage;
import org.sahli.asciidoc.confluence.publisher.converter.FolderBasedAsciidocPagesStructureProvider.DefaultAsciidocPagesStructure;

public class SingleAsciidocPageStructureProvider implements AsciidocPagesStructureProvider {

    private final AsciidocPagesStructure structure;
    private final Charset sourceEncoding;

    public SingleAsciidocPageStructureProvider(Path filepath, Charset sourceEncoding) {
        structure = buildStructure(filepath);
        this.sourceEncoding = sourceEncoding;
    }

    AsciidocPagesStructure buildStructure(Path filepath){
        return new DefaultAsciidocPagesStructure(Collections.singletonList(new DefaultAsciidocPage(filepath)));
    }

    @Override
    public AsciidocPagesStructure structure() {
        return structure;
    }

    @Override
    public Charset sourceEncoding() {
        return sourceEncoding;
    }
}
