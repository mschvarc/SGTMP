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

package testframework.mosaik.simulators;

import de.offis.mosaik.api.SimProcess;
import de.offis.mosaik.api.Simulator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import testframework.mosaik.models.factory.GlobalTestEvaluatorFactory;
import testframework.mosaik.simulationcontrollers.GlobalTestEvaluatorSimulationController;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;
import testframework.mosaik.util.FrameworkMetaBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static testframework.mosaik.models.ModelTestEvaluator.TEST_RESULT_VAR_NAME;
import static testframework.mosaik.util.Helpers.createNamedTextNode;
import static testframework.mosaik.util.Helpers.ensureParentDirectoryExists;
import static testframework.mosaik.util.Helpers.exportXmlToString;
import static testframework.mosaik.util.Helpers.getBool;

public class GlobalTestEvaluatorSimulator extends AbstractSimulator {

    private static final String MODEL_NAME = "ModelGlobalTestEvaluator";


    private final FrameworkMetaBuilder metaBuilder = new FrameworkMetaBuilder();
    private int counter = 0;
    private String eid;
    private String testPathStr;
    private String fullId;
    private String testName;
    private Path testPath;

    public GlobalTestEvaluatorSimulator(AbstractSimulationController abstractSimulationController) {
        super(abstractSimulationController, "GlobalTestEvaluatorSimulator");
        metaBuilder.addModel(MODEL_NAME, true,
                new String[]{
                        "__StandaloneTestEvaluator__",
                        "__InputCount__",
                        "__TestResultPath__",
                        "__TestResultName__",
                        "__PermanentFailureEntry__",
                        "__MeasuresCount__",
                        "__GlobalMeasureIdMapping__",
                        "__GlobalTargetMeasures__"
                },
                new String[]{
                        "__TEST_PASS__"
                });
    }

    public static void main(String[] args) throws Throwable {
        Simulator sim = new GlobalTestEvaluatorSimulator(new GlobalTestEvaluatorSimulationController(new GlobalTestEvaluatorFactory()));
        if (args.length < 1) {
            throw new IllegalArgumentException();
        } else {
            SimProcess.startSimulation(args, sim);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> init(String sid, Map<String, Object> simParams) {
        super.init(sid, simParams);
        addFrameworkDataToMetadata(metaBuilder, simParams, MODEL_NAME);
        return this.metaBuilder.getOutput();
    }

    @Override
    public List<Map<String, Object>> create(int num, String model, Map<String, Object> modelParams) {
        counter += num;
        if (counter > 1) {
            throw new IllegalStateException("There can only be one GlobalTestEvaluatorSimulator");
        }
        List<Map<String, Object>> createdEntities = super.create(num, model, modelParams);
        eid = (String) (createdEntities.get(0)).get("eid");
        fullId = this.getSimName() + "." + eid;

        testPathStr = (String) modelParams.get("__TestResultPath__");
        testName = (String) modelParams.get("__TestResultName__");
        testPath = Paths.get(testPathStr);

        return createdEntities;
    }

    @Override
    public long step(long time, Map<String, Object> inputs) throws Exception {
        addInputsToCollection(inputs);
        getSimulationController().step(time);
        return time + getStepSize();
    }

    @Override
    public void cleanup() throws Exception {
        super.cleanup();
        Map<String, String> kvLastSteps = ((GlobalTestEvaluatorSimulationController) getSimulationController()).lastSimulationStep();
        Object allMeasures = getSimulationController().getValue(0, "allMeasuresProgress");
        System.out.println("<<<<<>>>>>>");
        System.out.println(allMeasures);
        System.out.println("<<<<<>>>>>>");

        boolean overallTestResult = getBool(getSimulationController().getValue(0, TEST_RESULT_VAR_NAME));
        writeResults(overallTestResult, kvLastSteps);
        System.out.println("OVERALL TEST RESULT: " + overallTestResult);
        System.out.println("ALL MEASURES: " + kvLastSteps.get("allMeasures"));

    }

    private void writeResults(boolean passed, Map<String, String> kvLastSteps) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = document.createElement("result");
        document.appendChild(rootElement);

        Element measures = document.createElement("measuresKV");
        rootElement.appendChild(measures);

        createNamedTextNode(document, rootElement, "passed", Boolean.toString(passed));
        createNamedTextNode(document, rootElement, "name", testName);

        for (Map.Entry<String, String> entry : kvLastSteps.entrySet()) {
            createNamedTextNode(document, measures, entry.getKey(), entry.getValue());
        }

        String exportedXml = exportXmlToString(document);

        ensureParentDirectoryExists(testPath);
        if (!testPath.toFile().exists()) {
            Files.createFile(testPath);
        }
        Files.write(testPath, exportedXml.getBytes(Charset.forName("UTF-8")));
    }

}
