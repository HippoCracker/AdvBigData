package com.main.cache;

public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);

    void remove(K key);
}
