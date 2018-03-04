package testframework.testplatform.web.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testframework.testplatform.facade.TestRunFacade;
import testframework.testplatform.facade.dto.TestRunDto;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/testrun")
public class TestRunController {

    @Autowired
    private TestRunFacade testRunFacade;

    @RequestMapping(path = "/fortest/{testId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<TestRunDto> forTest(@PathVariable("testId") long testId) {
        List<TestRunDto> allTestRunsForTest = testRunFacade.getAllTestRunsForTest(testId);
        return allTestRunsForTest;
    }

    @RequestMapping(path = "/getById/{testRunId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public TestRunDto getById(@PathVariable("testRunId") long testRunId) {
        TestRunDto testRun = testRunFacade.getTestRun(testRunId);
        return testRun;
    }


}
