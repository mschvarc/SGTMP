package testframework.testplatform.web.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.facade.MeasureFacade;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/measure")
public class MeasureController {

    @Autowired
    MeasureFacade facade;

    @RequestMapping(path = "/findAll", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<Measure> findAll() {
        return facade.findAll();
    }

    @RequestMapping(path = "/findById/{measureId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public Measure findById(@PathVariable("measureId") long measureId) {
        Measure result = facade.byId(measureId);
        return result;
    }

    @RequestMapping(path = "/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Measure createMeasure(@RequestBody Measure measure) {
        Measure result = facade.create(measure);
        return result;
    }

    @RequestMapping(path = "/update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Measure updateMeasure(@RequestBody Measure measure) {
        Measure result = facade.update(measure);
        return result;
    }

    @RequestMapping(path = "/delete", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Measure deleteMeasure(@RequestBody Measure measure) {
        Measure result = facade.delete(measure);
        return result;
    }


}
