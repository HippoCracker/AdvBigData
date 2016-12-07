package com.main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.main.context.JsonContext;
import com.main.elasticsearch.ESClient;
import com.main.elasticsearch.ESTransportClient;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.main.common.Utils.ID;
import static com.main.common.Utils.NAME;
import static com.main.common.Utils.TYPE;


public class ElasticSearch<E extends Json> {

    private ESClient esClient = new ESTransportClient();

    public boolean index(E e) {
        String index = e.getAsString(NAME);
        String type = e.getAsString(TYPE);
        String id = e.getAsString(ID);
        String json = e.jsonString();
        IndexRequest indexRequest = new IndexRequest(index, type, id);
        indexRequest.source(json);
        IndexResponse indexResponse = esClient.getClient().index(indexRequest).actionGet();
        System.out.println("ElasticSearch Indexed type: " + type + " id: " + id);
        return indexResponse.isCreated();
    }

    public Iterable<GetField> get(E e) {
        String index = e.getAsString(NAME);
        String type = e.getAsString(TYPE);
        String id = e.getAsString(ID);
        GetResponse response = esClient.getClient().prepareGet(index, type, id).get();
        System.out.println("ElasticSearch Get type: " + type + " id: " + id);
        return response;
    }

    public boolean delete(E e) {
        String index = e.getAsString(NAME);
        String type = e.getAsString(TYPE);
        String id = e.getAsString(ID);
        DeleteResponse response = esClient.getClient().prepareDelete(index, type, id).get();
        System.out.println("ElasticSearch Delete type: " + type + " id: " + id);
        return response.isFound();
    }

    public void update(E e) {
        String index = e.getAsString(NAME);
        String type = e.getAsString(TYPE);
        String id = e.getAsString(ID);
        String json = e.jsonString();

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("index");
        updateRequest.type("type");
        updateRequest.id("1");
        updateRequest.doc(json);
        try {
            esClient.getClient().update(updateRequest).get();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }

    public String search(String index, String type, JsonElement jsonElement) {
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
        for (JsonElement field : jsonElement.getAsJsonObject().getAsJsonArray("field")) {
            JsonArray array = new JsonArray();
            for (SearchHit hit : hits) {
                final SearchHitField result = hit.field(field.getAsString());
                if (result == null) {
                    continue;
                }
                final JsonElement value = JsonContext.parseJson(result.getValue());
                if (!array.contains(value)) {
                    array.add(value);
                }
            }
            container.add(field.getAsString(), array);
        }
        return container.toString();
    }

    public void shutdown() {
        esClient.shutdown();
    }

}
