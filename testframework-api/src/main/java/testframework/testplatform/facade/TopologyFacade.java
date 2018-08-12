package testframework.testplatform.facade;

import testframework.testplatform.dal.entities.wireentities.Topology;

import java.util.List;

public interface TopologyFacade {
    Topology byId(long id);

    Topology create(Topology topology);

    Topology update(Topology topology);

    Topology delete(Topology topology);

    List<Topology> findAll();
}
