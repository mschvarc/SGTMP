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

package testframework.testplatform.configurationgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;
import testframework.testplatform.exceptions.ConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static testframework.testplatform.configurationgenerator.Helpers.exportXmlToString;
import static testframework.testplatform.configurationgenerator.TemplateReplacer.VARIABLE_ESCAPE_SYMBOL;

/**
 * Translates user generated XML configuration files to ones that can be accepted by the Mosaik Framework
 * Generates wire_connectors.xml, models.xml, simulators.xml and copies simulation_config.xml without any changes
 */
@Component
public class TemplateProcessorImpl implements TemplateProcessor {

    private final DocumentBuilder builder;
    private final TemplateReplacer engine;


    @Autowired
    public TemplateProcessorImpl(DocumentBuilderFactory factory, TemplateReplacer engine) {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Failed to configure document builder", e);
        }
        this.engine = engine;
    }

    @Override
    public void replaceTemplates(Path[] input, Path[] output, Map<String, String> tokens) {
        if (input == null) {
            throw new IllegalArgumentException("inputPaths is null");
        }
        if (output == null) {
            throw new IllegalArgumentException("outputPaths is null");
        }
        if (tokens == null) {
            throw new IllegalArgumentException("tokens is null");
        }
        if (input.length != output.length) {
            throw new IllegalArgumentException("input and output length differs");
        }
        for (int i = 0; i < input.length; i++) {
            replaceTemplate(input[i], output[i], tokens);
        }
    }

    @Override
    public void replaceTemplate(Path input, Path output, Map<String, String> tokens) {
        try {
            Document document = loadConfig(input);
            processTemplate(document, tokens);
            writeResults(document, output);
        } catch (IOException | SAXException | TransformerException ex) {
            throw new ConfigurationException("Failed to replace templates", ex);
        }
    }

    //source: https://stackoverflow.com/questions/12191414/node-gettextcontent-is-there-a-way-to-get-text-content-of-the-current-node-no
    private String getFirstLevelTextContent(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < list.getLength(); ++i) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                textContent.append(child.getTextContent());
        }
        return textContent.toString();
    }

    private Document loadConfig(Path path) throws IOException, SAXException {
        return builder.parse(path.toUri().toString());
    }

    private void processTemplate(Document doc, Map<String, String> tokens) {
        //source: https://stackoverflow.com/questions/5386991/java-most-efficient-method-to-iterate-over-all-elements-in-a-org-w3c-dom-docume
        //source: https://stackoverflow.com/questions/12191414/node-gettextcontent-is-there-a-way-to-get-text-content-of-the-current-node-no
        DocumentTraversal traversal = (DocumentTraversal) doc;
        NodeIterator iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        for (Node node = iterator.nextNode(); node != null; node = iterator.nextNode()) {
            String firstLevelTextContent = getFirstLevelTextContent(node);
            if (firstLevelTextContent.length() > 0 && firstLevelTextContent.contains(VARIABLE_ESCAPE_SYMBOL)) {
                node.setTextContent(engine.replace(firstLevelTextContent, tokens));
            }
        }
    }


    private void writeResults(Document document, Path path) throws IOException, TransformerException {

        Path absParent = path.getParent().toAbsolutePath();
        if (!absParent.toFile().exists()) {
            Files.createDirectories(absParent);
        }

        Files.write(path, exportXmlToString(document).getBytes(Charset.forName("UTF-8")));
    }
}
