package testframework.testplatform.web.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.facade.TopologyFacade;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/topology")
public class TopologyController {

    @Autowired
    TopologyFacade facade;

    @RequestMapping(path = "/findById/{topologyId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public Topology findById(@PathVariable("topologyId") long topologyId) {
        Topology result = facade.byId(topologyId);
        return result;
    }

    @RequestMapping(path = "/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Topology createTopology(Topology topology) {
        Topology result = facade.create(topology);
        return result;
    }

    @RequestMapping(path = "/update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Topology updateTopology(Topology topology) {
        Topology result = facade.update(topology);
        return result;
    }

    @RequestMapping(path = "/delete", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Topology deleteTopology(Topology topology) {
        Topology result = facade.delete(topology);
        return result;
    }

}
