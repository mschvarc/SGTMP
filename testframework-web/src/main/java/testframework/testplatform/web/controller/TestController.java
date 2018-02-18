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

package testframework.testplatform.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import testframework.testplatform.facade.TestFacade;
import testframework.testplatform.facade.TestRunFacade;
import testframework.testplatform.facade.dto.TestDto;
import testframework.testplatform.facade.dto.TestRunDto;
import testframework.testplatform.web.models.RunTest;
import testframework.testplatform.web.models.TestRunsModel;
import testframework.testplatform.web.models.TestUpload;
import testframework.testplatform.web.util.storage.StorageFileNotFoundException;
import testframework.testplatform.web.util.storage.StorageService;
import testframework.testplatform.web.util.storage.TestUploader;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Controller
@RequestMapping(value = "/test")
public class TestController {

    private static final String FS = File.separator;
    private final StorageService storageService;
    private final TestUploader testUploader;
    private final TestFacade testFacade;
    private final TestRunFacade testRunFacade;


    @Autowired
    public TestController(StorageService storageService, TestUploader testUploader, TestFacade testFacade, TestRunFacade testRunFacade) {
        this.storageService = storageService;
        this.testUploader = testUploader;
        this.testFacade = testFacade;
        this.testRunFacade = testRunFacade;
    }


    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String listUploadedFiles(Model model) {

        model.addAttribute("TestUpload", new TestUpload());

        return "CreateTest";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String listTests(Model model) {
        RunTest runTest = new RunTest();
        runTest.setTests(testFacade.getAllTests());
        model.addAttribute("RunTest", runTest);
        return "RunTest";
    }

    @RequestMapping(value = "/run/{testID}", method = RequestMethod.GET)
    public String runTest(@PathVariable(value = "testID") String id, Model model) throws IOException {

        testFacade.runTest(id);

        model.addAttribute("message", "Started test ID: " + id);

        return "redirect:/test/";
    }

    @RequestMapping(value = "/view/{testID}", method = RequestMethod.GET)
    public String viewTest(@PathVariable(value = "testID") long id, Model model) {

        List<TestRunDto> allTestRunsForTest = testRunFacade.getAllTestRunsForTest(id);
        TestRunsModel testRuns = new TestRunsModel();
        testRuns.setTestRuns(allTestRunsForTest);
        model.addAttribute("TestRunsModel", testRuns);
        return "ViewTestRunsForTest";
    }

    @RequestMapping(value = "/files/{filename:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String handleFileUpload(@Valid @ModelAttribute TestUpload model, BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors() || !model.isValid()) {
            redirectAttributes.addFlashAttribute("message", "Validation failed ");
            return "redirect:/test/";
        }

        TestDto test = testUploader.createSkeletonTest();
        Path testConfigPath = Paths.get(test.getUuid() + FS + "config" + FS + "user" + FS);

        storageService.store(model.getSimulationConfig(), testConfigPath.resolve("simulation_config.xml"));
        storageService.store(model.getSimulators(), testConfigPath.resolve("simulators.xml"));
        storageService.store(model.getModels(), testConfigPath.resolve("models.xml"));
        storageService.store(model.getWires(), testConfigPath.resolve("wires.xml"));
        storageService.store(model.getTestConfig(), testConfigPath.resolve("config.xml"));


        testUploader.createTest(model, test);

        redirectAttributes.addFlashAttribute("message", "Created new Test: " + test.getId());

        return "redirect:/test/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
