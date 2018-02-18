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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Helpers {

    private Helpers() {
    }

    public static void createNamedTextNode(Document document, Element source, String name, String content) {
        Element sourceSimulatorID = document.createElement(name);
        sourceSimulatorID.appendChild(document.createTextNode(content));
        source.appendChild(sourceSimulatorID);
    }

    public static String exportXmlToString(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }

    public static String[] pathsArrayToString(Path[] filePaths) {
        String[] paths = new String[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            paths[i] = filePaths[i].toAbsolutePath().toString();
        }
        return paths;
    }

    public static Element getFirstElementByName(Node node, String name) {
        return (Element) ((Element) node).getElementsByTagName(name).item(0);
    }

    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        iterable.iterator(),
                        Spliterator.ORDERED
                ),
                false
        );
    }


    //Source: http://stackoverflow.com/a/32781802
    public static Iterable<Node> iterableNodeList(final NodeList n) {
        return () -> new Iterator<Node>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < n.getLength();
            }

            @Override
            public Node next() {
                if (hasNext()) {
                    return n.item(index++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public static void ensureParentDirectoryExists(Path path) throws IOException {
        ensureDirectoryExists(path.getParent());
    }

    public static void ensureDirectoryExists(Path path) throws IOException {
        if (!path.toFile().exists()) {
            Files.createDirectories(path);
        }
        assert path.toFile().exists();
        assert path.toFile().isDirectory();
    }

}
