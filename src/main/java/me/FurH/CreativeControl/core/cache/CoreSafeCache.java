package me.FurH.CreativeControl.core.cache;

import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import sun.misc.*;
import java.util.concurrent.locks.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

public class CoreSafeCache<K, V> extends ConcurrentHashMap<K, V>
{
    private static final long serialVersionUID = 426161011525380934L;
    private int capacity;
    private int size;
    private int reads;
    private int writes;
    
    public CoreSafeCache(final int cacheSize) {
        super(cacheSize, 0.75f);
        this.capacity = -1;
        this.size = 0;
        this.reads = 0;
        this.writes = 0;
        this.capacity = cacheSize;
    }
    
    public CoreSafeCache() {
        super();
        this.capacity = -1;
        this.size = 0;
        this.reads = 0;
        this.writes = 0;
    }
    
    public V get(final Object key) {
        ++this.reads;
        return super.get(key);
    }
    
    public V put(final K key, final V value) {
        ++this.writes;
        if (this.containsKey(key)) {
            return super.replace(key, value);
        }
        ++this.size;
        if (this.capacity != -1 && this.size > this.capacity) {
            super.clear();
        }
        return super.put(key, value);
    }
    
    public V remove(final Object key) {
        --this.size;
        return super.remove(key);
    }
    
    public K getKey(final V value) {
        for (final K key : this.keySet()) {
            if (this.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }
    
    public K removeValue(final V value) {
        final K key = this.getKey(value);
        if (key != null) {
            this.remove(key);
        }
        return key;
    }
    
    public boolean containsValue(final Object value) {
        ++this.reads;
        return super.containsValue(value);
    }
    
    public boolean containsKey(final Object key) {
        ++this.reads;
        return super.containsKey(key);
    }
    
    public void clear() {
        super.clear();
    }
    
    public int getReads() {
        return this.reads;
    }
    
    public int getWrites() {
        return this.writes;
    }
    
    public int getMaxSize() {
        return this.capacity;
    }
}
