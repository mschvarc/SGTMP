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

package testframework.testplatform.xml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import testframework.testplatform.PlatformConfiguration;
import testframework.testplatform.exceptions.ConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testframework.testplatform.configurationgenerator.Helpers.getFirstElementByName;
import static testframework.testplatform.configurationgenerator.Helpers.iterableNodeList;


/**
 * Reader for global_config.xml
 */
@Component
public class GlobalConfigReader {
    private final DocumentBuilder builder;
    private final PlatformConfiguration configuration;

    @Autowired
    public GlobalConfigReader(PlatformConfiguration configuration) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException("Could not create document builder", ex);
        }
        this.configuration = configuration;
    }

    public Map<String, String> getKeyValues(Path path) {
        Map<String, String> result = new HashMap<>();
        result.put("MOSAIK_PYTHON_PATH", configuration.getGlobalConfigPath());

        try {
            Document configDoc = builder.parse(path.toFile());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/config/variables/variable";
            Iterable<Node> nodes = iterableNodeList((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET));
            for (Node node : nodes) {
                Element keyNode = getFirstElementByName(node, "key");
                Element valueNode = getFirstElementByName(node, "value");
                result.put(keyNode.getTextContent(), valueNode.getTextContent());
            }
            return result;
        } catch (XPathExpressionException | IOException | SAXException ex) {
            throw new ConfigurationException("Error retrieving data from file", ex);
        }
    }

    public List<String> extractMosaikConfig() {
        return Arrays.asList(configuration.getMosaikEnv().toAbsolutePath().toString(),
                configuration.getPythonPlatform().toAbsolutePath().toString());
    }

    public boolean redirectErrorStream() {
        return configuration.isMosaikRedirectErrorStream();
    }

    public boolean printResult() {
        return configuration.isPrintResults();
    }

}
