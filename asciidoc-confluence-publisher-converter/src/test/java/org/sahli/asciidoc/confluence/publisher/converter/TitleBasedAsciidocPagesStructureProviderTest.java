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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sahli.asciidoc.confluence.publisher.converter.AsciidocPagesStructureProvider.AsciidocPage;

public class TitleBasedAsciidocPagesStructureProviderTest {

    private static AsciidocPage NON_EXISTING_ASCIIDOC_PAGE = mock(AsciidocPage.class);

    @Test
    public void structure_nestedStructure_returnsAsciidocPagesStructureWithAllNonIncludeAdocFiles() {
        // arrange
        Path documentationRootFolder = Paths.get("src/test/resources/title-based-asciidoc-page-structure");
        TitleBasedAsciidocPageStructureProvider provider = new TitleBasedAsciidocPageStructureProvider(documentationRootFolder, UTF_8);

        // act
        Map<String, FolderBasedAsciidocPagesStructureProvider> providers = provider.provides();

        // assert
        assertThat(providers.keySet().size(), is(3));
        assertThat(providers.containsKey("Top Base"), is(true));
        assertThat(providers.get("Top Base").structure().pages().size(), is(2));
        assertThat(providers.containsKey("TopPage1"), is(true));
        assertThat(providers.get("TopPage1").structure().pages().size(), is(1));
        assertThat(providers.containsKey("TopPage2"), is(true));
        assertThat(providers.get("TopPage2").structure().pages().size(), is(1));



        AsciidocPage testPage = asciidocPageByPath(new ArrayList<>(provider.provides().values()), documentationRootFolder.resolve("TopPage1.adoc"));
        assertThat(testPage, is(not(nullValue())));
        assertThat(testPage.children().size(), is(0));

        AsciidocPage testPage2 = asciidocPageByPath(new ArrayList<>(provider.provides().values()), documentationRootFolder.resolve("toppage2.adoc"));
        assertThat(testPage, is(not(nullValue())));
        assertThat(testPage.children().size(), is(0));

        AsciidocPage pageOnePage = asciidocPageByPath(new ArrayList<>(provider.provides().values()), documentationRootFolder.resolve("page-one/page1.adoc"));
        assertThat(pageOnePage, is(not(nullValue())));
        assertThat(pageOnePage.children().size(), is(0));

        AsciidocPage pageTwoTopPage = asciidocPageByPath(new ArrayList<>(provider.provides().values()), documentationRootFolder.resolve("page-two/top.adoc"));
        assertThat(pageTwoTopPage, is(not(nullValue())));
        assertThat(pageTwoTopPage.children().size(), is(3));
    }

    @Test
    public void sourceEncoding_sourceEncodingProvided_returnsProvidedSourceEncoding() {
        // arrange
        Path documentationRootFolder = Paths.get("src/test/resources/title-based-asciidoc-page-structure");
        TitleBasedAsciidocPageStructureProvider provider = new TitleBasedAsciidocPageStructureProvider(documentationRootFolder, UTF_8);

        // act
        Charset sourceEncoding = provider.sourceEncoding();

        // assert
        assertThat(sourceEncoding, is(UTF_8));
    }

    private AsciidocPage asciidocPageByPath(List<FolderBasedAsciidocPagesStructureProvider> providers, Path asciidocPagePath) {
        return providers.stream()
                 .map(provider -> provider.structure().pages())
                 .flatMap(Collection::stream)
                 .filter((asciidocPage) -> asciidocPage.path().equals(asciidocPagePath))
                 .findFirst()
                 .orElse(NON_EXISTING_ASCIIDOC_PAGE);
    }

}
