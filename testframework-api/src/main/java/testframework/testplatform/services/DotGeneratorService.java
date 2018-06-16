package testframework.testplatform.services;

import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Label;
import guru.nidi.graphviz.model.Node;
import org.springframework.stereotype.Service;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.dal.entities.wireentities.WireConnectionEdge;
import testframework.testplatform.dal.entities.wireentities.WireModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

@Service
public class DotGeneratorService {

    /**
     * Generate DOT graph from topology
     *
     * @param topology source
     * @return Graph of the simulation topology
     */
    public Graph generateGraph(Topology topology) {

        Set<WireModel> allModels = new HashSet<>();
        for (WireConnectionEdge edge : topology.getConnections()) {
            allModels.add(edge.getSource());
            allModels.add(edge.getDestination());
        }

        Map<WireModel, Set<WireConnectionEdge>> topoGraph = new HashMap<>();
        for (WireConnectionEdge edge : topology.getConnections()) {
            Set<WireConnectionEdge> entry = topoGraph.getOrDefault(edge.getSource(), new HashSet<>());
            entry.add(edge);
            topoGraph.put(edge.getSource(), entry);
        }

        Map<WireModel, Node> modelNodeMapping = new HashMap<>();
        for (WireModel model : allModels) {
            Node node = node(model.getFullName());
            modelNodeMapping.put(model, node);
        }

        Graph graph = graph().directed();

        for (Map.Entry<WireModel, Set<WireConnectionEdge>> entry : topoGraph.entrySet()) {

            WireModel sourceModel = entry.getKey();
            for (WireConnectionEdge edge : entry.getValue()) {
                assert edge.getSource() == sourceModel;
                WireModel destinationModel = edge.getDestination();

                Node sourceNode = modelNodeMapping.get(sourceModel);
                Node destinationNode = modelNodeMapping.get(destinationModel);

                String label = edge.getInputName() + " -> " + edge.getOutputName();
                Style lineStyle = edge.isDirect() ? Style.SOLID : Style.DASHED;
                graph = graph.with(sourceNode.link(to(destinationNode).with(Label.of(label), lineStyle)));
            }

        }


        return graph;
    }

    public String graphToSvg(Graph graph) {
        Graphviz viz = Graphviz.fromGraph(graph);
        return viz.engine(Engine.DOT).render(Format.SVG).toString();
    }

}
