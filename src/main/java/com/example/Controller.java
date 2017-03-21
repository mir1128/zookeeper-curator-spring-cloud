package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    PathChildrenCacheService pathChildrenCacheService;

    @Autowired
    NodeCacheService nodeCacheService;

    @RequestMapping(path = "/path", method = RequestMethod.POST)
    public String path(@RequestBody Operation operation) throws Exception {

        pathChildrenCacheService.processCommands(operation);
        return "";
    }

    @RequestMapping(path = "/node", method = RequestMethod.POST)
    public String node(@RequestBody Operation operation) throws Exception {
        nodeCacheService.processCommands(operation);
        return "";
    }
}
