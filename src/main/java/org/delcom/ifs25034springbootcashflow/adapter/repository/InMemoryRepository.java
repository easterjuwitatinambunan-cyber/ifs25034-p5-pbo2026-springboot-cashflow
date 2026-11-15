package org.delcom.ifs25034springbootcashflow.adapter.repository;

import org.delcom.ifs25034springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Basis repository generik berbasis memori, dipakai untuk unit test. */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {

    private final Map<Integer, T> store = new LinkedHashMap<>();
    private int sequence = 0;

    protected abstract Integer idOf(T entity);

    protected abstract void assignId(T entity, int id);

    @Override
    public synchronized T save(T entity) {
        if (idOf(entity) == null) {
            assignId(entity, ++sequence);
        } else {
            sequence = Math.max(sequence, idOf(entity));
        }
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
