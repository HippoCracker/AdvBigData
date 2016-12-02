package com.main.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by zongzesheng on 12/1/16.
 */
public class ESTransportClient implements ESClient {

    private TransportClient client;

    public ESTransportClient() {
        try {
            client = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to connect ElasticSearch host: localhost", e);
        }
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void shutdown() {
        client.close();
        client = null;
    }
}
