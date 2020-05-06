package org.sahli.asciidoc.confluence.publisher.converter;

import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.sahli.asciidoc.confluence.publisher.client.OrphanRemovalStrategy.KEEP_ORPHANS;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahli.asciidoc.confluence.publisher.client.http.NotFoundException;
import org.sahli.asciidoc.confluence.publisher.client.metadata.ConfluencePublisherMetadata;

public class ScatteredAsciidocPageStructureProvider extends DefaultAsciidocPagesStructureProvider {

    private final AsciidocPagesStructure structure;
    private final Charset sourceEncoding;

    public ScatteredAsciidocPageStructureProvider(Path documentationRootFolder, Charset sourceEncoding){
        structure = buildStructure(documentationRootFolder);
        this.sourceEncoding = sourceEncoding;
    }

    private AsciidocPagesStructure buildStructure(Path documentationRootFolder) {
        try {
            Map<String, EntitledAsciidocPage> index = indexAsciidocPageByTitle(documentationRootFolder);
            index.forEach((title, page) -> {
                index.computeIfPresent(page.getParentTitle(), (ignore, parentPage) -> {
                    parentPage.addChild(page);
                    index.remove(title);
                    return parentPage;
                });
            });
            Arrays.asList(index.values());
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Could not create asciidoc source structure", e);
        }
        return null;
    }

    private Map<String, EntitledAsciidocPage> indexAsciidocPageByTitle(Path documentationRootFolder) throws IOException {
        return walk(documentationRootFolder)
                .filter(path -> validateAdocFile(path))
                .collect(toMap(path->title(path), path -> new EntitledAsciidocPage(path)));
    }

    private String parentTitle(Path adocFilePath) {
        return AsciidocConfluencePage.parseParentTitle(adocFilePath, sourceEncoding);
    }

    private String title(Path adocFilePath) {
        return AsciidocConfluencePage.parseTitle(adocFilePath, sourceEncoding);
    }


    class EntitledAsciidocPage extends DefaultAsciidocPage {
        private final String title;
        private final String parentTitle;

        EntitledAsciidocPage(Path path) {
            super(path);
            this.title = title(path);
            this.parentTitle = parentTitle(path);
        }

        public String getTitle(){
            return title;
        }

        public String getParentTitle(){
            return parentTitle;
        }
    }
}
