/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.testplatform.web.httpcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import testframework.testplatform.facade.TestRunFacade;
import testframework.testplatform.facade.dto.TestRunDto;


@Controller
@RequestMapping(value = "/java/testrun")
public class HttpTestRunController {

    private final TestRunFacade testRunFacade;


    @Autowired
    public HttpTestRunController(TestRunFacade testRunFacade) {
        this.testRunFacade = testRunFacade;
    }

    @RequestMapping(value = "/runnow/{testRunID}", method = RequestMethod.GET)
    public String runTestNow(@PathVariable(value = "testRunID") long id, Model model) {
        testRunFacade.startTestRun(id);
        return "redirect:/java/testrun/view/" + id;
    }

    @RequestMapping(value = "/view/{testRunID}", method = RequestMethod.GET)
    public String viewTestRunDetails(@PathVariable(value = "testRunID") long id, Model model) {
        TestRunDto testRun = testRunFacade.getTestRun(id);
        if (testRun == null) {
            return "redirect:/java/test/";
        }
        model.addAttribute("testRun", testRun);
        return "ViewTestRunDetails";
    }

}
