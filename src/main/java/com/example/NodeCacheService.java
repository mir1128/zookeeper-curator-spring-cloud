package com.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeCacheService {
    private static final String PATH = "/example/nodeCache";

    private CuratorFramework curatorFramework;
    private NodeCache cache;

    @Autowired
    public NodeCacheService(CuratorFramework curatorFramework) throws Exception {
        this.curatorFramework = curatorFramework;
        cache = new NodeCache(curatorFramework, PATH);
        cache.start();
        cache.getListenable().addListener(() -> {
            if (cache.getCurrentData() != null) {
                System.out.println("Node changed: " + cache.getCurrentData().getPath() + ", value: " + new String(cache.getCurrentData().getData()));
            }
        });
    }


    public String processCommands(Operation operation) throws Exception {
        if (operation.getOperation().equalsIgnoreCase("q") || operation.getOperation().equalsIgnoreCase("quit")) {
            return "quit";
        } else if (operation.getOperation().equals("set")) {
            setValue(curatorFramework, operation.getCommand(), operation.getArgs().toArray(new String[0]));
        } else if (operation.equals("remove")) {
            remove(curatorFramework);
        } else if (operation.equals("list")) {
            show(cache);
        }

        return "success";
    }

    private static void show(NodeCache cache) {
        if (cache.getCurrentData() != null)
            System.out.println(cache.getCurrentData().getPath() + " = " + new String(cache.getCurrentData().getData()));
        else
            System.out.println("cache don't set a value");
    }

    private static void remove(CuratorFramework client) throws Exception {
        try {
            client.delete().forPath(PATH);
        } catch (KeeperException.NoNodeException e) {
            // ignore
        }
    }

    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("syntax error (expected set <value>): " + command);
            return;
        }
        byte[] bytes = args[0].getBytes();
        try {
            client.setData().forPath(PATH, bytes);
        } catch (KeeperException.NoNodeException e) {
            client.create().creatingParentsIfNeeded().forPath(PATH, bytes);
        }
    }
}
