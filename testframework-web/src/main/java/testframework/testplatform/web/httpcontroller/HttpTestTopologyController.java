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

import guru.nidi.graphviz.model.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.dal.repository.TopologyRepository;
import testframework.testplatform.facade.TestFacade;
import testframework.testplatform.facade.TopologyFacade;
import testframework.testplatform.facade.dto.TestDto;
import testframework.testplatform.services.DotGeneratorService;
import testframework.testplatform.services.TestService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Controller
@RequestMapping(value = "/java/test/topology")
public class HttpTestTopologyController {

    @Autowired
    private TestFacade testFacade;

    @Autowired
    private TestService testService;

    @Autowired
    private TopologyFacade facade;

    @Autowired
    private TopologyRepository topologyRepository;

    @Autowired
    private DotGeneratorService generatorService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @RequestMapping(value = "/{testId}", method = RequestMethod.GET)
    public String displayTopology(@PathVariable(value = "testId") long id, Model model) throws Exception {
        System.err.println(">>>>generating svg");
        TestDto test = testFacade.findById(id);
        Topology topology = topologyRepository.getById(test.getTopology().getId());
        Graph graph = generatorService.generateGraph(topology);

        Future<?> future = executor.submit(() -> {
            String svg = generatorService.graphToSvg(graph);
            model.addAttribute("svg", svg);
            System.err.println(">>>>generated svg");
        });
        future.get();
        return "TestTopology";
    }
}
