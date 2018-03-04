package testframework.testplatform.dal.repository;

import testframework.testplatform.dal.entities.wireentities.Topology;

public interface TopologyRepository extends EntityRepository<Topology> {
    @Override
    void create(Topology entity);

    @Override
    void update(Topology entity);

    @Override
    void delete(Topology entity);

    @Override
    Topology getById(long id);
}
