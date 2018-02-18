/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.configurationgeneration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import testframework.testplatform.configurationgenerator.TemplateProcessor;
import testframework.testplatform.configurationgenerator.TemplateProcessorImpl;
import testframework.testplatform.configurationgenerator.TemplateReplacer;
import testframework.testplatform.configurationgenerator.TemplateReplacerImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static testframework.testplatform.configurationgenerator.TemplateReplacer.VARIABLE_ESCAPE_SYMBOL;


public class TemplateProcessorTest {

    private final Path sourcePath = Paths.get(TemplateProcessorTest.class.getResource("config.xml").toURI());
    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    private Path outputFile;


    private TemplateReplacer templateReplacer;
    private TemplateProcessor templateProcessor;
    private DocumentBuilder builder;

    public TemplateProcessorTest() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setCoalescing(true);
        builderFactory.setIgnoringElementContentWhitespace(true);
        builderFactory.setIgnoringComments(true);

        templateReplacer = new TemplateReplacerImpl();
        templateProcessor = new TemplateProcessorImpl(builderFactory, templateReplacer);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        builder = factory.newDocumentBuilder();
        outputFile = outputFolder.newFile().toPath();
    }

    //Source: https://stackoverflow.com/a/4211237/1663367
    @Test
    public void testNoReplacement() throws Exception {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }};

        templateProcessor.replaceTemplate(sourcePath, outputFile, mapping);
        List<String> strings = Files.readAllLines(outputFile, Charset.forName("UTF-8"));
        assertThat(strings).doesNotContain(VARIABLE_ESCAPE_SYMBOL);
        Document inputDoc = builder.parse(outputFile.toFile());
        inputDoc.normalizeDocument();
        Document outputDoc = builder.parse(outputFile.toFile());
        outputDoc.normalizeDocument();
        assertThat(inputDoc.isEqualNode(outputDoc)).isTrue();
    }

    @Test
    public void testBasicReplacement() throws Exception {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }};

        templateProcessor.replaceTemplate(sourcePath, outputFile, mapping);
        List<String> strings = Files.readAllLines(outputFile, Charset.forName("UTF-8"));
        assertThat(strings).doesNotContain(VARIABLE_ESCAPE_SYMBOL);
        Document configDoc = builder.parse(outputFile.toFile());
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final String selectArgsXpath = "/config/test/launch/process/args";
        NodeList nodes = (NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET);
        assertThat(nodes.getLength()).isEqualTo(1);
        Node item = nodes.item(0);
        assertThat(item.getTextContent()).isEqualToIgnoringWhitespace("abcd value1 defgh");
    }

    @Test
    public void testMultipleReplacements() throws Exception {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }};

        templateProcessor.replaceTemplate(sourcePath, outputFile, mapping);
        List<String> strings = Files.readAllLines(outputFile, Charset.forName("UTF-8"));
        assertThat(strings).doesNotContain(VARIABLE_ESCAPE_SYMBOL);
        Document configDoc = builder.parse(outputFile.toFile());
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final String selectArgsXpath = "/config/test/launch/process/params";
        NodeList nodes = (NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET);
        assertThat(nodes.getLength()).isEqualTo(1);
        Node item = nodes.item(0);
        assertThat(item.getTextContent()).isEqualToIgnoringWhitespace("ijkl value1 value2 mnop");
    }

}
