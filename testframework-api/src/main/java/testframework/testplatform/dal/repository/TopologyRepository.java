package testframework.testplatform.dal.repository;

import testframework.testplatform.dal.entities.wireentities.Topology;

import java.util.List;

public interface TopologyRepository extends EntityRepository<Topology> {
    @Override
    void create(Topology entity);

    @Override
    void update(Topology entity);

    @Override
    void delete(Topology entity);

    @Override
    Topology getById(long id);

    List<Topology> findAll();
}
