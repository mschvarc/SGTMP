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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import testframework.testplatform.exceptions.ConfigurationException;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.PerStepMeasureSerial;
import testframework.testplatform.dal.entities.wireentities.WireConnectionEdge;
import testframework.testplatform.dal.entities.wireentities.WireModel;
import testframework.testplatform.dal.entities.wireentities.WireSimulator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static testframework.testplatform.configurationgenerator.Helpers.createNamedTextNode;
import static testframework.testplatform.configurationgenerator.Helpers.exportXmlToString;
import static testframework.testplatform.configurationgenerator.Helpers.getFirstElementByName;
import static testframework.testplatform.configurationgenerator.Helpers.iterableNodeList;
import static testframework.testplatform.configurationgenerator.Helpers.toStream;


public class RuntimeConfigurationGeneratorImpl implements RuntimeConfigurationGenerator {

    private static final String SOURCE = "source";
    private static final String SIMULATOR_ID = "simulatorID";
    private static final String MODEL_ID = "modelID";
    private static final String OUTPUT_NAME = "outputName";
    private static final String MODEL_INDEX = "modelIndex";
    private static final String DESTINATION = "destination";
    private static final String INPUT_NAME = "inputName";
    private static final String IS_DIRECT = "isDirect";
    private static final String ASYNC_REQUIRED = "asyncRequired";
    private static final String CONNECTION = "connection";
    private static final String DIRECT_CONNECTIONS = "directConnections";
    private static final String ASYNC_CONNECTIONS = "asyncConnections";
    private static final String SYNC_TARGET_NAME = "syncTargetName";
    private static final String ADDITIONAL_WORLD_PARAMS = "additionalWorldParams";
    private static final String PARAM = "param";
    private static final String VALUE = "value";
    private static final String KEY = "key";
    private final DocumentBuilder builder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Test test;
    private Document userWireDoc;
    private Document modelsDoc;
    private Document genWireDoc;
    private Document simulatorsDoc;
    private Document simulationConfigDoc;
    private Document configDoc;


