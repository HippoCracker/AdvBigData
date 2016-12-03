package com.main.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class ESLocalNodeClient implements ESClient {

    private Node node;

    public ESLocalNodeClient() {
        this.node = new NodeBuilder()
                .client(true).build().start();
    }

    @Override
    public Client getClient() {
        return node.client();
    }

    @Override
    public void shutdown() {
        getClient().close();
        node.close();
        node = null;
    }
}
