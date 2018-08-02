package testframework.testplatform.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.repository.MeasureRepository;
import testframework.testplatform.mapper.Automapper;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MeasureFacadeImpl implements MeasureFacade {

    private final Automapper mapper;
    private final MeasureRepository repository;

    @Autowired
    public MeasureFacadeImpl(Automapper mapper, MeasureRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Measure byId(long id) {
        return repository.getById(id);
    }

    @Override
    public Measure create(Measure measure) {
        repository.create(measure);
        return measure;
    }

    @Override
    public Measure update(Measure measure) {
        repository.update(measure);
        return measure;
    }

    @Override
    public Measure delete(Measure measure) {
        repository.delete(measure);
        return measure;
    }

    @Override
    public List<Measure> findAll() {
        return repository.findAll();
    }
}
