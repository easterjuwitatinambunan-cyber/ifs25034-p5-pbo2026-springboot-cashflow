package org.delcom.ifs25034springbootcashflow.adapter.repository;

import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected Integer idOf(User entity) {
        return entity.getId();
    }

    @Override
    protected void assignId(User entity, int id) {
        entity.setId(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
