package me.FurH.CreativeControl.core.cache.soft;

import java.lang.ref.*;
import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
//import sun.misc.*;
import java.util.concurrent.locks.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

public class CoreSoftSafeCache<K, V>
{
    private static final long serialVersionUID = 426161011525380934L;
    private final ReferenceQueue<V> queue;
    private ConcurrentHashMap<K, CoreSoftValue<V, K>> map;
    private int capacity;
    private int size;
    private int reads;
    private int writes;
    
    public CoreSoftSafeCache(final int cacheSize) {
        super();
        this.queue = new ReferenceQueue<V>();
        this.capacity = -1;
        this.size = 0;
        this.reads = 0;
        this.writes = 0;
        this.map = new ConcurrentHashMap<K, CoreSoftValue<V, K>>(cacheSize, 0.75f);
        this.capacity = cacheSize;
    }
    
    public CoreSoftSafeCache() {
        super();
        this.queue = new ReferenceQueue<V>();
        this.capacity = -1;
        this.size = 0;
        this.reads = 0;
        this.writes = 0;
        this.map = new ConcurrentHashMap<K, CoreSoftValue<V, K>>();
    }
    
    public V get(final K key) {
        ++this.reads;
        final CoreSoftValue<V, K> soft = this.map.get(key);
        if (soft != null) {
            final V result = soft.get();
            if (result == null) {
                this.map.remove(key);
                this.cleanup();
            }
            return result;
        }
        this.map.remove(key);
        return null;
    }
    
    public V put(final K key, final V value) {
        ++this.writes;
        CoreSoftValue<V, K> soft = null;
        if (this.containsKey(key)) {
            soft = new CoreSoftValue<V, K>(value, key, this.queue);
            this.map.replace(key, soft);
            return soft.get();
        }
        ++this.size;
        if (this.capacity > 0 && this.size > this.capacity) {
            try {
                this.map.remove(this.map.keySet().iterator().next());
            }
            catch (Throwable ex) {
                this.map.clear();
                System.out.println("ERR: " + ex.getMessage());
            }
        }
        soft = new CoreSoftValue<V, K>(value, key, this.queue);
        this.map.put(key, soft);
        return soft.get();
    }
    
    public K getKey(final V value) {
        K ret = null;
        final List<K> keys = new ArrayList<K>(this.map.keySet());
        for (final K key : keys) {
            final V get = this.get(key);
            if (get == null) {
                continue;
            }
            if (get.equals(value)) {
                ret = key;
                break;
            }
        }
        keys.clear();
        return ret;
    }
    
    public K removeValue(final V value) {
        final K key = this.getKey(value);
        if (key != null) {
            this.remove(key);
        }
        return key;
    }
    
    public V remove(final K key) {
        ++this.writes;
        ++this.reads;
        final CoreSoftValue<V, K> ret = this.map.remove(key);
        if (ret == null) {
            return null;
        }
        return ret.get();
    }
    
    public boolean containsKey(final K key) {
        ++this.reads;
        return this.map.containsKey(key);
    }
    
    public void clear() {
        this.map.clear();
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
    
    public int size() {
        this.cleanup();
        return this.map.size();
    }
    
    public void cleanup() {
        CoreSoftValue<V, K> sv;
        while ((sv = (CoreSoftValue<V, K>)(CoreSoftValue)this.queue.poll()) != null) {
            this.remove(sv.getKey());
        }
    }
    
    public void cleanupTask() {
        this.cleanupTask(60000L);
    }
    
    public void cleanupTask(final long delay) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                CoreSoftSafeCache.this.cleanup();
            }
        }, delay);
    }
    
    public ConcurrentHashMap<K, CoreSoftValue<V, K>> getHandle() {
        return this.map;
    }
}
