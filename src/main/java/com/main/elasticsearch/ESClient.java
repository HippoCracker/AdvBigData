package com.main.elasticsearch;

import org.elasticsearch.client.Client;

public interface ESClient {

    Client getClient();

    void shutdown();
}
