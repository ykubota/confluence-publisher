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
import java.util.List;
import java.util.Map;

import org.sahli.asciidoc.confluence.publisher.client.ConfluencePublisher;
import org.sahli.asciidoc.confluence.publisher.client.http.NotFoundException;
import org.sahli.asciidoc.confluence.publisher.client.metadata.ConfluencePublisherMetadata;

public class ScatteredAsciidocPageStructureProvider extends DefaultAsciidocPagesStructureProvider {

    private final AsciidocPagesStructure structure;
    private final Charset sourceEncoding;

    public ScatteredAsciidocPageStructureProvider(Path documentationRootFolder, Charset sourceEncoding, AsciidocConfluenceConverter converter){
        structure = buildStructure(documentationRootFolder, converter);
        this.sourceEncoding = sourceEncoding;
    }

    private AsciidocPagesStructure buildStructure(Path documentationRootFolder, AsciidocConfluenceConverter converter) throws IOException {
        walk(documentationRootFolder)
                .filter(AsciidocValidator::validate)
                .forEach(path -> {

                })
             .forEach(path -> {
                 SingleAsciidocPageStructureProvider provider = new SingleAsciidocPageStructureProvider(path, sourceEncoding);
                 ConfluencePublisherMetadata
                         confluencePublisherMetadata = asciidocConfluenceConverter.convert(provider, pageTitlePostProcessor, buildFolder, attributes);
                 if (confluencePublisherMetadata.getPages().size() != 1) {
                     throw new RuntimeException("Parsed multiple pages at once despite each-file mode when read file: " + path);
                 }
                 String parentTitle = confluencePublisherMetadata.getPages().get(0).getParentTitle();
                 if (parentTitle.isEmpty()) {
                     System.out.println("Asciidoc file (" + path + ") doesn't have :" + AsciidocConfluencePage.PARENT_PAGE_TITLE_ATTRIBUTE
                                        + ": attribute. Skipped.");
                 } else {
                     try {
                         String parentId = confluenceClient.getPageByTitle(spaceKey, parentTitle);
                         confluencePublisherMetadata.setAncestorId(parentId);
                         ConfluencePublisher
                                 confluencePublisher = new ConfluencePublisher(confluencePublisherMetadata, publishingStrategy, KEEP_ORPHANS, confluenceClient, new SystemOutLoggingConfluencePublisherListener(), versionMessage);
                         confluencePublisher.publish();
                     } catch (NotFoundException e) {
                         throw new IllegalArgumentException(
                                 "Could not find the parent page being entitled: " + parentTitle, e);
                     }
                 }
             });


        try {
            Map<Path, DefaultAsciidocPage>
                    asciidocPageIndex = indexAsciidocPagesByFolderPath(documentationRootFolder);
            List<DefaultAsciidocPage> allAsciidocPages = connectAsciidocPagesToParent(asciidocPageIndex);
            List<AsciidocPage> topLevelAsciiPages = findTopLevelAsciiPages(allAsciidocPages, documentationRootFolder);

            return new DefaultAsciidocPagesStructure(topLevelAsciiPages);
        } catch (IOException e) {
            throw new RuntimeException("Could not create asciidoc source structure", e);
        }
        return null;
    }

    private List<DefaultAsciidocPage> connectAsciidocPagesToParent(Map<Path, DefaultAsciidocPage> asciidocPageIndex) {
        asciidocPageIndex.forEach((asciidocPageFolderPath, asciidocPage) -> {
            asciidocPageIndex.computeIfPresent(asciidocPage.path().getParent(), (ignored, parentAsciidocPage) -> {
                parentAsciidocPage.addChild(asciidocPage);

                return parentAsciidocPage;
            });
        });

        return new ArrayList<>(asciidocPageIndex.values());
    }

    private static Map<Path, DefaultAsciidocPage> indexAsciidocPagesByFolderPath(Path documentationRootFolder) throws IOException {
        return walk(documentationRootFolder)
                .filter((path) -> isAdocFile(path) && !isIncludeFile(path))
                .collect(toMap((asciidocPagePath) -> removeExtension(asciidocPagePath), (asciidocPagePath) -> new DefaultAsciidocPage(asciidocPagePath)));
    }

    private static List<AsciidocPage> findTopLevelAsciiPages(List<DefaultAsciidocPage> asciiPageByFolderPath, Path documentationRootFolder) {
        return asciiPageByFolderPath.stream()
                                    .filter((asciidocPage) -> asciidocPage.path().equals(documentationRootFolder.resolve(asciidocPage.path().getFileName())))
                                    .collect(toList());
    }

    private static Path removeExtension(Path path) {
        return Paths.get(path.toString().substring(0, path.toString().lastIndexOf('.')));
    }

    private static boolean isAdocFile(Path file) {
        return file.toString().endsWith(ADOC_FILE_EXTENSION);
    }

    private static boolean isIncludeFile(Path file) {
        return file.getFileName().toString().startsWith(INCLUDE_FILE_PREFIX);
    }

    // Titleの重複
    // 親を先
}
