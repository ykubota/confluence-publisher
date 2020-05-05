/*
 * Copyright 2020 the original author or authors.
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TitleBasedAsciidocPageStructureProvider extends DefaultAsciidocPagesStructureProvider {

    private final Map<String, FolderBasedAsciidocPagesStructureProvider> providers;

    public TitleBasedAsciidocPageStructureProvider(Path documentationRootFolder, Charset sourceEncoding) {
        // This provider has multiple FolderBasedAsciidocPagesStructureProvider instead of structure
        super(null, sourceEncoding);
        providers = buildProviders(documentationRootFolder);
    }

    public Map<String, FolderBasedAsciidocPagesStructureProvider> provides() {
        return providers;
    }

    Map<String, FolderBasedAsciidocPagesStructureProvider> buildProviders(
            Path documentationRootFolder) {
        try {
            Map<String, EntitledAsciidocPage> index = indexAsciidocFiles(documentationRootFolder);
            index = moveChildPagesToParent(index);
            return aggregateBasedOnParent(index)
                    .entrySet()
                    .stream()
                    .collect(toMap(
                            Map.Entry::getKey,
                            e -> new FolderBasedAsciidocPagesStructureProvider(
                                    new DefaultAsciidocPagesStructure(e.getValue()), sourceEncoding)));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Could not create asciidoc source structure", e);
        }
    }

    Map<String, EntitledAsciidocPage> indexAsciidocFiles(Path documentationRootFolder) throws IOException {
        try {
            return walk(documentationRootFolder)
                    .filter(DefaultAsciidocPagesStructureProvider::validateAdocFile)
                    .collect(toMap(this::title, EntitledAsciidocPage::new));
        } catch (IllegalStateException e) {
            throw new RuntimeException("Duplicated titles are not allowed.", e);
        }
    }

    Map<String, EntitledAsciidocPage> moveChildPagesToParent(Map<String, EntitledAsciidocPage> index) {
        List<String> children = new ArrayList<>();
        index.forEach((title, page) -> index.computeIfPresent(page.getParentTitle(), (ignore, parentPage) -> {
            parentPage.addChild(page);
            children.add(title);
            return parentPage;
        }));
        index.keySet().removeAll(children);
        return index;
    }

    Map<String, List<AsciidocPage>> aggregateBasedOnParent(Map<String, EntitledAsciidocPage> pages) {
        return pages.values().stream()
                    .collect(toMap(
                            page -> page.parentTitle,
                            Arrays::asList,
                            (page1, page2) -> Stream.concat(page1.stream(), page2.stream()).collect(toList())
                    ));
    }

    String title(Path adocFilePath) {
        return AsciidocConfluencePage.fetchTitle(adocFilePath, sourceEncoding);
    }

    class EntitledAsciidocPage extends DefaultAsciidocPage {
        private final String title;
        private final String parentTitle;

        EntitledAsciidocPage(Path path) {
            super(path);
            title = title(path);
            parentTitle = parentTitle(path);
        }

        public String getTitle() {
            return title;
        }

        public String getParentTitle() {
            return parentTitle;
        }

        String parentTitle(Path adocFilePath) {
            return AsciidocConfluencePage.fetchParentTitle(adocFilePath, sourceEncoding);
        }
    }
}
