package com.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PathChildrenCacheService {
    private static final String PATH = "/example/cache";

    @Autowired
    private CuratorFramework curatorFramework;

    private PathChildrenCache cache;

    @Autowired
    public PathChildrenCacheService(CuratorFramework curatorFramework) throws Exception {
        this.curatorFramework = curatorFramework;
        cache = new PathChildrenCache(curatorFramework, PATH, true);
        cache.start();
        addListener(cache);
    }

    public String processCommands(Operation operation) throws Exception {
        if (operation.getOperation().equalsIgnoreCase("help") || operation.getOperation().equalsIgnoreCase("?")) {
            help();
        } else if (operation.getOperation().equalsIgnoreCase("q") || operation.getOperation().equalsIgnoreCase("quit")) {
            return "quit";
        } else if (operation.getOperation().equals("set")) {
            setValue(curatorFramework, operation.getCommand(), (String[]) operation.getArgs().toArray(new String[0]));
        } else if (operation.equals("remove")) {
            remove(curatorFramework, operation.getCommand(), (String[]) operation.getArgs().toArray());
        } else if (operation.equals("list")) {
            list(cache);
        }

        return "success";
    }

    private static void list(PathChildrenCache cache) {
        if (cache.getCurrentData().size() == 0) {
            System.out.println("* empty *");
        } else {
            for (ChildData data : cache.getCurrentData()) {
                System.out.println(data.getPath() + " = " + new String(data.getData()));
            }
        }
    }

    private void remove(CuratorFramework client, String command, String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("syntax error (expected remove <path>): " + command);
            return;
        }

        String name = args[0];
        if (name.contains("/")) {
            System.err.println("Invalid node name" + name);
            return;
        }
        String path = ZKPaths.makePath(PATH, name);

        try {
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            // ignore
        }
    }

    private void setValue(CuratorFramework client, String command, String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("syntax error (expected set <path> <value>): " + command);
            return;
        }

        String name = args[0];
        if (name.contains("/")) {
            System.err.println("Invalid node name" + name);
            return;
        }
        String path = ZKPaths.makePath(PATH, name);

        byte[] bytes = args[1].getBytes();

        try {
            client.setData().forPath(path, bytes);
        } catch (KeeperException.NoNodeException e) {
            client.create().creatingParentContainersIfNeeded().forPath(path, bytes);
        }
    }

    private String help() {
        StringBuilder stringBuilder  = new StringBuilder();
        stringBuilder.append("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:\n");
        stringBuilder.append("set <name> <value>: Adds or updates a node with the given name");
        stringBuilder.append("remove <name>: Deletes the node with the given name");
        stringBuilder.append("list: List the nodes/values in the cache");
        stringBuilder.append("quit: Quit the example");
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private void addListener(PathChildrenCache cache) {
        PathChildrenCacheListener listener = (client1, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED: {
                    System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }

                case CHILD_UPDATED: {
                    System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    System.out.println("Node changed: " + new String(event.getData().getData()));
                    break;
                }

                case CHILD_REMOVED: {
                    System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
            }
        };
        cache.getListenable().addListener(listener);
    }
}
