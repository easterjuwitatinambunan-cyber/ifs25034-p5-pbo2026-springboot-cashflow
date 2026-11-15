package org.delcom.ifs25034springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25034springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUseCaseTest {

    private UserRepository users;
    private AuthUseCase auth;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        auth = new AuthUseCase(users, new PasswordHasher());
    }

    @Test
    void registerCreatesUserWithHashedPassword() throws DomainException {
        User u = auth.register("  Andi.B ", "rahasia1", " Andi Budi ");
        assertEquals("andi.b", u.getUsername());
        assertEquals("Andi Budi", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals(new PasswordHasher().hash("rahasia1"), u.getPasswordHash());
        assertNotNull(u.getId());
    }

    @Test
    void registerRejectsInvalidInput() throws DomainException {
        assertThrows(ValidationException.class, () -> auth.register("ab", "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> auth.register("valid", "123", "Nama"));
        assertThrows(ValidationException.class, () -> auth.register("valid", null, "Nama"));
        assertThrows(ValidationException.class, () -> auth.register("valid", "rahasia1", "  "));
        assertThrows(ValidationException.class, () -> auth.register("valid", "rahasia1", null));
        assertThrows(ValidationException.class, () -> auth.register("valid", "rahasia1", "x".repeat(201)));
        auth.register("valid", "rahasia1", "Nama");
        assertThrows(ValidationException.class, () -> auth.register("VALID", "rahasia1", "Nama"));
    }

    @Test
    void loginChecksCredentials() throws DomainException {
        auth.register("budi", "rahasia1", "Budi");
        assertEquals("budi", auth.login(" BUDI ", "rahasia1").getUsername());
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah-salah"));
        assertThrows(ValidationException.class, () -> auth.login("tidak-ada", "rahasia1"));
        assertThrows(ValidationException.class, () -> auth.login("", "rahasia1"));
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
    }

    @Test
    void ensureRootAdminSeedsOnceAndPromotes() throws DomainException {
        User admin = auth.ensureRootAdmin("Admin", "admin");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("admin", admin.getUsername());
        assertEquals(admin.getId(), auth.ensureRootAdmin("admin", "lain").getId());
        assertEquals(1, users.findAll().size());
        assertEquals("admin", auth.login("admin", "admin").getUsername());

        User promoted = auth.register("boss", "rahasia1", "Boss");
        assertEquals(Role.USER, promoted.getRole());
        assertEquals(Role.ADMIN, auth.ensureRootAdmin("boss", "x").getRole());
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("", "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("root", ""));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("root", null));
    }
}
