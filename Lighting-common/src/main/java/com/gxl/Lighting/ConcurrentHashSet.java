package com.gxl.Lighting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 拓展 ConcurrentHashMap 对象
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {

    private final ConcurrentHashMap<E, Object> map;

    public ConcurrentHashSet(){
        map = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int capacity){
        map = new ConcurrentHashMap<>(capacity);
    }

    private static final Object EMPTY = new Object();

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty(){
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, EMPTY) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == EMPTY;
    }

    @Override
    public void clear() {
        map.clear();
    }

    public List<E> toList(){
        List l = new CopyOnWriteArrayList();
        Iterator<E> iterator = iterator();
        while(iterator.hasNext()){
            l.add(iterator().next());
        }
        return l;
    }
}
