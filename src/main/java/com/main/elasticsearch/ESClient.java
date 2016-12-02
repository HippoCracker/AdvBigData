package com.main.elasticsearch;

import org.elasticsearch.client.Client;

/**
 * Created by zongzesheng on 12/1/16.
 */
public interface ESClient {

    Client getClient();

    void shutdown();
}
