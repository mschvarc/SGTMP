package testframework;

import guru.nidi.graphviz.model.Graph;
import org.junit.Before;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestStep;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.MeasureType;
import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;
import testframework.testplatform.dal.entities.measure.operators.LessThanOperand;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.dal.entities.wireentities.WireConnectionEdge;
import testframework.testplatform.dal.entities.wireentities.WireModel;
import testframework.testplatform.dal.entities.wireentities.WireSimulator;
import testframework.testplatform.services.DotGeneratorService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DotGenerationServiceTest {

    private Test test = new Test();

    @Before
    public void setup() {

        test.setTestDescription("test description");
        test.setTestName("test name");
        test.setUuid(UUID.fromString("0313c600-b72d-11e7-abc4-cec278b6b50a").toString());

        //setup measures for testing
        MeasureAttribute measureAttribute1 = new MeasureAttribute();
        measureAttribute1.setSimulatorId("SimulatorArduino");
        measureAttribute1.setModelId("Model_Arduino");
        measureAttribute1.setName("SimulatorArduino:Model_Arduino:voltageOut");
        measureAttribute1.setMeasureInputName("voltageMeasure");

        Measure measure1 = new Measure();
        measure1.setMeasureType(MeasureType.EXPECTED);
        measure1.setValue(50);
        measure1.setUnit("mili-Volts");
        measure1.setMeasureAttribute(measureAttribute1);

        LessThanOperand lessThanOperand = new LessThanOperand(new Measure(5000));
        Map<MeasureAttribute, ExpectedResult> expectedResultMap = new HashMap<>();
        expectedResultMap.put(measureAttribute1, lessThanOperand);

        TestStep step1 = new TestStep();
        step1.setExpectedResults(expectedResultMap);
        step1.setStep(5);
        step1.setTest(test);

        test.setTestSteps(Collections.singletonList(step1));


        //setup topology
        Topology topology = new Topology();

        WireSimulator simulator1 = new WireSimulator("sim1", false);
        WireModel model1 = new WireModel(simulator1, "model1", 0);

        WireSimulator simulator2 = new WireSimulator("sim2", false);
        WireModel model2 = new WireModel(simulator2, "model2", 0);

        WireConnectionEdge edge1 = new WireConnectionEdge("input1", "output1", model1, model2, true, true);
        WireConnectionEdge edge2 = new WireConnectionEdge("input2", "output2", model2, model1, false, true);

        topology.setTopologyName("topology1");
        topology.getConnections().add(edge1);
        topology.getConnections().add(edge2);

        test.setTopology(topology);
    }

    @org.junit.Test
    public void testJsonGeneration() throws Exception {
        DotGeneratorService service = new DotGeneratorService();
        Graph graph = service.generateGraph(test.getTopology());
        String result = service.graphToSvg(graph);
        assertThat(result).isNotBlank();
    }
}
