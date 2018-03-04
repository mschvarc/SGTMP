package testframework.testplatform.facade;

import testframework.testplatform.dal.entities.wireentities.Topology;

public interface TopologyFacade {
    Topology byId(long id);

    Topology create(Topology topology);

    Topology update(Topology topology);

    Topology delete(Topology topology);
}
