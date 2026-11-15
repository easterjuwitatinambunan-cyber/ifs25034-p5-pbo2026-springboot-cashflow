package org.delcom.ifs25034springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25034springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCaseTest {

    private UserUseCase useCase;
    private User andi;
    private User admin;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        PasswordHasher hasher = new PasswordHasher();
        useCase = new UserUseCase(users, hasher);
        andi = users.save("andi", hasher.hash("lama123"), "Andi", Role.USER);
        admin = users.save("admin", "h", "Admin", Role.ADMIN);
    }

    @Test
    void onlyAdminListsUsers() throws DomainException {
        assertEquals(2, useCase.listUsers(admin).size());
        assertThrows(ValidationException.class, () -> useCase.listUsers(andi));
        assertThrows(ValidationException.class, () -> useCase.listUsers(null));
    }

    @Test
    void getAndFind() throws DomainException {
        assertEquals("andi", useCase.getUser(andi.getId()).getUsername());
        assertThrows(EntityNotFoundException.class, () -> useCase.getUser(404));
        assertTrue(useCase.find(andi.getId()).isPresent());
        assertTrue(useCase.find(404).isEmpty());
    }

    @Test
    void updateProfileChangesNameAndOptionalPassword() throws DomainException {
        User u = useCase.updateProfile(andi.getId(), " Andi Baru ", "");
        assertEquals("Andi Baru", u.getFullName());
        assertEquals(new PasswordHasher().hash("lama123"), u.getPasswordHash());
        u = useCase.updateProfile(andi.getId(), "Andi Baru", null);
        assertEquals(new PasswordHasher().hash("lama123"), u.getPasswordHash());
        u = useCase.updateProfile(andi.getId(), "Andi Baru", "baru1234");
        assertEquals(new PasswordHasher().hash("baru1234"), u.getPasswordHash());
        assertThrows(ValidationException.class, () -> useCase.updateProfile(andi.getId(), " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(andi.getId(), null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(andi.getId(), "x".repeat(201), null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(andi.getId(), "Andi", "123"));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(404, "Andi", null));
    }

    @Test
    void updatePhotoStoresPath() throws DomainException {
        assertEquals("foto.png", useCase.updatePhoto(andi.getId(), "foto.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(404, "x.png"));
    }
}
