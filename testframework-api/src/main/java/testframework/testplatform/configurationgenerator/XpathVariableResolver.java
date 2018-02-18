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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;
import java.util.HashMap;
import java.util.Map;

//Source: https://stackoverflow.com/questions/3170887/sending-xpath-a-variable-from-java
class XpathVariableResolver implements XPathVariableResolver {
    // local store of variable name -> variable value mappings
    private Map<String, String> variableMappings = new HashMap<>();

    // a way of setting new variable mappings
    public void setVariable(String key, String value) {
        variableMappings.put(key, value);
    }

    // override this method in XPathVariableResolver to
    // be used during evaluation of the XPath expression
    @Override
    public Object resolveVariable(QName varName) {
        // if using namespaces, there's more to do here
        String key = varName.getLocalPart();
        return variableMappings.get(key);
    }
}
