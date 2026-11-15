package org.delcom.ifs25034springbootcashflow.domain.repository;

import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    default User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(null, username, passwordHash, fullName, role, null));
    }
}
