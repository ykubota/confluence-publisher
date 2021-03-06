/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sahli.asciidoc.confluence.publisher.converter;

import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FolderBasedAsciidocPagesStructureProvider extends DefaultAsciidocPagesStructureProvider {

    private final AsciidocPagesStructure structure;
    private final Charset sourceEncoding;

    public FolderBasedAsciidocPagesStructureProvider(Path documentationRootFolder, Charset sourceEncoding) {
        this.structure = buildStructure(documentationRootFolder);
        this.sourceEncoding = sourceEncoding;
    }

    public FolderBasedAsciidocPagesStructureProvider(AsciidocPagesStructure structure, Charset sourceEncoding) {
        this.structure = structure;
        this.sourceEncoding = sourceEncoding;
    }

    @Override
    public AsciidocPagesStructure structure() {
        return this.structure;
    }

    @Override
    public Charset sourceEncoding() {
        return this.sourceEncoding;
    }

    private AsciidocPagesStructure buildStructure(Path documentationRootFolder) {
        try {
            Map<Path, DefaultAsciidocPage> asciidocPageIndex = indexAsciidocPagesByFolderPath(documentationRootFolder);
            List<DefaultAsciidocPage> allAsciidocPages = connectAsciidocPagesToParent(asciidocPageIndex);
            List<AsciidocPage> topLevelAsciiPages = findTopLevelAsciiPages(allAsciidocPages, documentationRootFolder);

            return new DefaultAsciidocPagesStructure(topLevelAsciiPages);
        } catch (IOException e) {
            throw new RuntimeException("Could not create asciidoc source structure", e);
        }
    }

    @SuppressWarnings("CodeBlock2Expr")
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
                .filter((path) -> validateAdocFile(path))
                .collect(toMap((asciidocPagePath) -> removeExtension(asciidocPagePath), (asciidocPagePath) -> new DefaultAsciidocPage(asciidocPagePath)));
    }

    private static List<AsciidocPage> findTopLevelAsciiPages(List<DefaultAsciidocPage> asciiPageByFolderPath, Path documentationRootFolder) {
        return asciiPageByFolderPath.stream()
                .filter((asciidocPage) -> asciidocPage.path().equals(documentationRootFolder.resolve(asciidocPage.path().getFileName())))
                .collect(toList());
    }
}