    /**
     * Configures this class for a specific Test instance
     *
     * @param test test
     */
    public RuntimeConfigurationGeneratorImpl(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("Test cannot be null");
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException("Could not create Xpath parser", ex);
        }

        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.configure(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION, true);
        mapper.registerModule(hibernate5Module);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        this.test = test;
    }

    @Override
    public void createTestDataModels(String[] filePaths) {
        try {
            loadConfig(filePaths);
            parseConfig();
            writeResults(filePaths);
        } catch (XPathExpressionException | IOException | SAXException | TransformerException ex) {
            throw new ConfigurationException("Malformed input file", ex);
        }
    }

    private void loadConfig(String[] args) throws IOException, SAXException {
        simulationConfigDoc = builder.parse(Paths.get(args[0]).toUri().toString());
        simulatorsDoc = builder.parse(Paths.get(args[1]).toUri().toString());
        modelsDoc = builder.parse(Paths.get(args[2]).toUri().toString());
        userWireDoc = builder.parse(Paths.get(args[3]).toUri().toString());
        genWireDoc = builder.newDocument();
        configDoc = builder.parse(Paths.get(args[4]).toUri().toString());
    }

    private void parseConfig() throws XPathExpressionException, IOException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final String connectionXpath = "/wiring/connection";
        final NodeList connectionNodes = (NodeList) xpath.evaluate(connectionXpath, userWireDoc, XPathConstants.NODESET);

        List<WireConnectionEdge> edges = createEdgeData(connectionNodes);
        NewInputs newInputsForModel = new NewInputs();

        addAsyncEdgeData(edges, newInputsForModel);
        TestStepList testStepList = new TestStepList(test);

        configureLocalMeasures(testStepList, newInputsForModel, edges);
        globalSimulatorConfig(edges, newInputsForModel);
        globalSimulatorConfigMeasures(testStepList);

        generateNewSimulatorsConfig(newInputsForModel, edges);

        generateWireConfig(edges);
    }

    private void writeResults(String[] args) throws FileNotFoundException, TransformerException {
        try (PrintWriter pw = new PrintWriter(args[5])) {
            pw.println(exportXmlToString(simulationConfigDoc));
        }
        try (PrintWriter pw = new PrintWriter(args[6])) {
            pw.println(exportXmlToString(simulatorsDoc));
        }
        try (PrintWriter pw = new PrintWriter(args[7])) {
            pw.println(exportXmlToString(modelsDoc));
        }
        try (PrintWriter pw = new PrintWriter(args[8])) {
            pw.println(exportXmlToString(genWireDoc));
        }
        try (PrintWriter pw = new PrintWriter(args[9])) {
            pw.println(exportXmlToString(configDoc));
        }
    }

    private List<WireConnectionEdge> createEdgeData(NodeList connectionNodes) {
        List<WireConnectionEdge> edges = new ArrayList<>();
        for (Node connection : iterableNodeList(connectionNodes)) {
            Element source = getFirstElementByName(connection, SOURCE);
            String sourceSimulatorID = getFirstElementByName(source, SIMULATOR_ID).getTextContent();
            String sourceModelID = getFirstElementByName(source, MODEL_ID).getTextContent();
            String sourceOutputName = getFirstElementByName(source, OUTPUT_NAME).getTextContent();
            int sourceModelIndex = Integer.parseInt(getFirstElementByName(source, MODEL_INDEX).getTextContent());

            Element destination = getFirstElementByName(connection, DESTINATION);
            String destinatonSimulatorID = getFirstElementByName(destination, SIMULATOR_ID).getTextContent();
            String destinatonModelID = getFirstElementByName(destination, MODEL_ID).getTextContent();
            String destinatonInputName = getFirstElementByName(destination, INPUT_NAME).getTextContent();
            int destinatonModelIndex = Integer.parseInt(getFirstElementByName(destination, MODEL_INDEX).getTextContent());

            boolean isDirect = Boolean.parseBoolean(getFirstElementByName(connection, IS_DIRECT).getTextContent());
            boolean asyncRequests = Boolean.parseBoolean(getFirstElementByName(connection, ASYNC_REQUIRED).getTextContent());


            WireSimulator sourceSimulator = new WireSimulator(sourceSimulatorID);
            WireModel sourceModel = new WireModel(sourceSimulator, sourceModelID, sourceModelIndex);

            WireSimulator destinationSimulator = new WireSimulator(destinatonSimulatorID);
            WireModel destinationModel = new WireModel(destinationSimulator, destinatonModelID, destinatonModelIndex);

            WireConnectionEdge edge = new WireConnectionEdge(destinatonInputName, sourceOutputName, sourceModel, destinationModel, isDirect, asyncRequests);
            edges.add(edge);
        }
        return edges;
    }

    private void addAsyncEdgeData(List<WireConnectionEdge> edges, NewInputs newInputsForModel) {
    /* go through edges and:
        1. add one global test evaluator (TBD in python???)
        2. connect all local test evaluators to global one (TBD, will be async in python automatically???)
        3. Add syncTargetName to directConnections
        4. generate asyncConnections
        5. syncTargetName must be added to model params for META generation, ensure it always returns some value (even null)
    */

        //Add syncTargetName to directConnections and to data for async edge
        List<WireConnectionEdge> newEdges = new ArrayList<>();
        for (WireConnectionEdge edge : edges) {
            if (edge.isDirect()) {
                continue;
            }

            String syncName = "syncTarget_" + edge.getDestination().getFullName() + "_" + edge.getOutputName();
            edge.setSyncTargetName(syncName);
            //flipped dest / src
            WireConnectionEdge newEdge = new WireConnectionEdge(syncName, syncName, edge.getDestination(), edge.getSource(), true, true);
            newEdges.add(newEdge);
            newInputsForModel.addInputToMap(edge.getDestination(), syncName);

            newInputsForModel.addInputToMap(edge.getSource(), syncName);

        }
        edges.addAll(newEdges);
    }

    private void generateWireConfig(List<WireConnectionEdge> edges) {
        //write back the WIRING data to XML
        Element rootElement = genWireDoc.createElement("wiring");
        genWireDoc.appendChild(rootElement);
        Element directConnections = genWireDoc.createElement(DIRECT_CONNECTIONS);
        rootElement.appendChild(directConnections);
        Element asyncConnections = genWireDoc.createElement(ASYNC_CONNECTIONS);
        rootElement.appendChild(asyncConnections);

        for (WireConnectionEdge edge : edges) {
            if (edge.isDirect()) {
                Element connection = genWireDoc.createElement(CONNECTION);
                directConnections.appendChild(connection);

                Element source = genWireDoc.createElement(SOURCE);
                connection.appendChild(source);
                createNamedTextNode(genWireDoc, source, SIMULATOR_ID, edge.getSource().getSimulator().getSimulatorId());
                createNamedTextNode(genWireDoc, source, MODEL_ID, edge.getSource().getModelId());
                createNamedTextNode(genWireDoc, source, MODEL_INDEX, Integer.toString(edge.getSource().getModelIndex()));
                createNamedTextNode(genWireDoc, source, OUTPUT_NAME, edge.getOutputName());

                Element destination = genWireDoc.createElement(DESTINATION);
                connection.appendChild(destination);
                createNamedTextNode(genWireDoc, destination, SIMULATOR_ID, edge.getDestination().getSimulator().getSimulatorId());
                createNamedTextNode(genWireDoc, destination, MODEL_ID, edge.getDestination().getModelId());
                createNamedTextNode(genWireDoc, destination, MODEL_INDEX, Integer.toString(edge.getDestination().getModelIndex()));
                createNamedTextNode(genWireDoc, destination, INPUT_NAME, edge.getInputName());

                createNamedTextNode(genWireDoc, connection, ASYNC_REQUIRED, Boolean.toString(edge.isAsyncRequests()));
            } else {
                Element connection = genWireDoc.createElement(CONNECTION);
                asyncConnections.appendChild(connection);

                Element source = genWireDoc.createElement(SOURCE);
                connection.appendChild(source);
                createNamedTextNode(genWireDoc, source, SIMULATOR_ID, edge.getSource().getSimulator().getSimulatorId());
                createNamedTextNode(genWireDoc, source, MODEL_ID, edge.getSource().getModelId());
                createNamedTextNode(genWireDoc, source, MODEL_INDEX, Integer.toString(edge.getSource().getModelIndex()));
                createNamedTextNode(genWireDoc, source, OUTPUT_NAME, edge.getOutputName());

                Element destination = genWireDoc.createElement(DESTINATION);
                connection.appendChild(destination);
                createNamedTextNode(genWireDoc, destination, SIMULATOR_ID, edge.getDestination().getSimulator().getSimulatorId());
                createNamedTextNode(genWireDoc, destination, MODEL_ID, edge.getDestination().getModelId());
                createNamedTextNode(genWireDoc, destination, MODEL_INDEX, Integer.toString(edge.getDestination().getModelIndex()));
                createNamedTextNode(genWireDoc, destination, INPUT_NAME, edge.getInputName());
                createNamedTextNode(genWireDoc, destination, SYNC_TARGET_NAME, edge.getSyncTargetName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void generateNewSimulatorsConfig(NewInputs newInputsForModel, List<WireConnectionEdge> edges) throws XPathExpressionException {
        XPath xpathModels = XPathFactory.newInstance().newXPath();
        XPath xpathSimulators = XPathFactory.newInstance().newXPath();
        XpathVariableResolver variableResolver = new XpathVariableResolver();
        xpathSimulators.setXPathVariableResolver(variableResolver);

        //simulators.XML
        /*
         1. add asyncInfo to params of metamodel
         2. create global test evaluator with appropriate input0...inputXX and inputcount=XX
            injecting:
            __additionalParams__ = {asyncInfo }
            __additionalInputs__ = {asyncTargetXX }
         */

        final String simulatorsXpathStr = "/simulators/simulator";
        final Iterable<Node> nodes = iterableNodeList((NodeList) xpathModels.evaluate(simulatorsXpathStr, simulatorsDoc, XPathConstants.NODESET));

        for (Node simulatorNode : nodes) {
            String simulatorID = getFirstElementByName(simulatorNode, "id").getTextContent();
            List<String> additionalInputsArray = new ArrayList<>();
            Element modelParams = getFirstElementByName(simulatorNode, ADDITIONAL_WORLD_PARAMS);

            if (needsAsyncInfoAttribute(edges, simulatorID)) {
                JSONArray additionalParams = new JSONArray();
                additionalParams.add("asyncInfo");

                Element newParamAttribute = simulatorsDoc.createElement(PARAM);
                modelParams.appendChild(newParamAttribute);
                createNamedTextNode(simulatorsDoc, newParamAttribute, KEY, "__additionalParams__");
                createNamedTextNode(simulatorsDoc, newParamAttribute, VALUE, additionalParams.toJSONString());
            }

            if (newInputsForModel.containsNestedKey(simulatorID)) {
                additionalInputsArray.addAll(newInputsForModel.getFlatInputsForSimulator(simulatorID));
            }
            if (additionalInputsArray.isEmpty()) {
                continue;
            }

            String jsonArrayInputs = JSONArray.toJSONString(additionalInputsArray);
            Element newParam = simulatorsDoc.createElement(PARAM);
            modelParams.appendChild(newParam);
            createNamedTextNode(simulatorsDoc, newParam, KEY, "__additionalInputs__");
            createNamedTextNode(simulatorsDoc, newParam, VALUE, jsonArrayInputs);
        }
    }

    private boolean needsAsyncInfoAttribute(List<WireConnectionEdge> edges, String simulatorID) {
        WireSimulator simulator = new WireSimulator(simulatorID);
        for (WireConnectionEdge edge : edges) {
            if (!edge.isAsyncRequests()) {
                continue;
            }
            if (edge.getDestination().getSimulator().equals(simulator) /*&& edge.getDestination().getModelId().equals(modelID)*/ /*&& edge.getDestination().getModelIndex() == modelIndex*/) {
                return true;
            }
        }
        return false;
    }

    private WireModel getGlobalTestEvalModelFromModels(Document document) throws XPathExpressionException {
        final XPath xpathModels = XPathFactory.newInstance().newXPath();

        final String globalTestEvaluatorSimulatorsXmlXpath = "/models/model[./simulatorID/text() = 'GlobalTestEvaluatorSimulator']";
        final Iterable<Node> globalEvalNodes = iterableNodeList((NodeList) xpathModels.evaluate(globalTestEvaluatorSimulatorsXmlXpath, document, XPathConstants.NODESET));
        final Node globalEvalElement = globalEvalNodes.iterator().next();
        return new WireModel(
                new WireSimulator(getFirstElementByName(globalEvalElement, SIMULATOR_ID).getTextContent()),
                getFirstElementByName(globalEvalElement, MODEL_ID).getTextContent(),
                Integer.parseInt(getFirstElementByName(globalEvalElement, MODEL_INDEX).getTextContent()));
    }

    private void globalSimulatorConfig(List<WireConnectionEdge> edges, NewInputs newInputsForModel) throws XPathExpressionException {
        final XPath xpathModels = XPathFactory.newInstance().newXPath();
        final String globalTestEvaluatorSimulatorsXmlXpath = "/models/model[./simulatorID/text() = 'GlobalTestEvaluatorSimulator']";
        final Iterable<Node> globalEvalNodes = iterableNodeList((NodeList) xpathModels.evaluate(globalTestEvaluatorSimulatorsXmlXpath, modelsDoc, XPathConstants.NODESET));
        final Node globalEvalElement = globalEvalNodes.iterator().next();
        final WireModel globalTestEval = getGlobalTestEvalModelFromModels(modelsDoc);

        Element worldParamsGlobalEval = getFirstElementByName(globalEvalElement, "additionalModelParams");

        final String modelsWithStandaloneEval = "/models/model[./additionalModelParams/param/key/text() = '__StandaloneTestEvaluator__' and ./additionalModelParams/param/value/text() = 'true' ]";
        Iterable<Node> standaloneEvaluatorModels = iterableNodeList((NodeList) xpathModels.evaluate(modelsWithStandaloneEval, modelsDoc, XPathConstants.NODESET));

        final String modelsWithAttachedEval = "/models/model[./additionalModelParams/param/key/text() = '__requiresAttachedTestEvaluator__' and ./additionalModelParams/param/value/text() = 'true' ]";
        Iterable<Node> attachedEvaluatorModels = iterableNodeList((NodeList) xpathModels.evaluate(modelsWithAttachedEval, modelsDoc, XPathConstants.NODESET));

        List<Node> merged = Stream.concat(toStream(standaloneEvaluatorModels), toStream(attachedEvaluatorModels)).collect(Collectors.toList());

        int inputCount = 0;
        for (Node standaloneEvaluatorModel : merged) {
            //generate edges
            String simulatorID = getFirstElementByName(standaloneEvaluatorModel, SIMULATOR_ID).getTextContent();
            String modelID = getFirstElementByName(standaloneEvaluatorModel, MODEL_ID).getTextContent();
            int modelIndex = Integer.parseInt(getFirstElementByName(standaloneEvaluatorModel, MODEL_INDEX).getTextContent());

            if (simulatorID.equals("GlobalTestEvaluatorSimulator")) {
                continue;
            }

            WireModel localTestEval = new WireModel(new WireSimulator(simulatorID), modelID, modelIndex);
            WireConnectionEdge edge = new WireConnectionEdge("input" + inputCount, "__TEST_PASS__", localTestEval, globalTestEval, true, false);
            edges.add(edge);
            inputCount++;
        }

        Element newParam = modelsDoc.createElement(PARAM);
        worldParamsGlobalEval.appendChild(newParam);
        createNamedTextNode(modelsDoc, newParam, KEY, "__InputCount__");
        createNamedTextNode(modelsDoc, newParam, VALUE, Integer.toString(inputCount));

        //add additional_inputs:
        for (int i = 0; i < inputCount; i++) {
            newInputsForModel.addInputToMap(globalTestEval, "input" + i);
        }
    }

    @SuppressWarnings("unchecked")
    private void globalSimulatorConfigMeasures(TestStepList testStepList) throws XPathExpressionException, IOException {
        final XPath xpathModels = XPathFactory.newInstance().newXPath();

        final String globalTestEvaluatorSimulatorsXmlXpath
                = "/models/model[./simulatorID/text() = 'GlobalTestEvaluatorSimulator']";
        final Iterable<Node> globalEvalNodes = iterableNodeList(
                (NodeList) xpathModels.evaluate(globalTestEvaluatorSimulatorsXmlXpath,
                        modelsDoc, XPathConstants.NODESET));
        final Node globalTestEvalElement = globalEvalNodes.iterator().next();
        Element additionalModelParams = getFirstElementByName(globalTestEvalElement, "additionalModelParams");

        final int measuresCount = testStepList.getAllMeasureAttributes().size();

        Element newParamMeasureCount = modelsDoc.createElement(PARAM);
        additionalModelParams.appendChild(newParamMeasureCount);
        createNamedTextNode(modelsDoc, newParamMeasureCount, KEY, "__MeasuresCount__");
        createNamedTextNode(modelsDoc, newParamMeasureCount, VALUE, Integer.toString(measuresCount));

        Element newParamIdMap = modelsDoc.createElement(PARAM);
        additionalModelParams.appendChild(newParamIdMap);
        createNamedTextNode(modelsDoc, newParamIdMap, KEY, "__GlobalMeasureIdMapping__");
        Element newParamTargetMeasures = modelsDoc.createElement(PARAM);
        additionalModelParams.appendChild(newParamTargetMeasures);
        createNamedTextNode(modelsDoc, newParamTargetMeasures, KEY, "__GlobalTargetMeasures__");

        JSONArray expectedResultListArray = new JSONArray();

        for (PerStepMeasureSerial perStepMeasure : testStepList.getPerStepMeasures()) {
            String serialized = mapper.writeValueAsString(perStepMeasure);
            expectedResultListArray.add(serialized);
        }
        String globalMeasureIdMappingJson = mapper.writeValueAsString(testStepList.getAllMeasureAttributes());
        createNamedTextNode(modelsDoc, newParamIdMap, VALUE, globalMeasureIdMappingJson);
        createNamedTextNode(modelsDoc, newParamTargetMeasures, VALUE, expectedResultListArray.toJSONString());
    }

    private void configureLocalMeasures(
            TestStepList testStepList,
            NewInputs newInputsForModel,
            List<WireConnectionEdge> edges) throws XPathExpressionException {

        //add inputs
        for (int i = 0; i < testStepList.getAllMeasureAttributes().size(); i++) {
            MeasureAttribute measureAttribute = testStepList.getAllMeasureAttributes().get(i);

            WireModel globalEval = getGlobalTestEvalModelFromModels(modelsDoc);

            newInputsForModel.addInputToMap(
                    measureAttribute.getSimulatorId(),
                    measureAttribute.getModelId(),
                    measureAttribute.getModelIndex(),
                    measureAttribute.getMeasureInputName());

            newInputsForModel.addInputToMap(globalEval, measureAttribute.getMeasureInputName());

            WireSimulator sourceSimulator = new WireSimulator(measureAttribute.getSimulatorId());
            WireModel sourceModel = new WireModel(sourceSimulator, measureAttribute.getModelId(), measureAttribute.getModelIndex());

            //wire simulator.model.index -> globalTestEval.measure#
            WireConnectionEdge edge = new WireConnectionEdge(measureAttribute.getMeasureInputName(), measureAttribute.getMeasureInputName(), sourceModel, globalEval, true, true);
            edges.add(edge);
        }
    }
}
