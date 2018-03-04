package testframework.testplatform.dal.entities.wireentities;

import org.hibernate.validator.constraints.Length;
import testframework.testplatform.dal.entities.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Topology extends BaseEntity {

    @NotNull
    @Length(max = 100)
    private String topologyName;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<WireConnectionEdge> connections = new ArrayList<>();

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public List<WireConnectionEdge> getConnections() {
        return connections;
    }

    public void setConnections(List<WireConnectionEdge> connections) {
        this.connections = connections;
    }
}
