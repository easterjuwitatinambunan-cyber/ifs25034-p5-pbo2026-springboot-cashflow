package org.delcom.ifs25034springbootcashflow.adapter.storage;

import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Menyimpan gambar unggahan (bukti transaksi dan foto profil) ke UPLOAD_DIR dengan nama UUID. */
public class FileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final Pattern STORED_NAME =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(png|jpg|jpeg|gif|webp)$");

    private final Path root;

    public FileStorage(String uploadDir) {
        try {
            this.root = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat membuat folder upload: " + uploadDir, e);
        }
    }

    public Path root() {
        return root;
    }

    /** Mengembalikan nama file tersimpan (UUID + ekstensi). */
    public String save(String originalName, byte[] data) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran file maksimal 5 MB");
        }
        String extension = extensionOf(originalName);
        if (!EXTENSIONS.contains(extension)) {
            throw new ValidationException("Format file harus PNG, JPG, GIF, atau WEBP");
        }
        if (!hasImageSignature(extension, data)) {
            throw new ValidationException("Isi file bukan gambar yang valid");
        }
        String stored = UUID.randomUUID() + "." + extension;
        try {
            Files.write(root.resolve(stored), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menyimpan file", e);
        }
        return stored;
    }

    public boolean exists(String storedName) {
        return isStoredName(storedName) && Files.exists(root.resolve(storedName));
    }

    public boolean delete(String storedName) {
        if (!isStoredName(storedName)) {
            return false;
        }
        try {
            return Files.deleteIfExists(root.resolve(storedName));
        } catch (IOException e) {
            return false;
        }
    }

    /** URL publik file (dilayani /uploads/**), null jika tidak ada file. */
    public static String url(String storedName) {
        return storedName == null || storedName.isBlank() ? null : "/uploads/" + storedName;
    }

    private static boolean isStoredName(String name) {
        return name != null && STORED_NAME.matcher(name).matches();
    }

    private static String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean hasImageSignature(String extension, byte[] d) {
        return switch (extension) {
            case "png" -> d.length > 4 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G';
            case "jpg", "jpeg" -> d.length > 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF;
            case "gif" -> d.length > 4 && d[0] == 'G' && d[1] == 'I' && d[2] == 'F' && d[3] == '8';
            default -> d.length > 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                    && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P';
        };
    }
}
