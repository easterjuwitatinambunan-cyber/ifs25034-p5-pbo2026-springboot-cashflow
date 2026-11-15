package org.delcom.ifs25034springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port generik CRUD. save() menyisipkan entitas baru (ID null) atau memperbarui yang sudah ada. */
public interface IRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);

    default List<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}
