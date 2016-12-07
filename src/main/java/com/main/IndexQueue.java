package com.main;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;


public class IndexQueue<E extends Json> implements Runnable {

    private final Queue<E> requestQueue;
    private final Deque<E> sentRequestQueue;
    private int limit;
    private ElasticSearch elasticSearch;

    public IndexQueue(int limit, ElasticSearch elasticSearch) {
        this.requestQueue = new LinkedList<>();
        this.sentRequestQueue = new LinkedList<>();
        this.limit = limit;
        this.elasticSearch = elasticSearch;
    }

    public void offer(E e) {
        requestQueue.add(e);
    }

    public E poll() {
        E element = requestQueue.poll();

        if (sentRequestQueue.size() >= limit) {
            E noResponseRequest = sentRequestQueue.poll();
            requestQueue.offer(noResponseRequest);
        }
        sentRequestQueue.offer(element);
        return element;
    }

    public void indexFinished(E element) {
        sentRequestQueue.removeFirstOccurrence(element);
    }

    @Override
    public void run() {
        if (requestQueue.size() > 0) {
            elasticSearch.index(poll());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
