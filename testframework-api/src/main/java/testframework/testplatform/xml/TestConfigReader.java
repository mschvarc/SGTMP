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

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static testframework.testplatform.configurationgenerator.Helpers.getFirstElementByName;
import static testframework.testplatform.configurationgenerator.Helpers.iterableNodeList;


/**
 * Reader for test.xml
 */
@Component
public class TestConfigReader {
    private final DocumentBuilder builder;

    public TestConfigReader() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException("Could not create document builder", ex);
        }
    }


    public List<List<String>> extractPrograms(Path path) {
        try {
            List<List<String>> returnValues = new ArrayList<>();

            Document configDoc = builder.parse(path.toFile());

            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/config/test/launch/process";
            Iterable<Node> nodes = iterableNodeList((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET));
            for (Node process : nodes) {
                String processPath = getFirstElementByName(process, "path").getTextContent();
                String[] processArgs = getFirstElementByName(process, "args").getTextContent().split(" ");
                List<String> thisValues = new ArrayList<>();
                thisValues.add(processPath);
                thisValues.addAll(Arrays.asList(processArgs));
                returnValues.add(thisValues);
            }
            return returnValues;
        } catch (XPathExpressionException | IOException | SAXException ex) {
            throw new ConfigurationException("Error extracting programs", ex);
        }
    }

    public boolean redirectErrorStream(Path path) {
        try {
            Document configDoc = builder.parse(path.toFile());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/config/test/launch";
            Node item = ((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET)).item(0);
            String processPath = getFirstElementByName(item, "redirectErrorStream").getTextContent();
            return processPath.equalsIgnoreCase("true");
        } catch (XPathExpressionException | IOException | SAXException ex) {
            throw new ConfigurationException("Error in redirect error stream config", ex);
        }
    }

    public boolean printResults(Path path) {
        try {
            Document configDoc = builder.parse(path.toFile());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/config/test/launch";
            Node item = ((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET)).item(0);
            String processPath = getFirstElementByName(item, "printResults").getTextContent();
            return processPath.equalsIgnoreCase("true");
        } catch (XPathExpressionException | IOException | SAXException ex) {
            throw new ConfigurationException("Error extracting print results config", ex);
        }
    }

}
