package me.FurH.CreativeControl.core.cache.soft;

import java.lang.ref.*;

public class CoreSoftValue<V, K> extends SoftReference<V>
{
    private final K key;
    
    public CoreSoftValue(final V value, final K key, final ReferenceQueue<? super V> queue) {
        super(value, queue);
        this.key = key;
    }
    
    public K getKey() {
        return this.key;
    }
    
    public V get() {
        return super.get();
    }
}
