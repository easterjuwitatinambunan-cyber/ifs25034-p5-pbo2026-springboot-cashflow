package org.delcom.ifs25034springbootcashflow.usecase;

import org.delcom.ifs25034springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25034springbootcashflow.domain.repository.IUserRepository;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Proses bisnis autentikasi: registrasi, login, dan seed administrator. */
public class AuthUseCase {

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NAME_LENGTH = 200;
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9._-]{3,50}$");

    private final IUserRepository users;
    private final PasswordHasher hasher;

    public AuthUseCase(IUserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    public User register(String username, String password, String fullName) throws DomainException {
        String name = normalizeUsername(username);
        if (!USERNAME.matcher(name).matches()) {
            throw new ValidationException("Username 3-50 karakter dan hanya boleh huruf, angka, titik, garis bawah, atau strip");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter");
        }
        String full = fullName == null ? "" : fullName.trim();
        if (full.isEmpty()) {
            throw new ValidationException("Nama lengkap wajib diisi");
        }
        if (full.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Nama lengkap maksimal " + MAX_NAME_LENGTH + " karakter");
        }
        if (users.findByUsername(name).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return users.save(name, hasher.hash(password), full, Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        String name = normalizeUsername(username);
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        Optional<User> found = users.findByUsername(name);
        if (found.isEmpty() || !hasher.matches(password, found.get().getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return found.get();
    }

    /** Membuat administrator dari ROOT_USERNAME/ROOT_PASSWORD bila belum ada. */
    public User ensureRootAdmin(String username, String password) throws DomainException {
        String name = normalizeUsername(username);
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("ROOT_USERNAME dan ROOT_PASSWORD wajib diisi");
        }
        Optional<User> found = users.findByUsername(name);
        if (found.isPresent()) {
            User existing = found.get();
            if (existing.getRole() != Role.ADMIN) {
                existing.setRole(Role.ADMIN);
                return users.save(existing);
            }
            return existing;
        }
        return users.save(name, hasher.hash(password), "Administrator", Role.ADMIN);
    }
}
