package testframework.testplatform.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.dal.repository.TopologyRepository;
import testframework.testplatform.mapper.Automapper;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TopologyFacadeImpl implements TopologyFacade {

    private final Automapper mapper;
    private final TopologyRepository repository;

    @Autowired
    public TopologyFacadeImpl(Automapper mapper, TopologyRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Topology byId(long id) {
        return repository.getById(id);
    }

    @Override
    public Topology create(Topology topology) {
        repository.create(topology);
        return topology;
    }

    @Override
    public Topology update(Topology topology) {
        repository.update(topology);
        return topology;
    }

    @Override
    public Topology delete(Topology topology) {
        repository.delete(topology);
        return topology;
    }

    @Override
    public List<Topology> findAll() {
        return repository.findAll();
    }
}
