package testframework.testplatform.facade.dto;

import java.util.ArrayList;
import java.util.List;

public class TopologyDto {

    private long id;
    private String topologyName;

    private List<WireConnectionEdgeDto> connections = new ArrayList<>();

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public List<WireConnectionEdgeDto> getConnections() {
        return connections;
    }

    public void setConnections(List<WireConnectionEdgeDto> connections) {
        this.connections = connections;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
