package me.FurH.CreativeControl.core.cache;

import java.util.*;

public class CoreLRUCache<K, V> extends LinkedHashMap<K, V>
{
    private static final long serialVersionUID = -80132122077195160L;
    private int capacity;
    private int reads;
    private int writes;
    
    public CoreLRUCache(final int cacheSize) {
        super(cacheSize, 0.75f, true);
        this.capacity = 0;
        this.reads = 0;
        this.writes = 0;
        this.capacity = cacheSize;
    }
    
    public CoreLRUCache() {
        super();
        this.capacity = 0;
        this.reads = 0;
        this.writes = 0;
        this.capacity = 0;
    }
    
    public V get(final Object key) {
        ++this.reads;
        return super.get(key);
    }
    
    public V put(final K key, final V value) {
        ++this.writes;
        return super.put(key, value);
    }
    
    public K getKey(final V value) {
        K ret = null;
        final List<K> keys = new ArrayList<K>((Collection<? extends K>)this.keySet());
        for (final K key : keys) {
            if (this.get(key).equals(value)) {
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
    
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return this.capacity > 0 && this.size() > this.capacity;
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
