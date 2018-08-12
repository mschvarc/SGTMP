package testframework.testplatform.facade;

import testframework.testplatform.dal.entities.measure.Measure;

import java.util.List;

public interface MeasureFacade {
    Measure byId(long id);

    Measure create(Measure measure);

    Measure update(Measure topology);

    Measure delete(Measure topology);

    List<Measure> findAll();
}
