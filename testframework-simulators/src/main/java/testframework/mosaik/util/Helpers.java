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

package testframework.mosaik.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public final class Helpers {

    private Helpers() {
    }

    public static int getInt(Object value) {
        try {
            return ((Number) value).intValue();
        } catch (ClassCastException cce) {
            return Integer.parseInt(value.toString());
        }
    }

    public static long getLong(Object value) {
        try {
            return ((Number) value).longValue();
        } catch (ClassCastException cce) {
            return Long.parseLong(value.toString());
        }
    }

    public static double getDouble(Object value) {
        try {
            return ((Number) value).doubleValue();
        } catch (ClassCastException cce) {
            return Double.parseDouble(value.toString());
        }
    }

    public static boolean getBool(Object value) {
        return value.toString().equalsIgnoreCase("true");
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
