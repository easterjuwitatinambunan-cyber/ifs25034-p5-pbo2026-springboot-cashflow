package org.delcom.ifs25034springbootcashflow.usecase;

import org.delcom.ifs25034springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25034springbootcashflow.domain.repository.IUserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Proses bisnis pengguna: daftar (ADMIN), profil, password, foto. */
public class UserUseCase {

    private final IUserRepository users;
    private final PasswordHasher hasher;

    public UserUseCase(IUserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public List<User> listUsers(User actor) throws DomainException {
        if (actor == null || !actor.isAdmin()) {
            throw new ValidationException("Hanya ADMIN yang dapat melihat daftar pengguna");
        }
        return users.findAll().stream().sorted(Comparator.comparing(User::getId)).toList();
    }

    public User getUser(int id) throws DomainException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    public Optional<User> find(int id) {
        return users.findById(id);
    }

    /** newPassword boleh kosong: password lama dipertahankan. */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = getUser(id);
        String name = fullName == null ? "" : fullName.trim();
        if (name.isEmpty()) {
            throw new ValidationException("Nama lengkap wajib diisi");
        }
        if (name.length() > AuthUseCase.MAX_NAME_LENGTH) {
            throw new ValidationException("Nama lengkap maksimal " + AuthUseCase.MAX_NAME_LENGTH + " karakter");
        }
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        if (changePassword && newPassword.length() < AuthUseCase.MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");
        }
        user.setFullName(name);
        if (changePassword) {
            user.setPasswordHash(hasher.hash(newPassword));
        }
        return users.save(user);
    }

    public User updatePhoto(int id, String photoPath) throws DomainException {
        User user = getUser(id);
        user.setPhotoPath(photoPath);
        return users.save(user);
    }
}
