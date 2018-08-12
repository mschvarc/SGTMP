package testframework.testplatform.web.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testframework.testplatform.facade.TestFacade;
import testframework.testplatform.facade.dto.TestDto;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    TestFacade testFacade;

    @RequestMapping(path = "/findAll", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<TestDto> findAll() {
        return testFacade.findAll();
    }

    @RequestMapping(path = "/find/{testId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public TestDto findById(@PathVariable("testId") long testId) {
        TestDto testDto = testFacade.findById(testId);
        return testDto;
    }

    @RequestMapping(path = "/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public TestDto createTest(@RequestBody TestDto test) {
        TestDto testDto = testFacade.create(test);
        return testDto;
    }

    @RequestMapping(path = "/update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public TestDto updateTest(@RequestBody TestDto test) {
        TestDto testDto = testFacade.update(test);
        return testDto;
    }

    @RequestMapping(path = "/delete", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public TestDto deleteTest(@RequestBody TestDto test) {
        TestDto testDto = testFacade.delete(test);
        return testDto;
    }

}
