package com.main.cache;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache<K, V> implements Cache<K, V> {

    private final ReentrantLock lock = new ReentrantLock();

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final Deque<K> queue = new LinkedList<K>();
    private final int limit;

    public LRUCache(int limit) {
        this.limit = limit;
    }

    @Override
    public V get(K key) {
        V value = map.get(key);
        if (value != null) {
            removeThenAddKey(key);
        }
        return value;
    }

    @Override
    public V put(K key, V value) {
        V oldValue = map.put(key, value);
        if (oldValue != null) {
            removeThenAddKey(key);
        } else {
            addKey(key);
        }
        if (map.size() > limit) {
            return map.remove(removeLast());
        }
        return null;
    }

    @Override
    public V remove(K key) {
        removeFirstOccurrence(key);
        return map.remove(key);
    }

    private void addKey(K key) {
        lock.lock();
        try {
            queue.addFirst(key);
        } finally {
            lock.unlock();
        }
    }

    private K removeLast() {
        lock.lock();
        try {
            final K removedKey = queue.removeLast();
            return removedKey;
        } finally {
            lock.unlock();
        }
    }

    private void removeThenAddKey(K key) {
        lock.lock();
        try {
            queue.removeFirstOccurrence(key);
            queue.addFirst(key);
        } finally {
            lock.unlock();
        }
    }

    private void removeFirstOccurrence(K key) {
        lock.lock();
        try {
            queue.removeFirstOccurrence(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return map.toString();
    }


}
