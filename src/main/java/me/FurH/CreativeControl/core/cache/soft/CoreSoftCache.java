package me.FurH.CreativeControl.core.cache.soft;

import java.lang.ref.*;
import java.util.*;

public class CoreSoftCache<K, V>
{
    private static final long serialVersionUID = -80132122077195160L;
    private final ReferenceQueue<V> queue;
    private final LinkedHashMap<K, CoreSoftValue<V, K>> map;
    private int capacity;
    private int reads;
    private int writes;
    
    public CoreSoftCache(final int cacheSize) {
        super();
        this.queue = new ReferenceQueue<V>();
        this.capacity = 0;
        this.reads = 0;
        this.writes = 0;
        this.map = new LinkedHashMap<K, CoreSoftValue<V, K>>(cacheSize, 0.75f, true) {
            private static final long serialVersionUID = 2674509550119308224L;
            
            protected boolean removeEldestEntry(final Map.Entry<K, CoreSoftValue<V, K>> eldest) {
                return CoreSoftCache.this.capacity > 0 && this.size() > CoreSoftCache.this.capacity;
            }
        };
        this.capacity = cacheSize;
    }
    
    public CoreSoftCache() {
        this(0);
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
        final CoreSoftValue<V, K> soft = new CoreSoftValue<V, K>(value, key, this.queue);
        this.map.put(key, soft);
        return soft.get();
    }
    
    public K getKey(final V value) {
        K ret = null;
        final List<K> keys = new ArrayList<K>((Collection<? extends K>)this.map.keySet());
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
                CoreSoftCache.this.cleanup();
            }
        }, delay);
    }
    
    public LinkedHashMap<K, CoreSoftValue<V, K>> getHandle() {
        return this.map;
    }
}
