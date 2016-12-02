package com.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.main.context.JsonContext;
import com.main.elasticsearch.ESClient;
import com.main.elasticsearch.ESLocalNodeClient;
import com.main.elasticsearch.ESTransportClient;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.util.Map;

/**
 * Created by zongzesheng on 12/1/16.
 */
public class ElasticSearch {

    private ESClient esClient = new ESTransportClient();

    public void index(String index, String type, String id, String json) {
        IndexRequest indexRequest = new IndexRequest(index, type, id);
        indexRequest.source(json);
        IndexResponse indexResponse = esClient.getClient().index(indexRequest).actionGet();

    }

    public String search(String index, String type, JsonElement jsonElement) {
        try {
            SearchRequestBuilder requestBuilder = esClient.getClient()
                    .prepareSearch(index).setTypes(type);

            if (jsonElement.isJsonObject()) {
                for (JsonElement field : jsonElement.getAsJsonObject().getAsJsonArray("field")) {
                    requestBuilder.addField(field.getAsString());
                }
            } else {
                throw new IllegalArgumentException(jsonElement.getClass().getName()
                        + " cannot use to store search fields and values.");
            }
            SearchResponse response = requestBuilder.setQuery(QueryBuilders.matchAllQuery())
                    .execute().actionGet();
            SearchHit[] hits = response.getHits().getHits();

            JsonObject container = new JsonObject();
            for (SearchHit hit : hits) {
                for (JsonElement field : jsonElement.getAsJsonObject().getAsJsonArray("field")) {
                    final SearchHitField result = hit.field(field.getAsString());
                    if (result == null) {
                        throw new IndexNotFoundException(field + " is not found.");
                    }
                    final String value = result.getValue();
                    if (value == null) {
                        throw new NullPointerException("The result of " + field + " is null.");
                    }
                    container.add(field.getAsString(), JsonContext.parseJson(value));
                }

            }
            return container.toString();
        } catch (IndexNotFoundException | NullPointerException e) {
            return e.getMessage();
        }
    }

    public void shutdown() {
        esClient.shutdown();
    }

}
