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

import static testframework.testplatform.configurationgenerator.Helpers.getFirstElementByName;


/**
 * Reader for results.xml
 */
@Component
public class TestRunResultReader {
    private final DocumentBuilder builder;

    public TestRunResultReader() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException("Could not create document builder", ex);
        }
    }


    public String getTestResult(Path path) throws IOException {
        try {
            Document configDoc = builder.parse(path.toFile());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/result";
            Node item = ((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET)).item(0);
            return getFirstElementByName(item, "passed").getTextContent();
        } catch (SAXException | XPathExpressionException ex) {
            throw new ConfigurationException("Error getting test results", ex);
        }
    }

    public String getMeasuresJson(Path path) throws IOException {
        try {
            Document configDoc = builder.parse(path.toFile());
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String selectArgsXpath = "/result/measuresKV";
            Node item = ((NodeList) xpath.evaluate(selectArgsXpath, configDoc, XPathConstants.NODESET)).item(0);
            return getFirstElementByName(item, "measures").getTextContent();
        } catch (SAXException | XPathExpressionException ex) {
            throw new ConfigurationException("Error getting measures", ex);
        }
    }
}
