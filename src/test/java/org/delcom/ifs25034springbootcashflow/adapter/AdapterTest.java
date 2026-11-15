package org.delcom.ifs25034springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.*;
import org.delcom.ifs25034springbootcashflow.domain.exception.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AdapterTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

    @Test
    void presenterFormatsMoneyAndMessages() {
        FinancePresenter p = new FinancePresenter();
        Transaction t = new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 3, 5, 14, 30));
        assertEquals("Rp 1,000,000.00", p.money(1_000_000));
        assertEquals("Rp 0.00", p.money(0));
        assertEquals("-Rp 1,500.00", p.money(-1500));
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.line(t));
        assertEquals("Saldo: Rp 4,975,000.00", p.balanceLine(4_975_000));
        assertTrue(p.dateTime(t.getCreatedAt()).contains("2026"));
        assertEquals("-", p.dateTime(null));
        assertTrue(p.addedMessage(t).contains("berhasil ditambahkan"));
        assertTrue(p.deletedMessage(t).contains("berhasil dihapus"));
        assertEquals("Profil berhasil diperbarui", p.profileUpdatedMessage());
        assertEquals("oops", p.errorMessage(new ValidationException("oops")));
    }

    @Test
    void inMemoryRepositoriesAssignIdsAndQuery() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "a", 10, TransactionType.INCOME, null);
        Transaction b = repo.save(2, "b", 20, TransactionType.EXPENSE, null);
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(2, repo.findAll().size());
        assertTrue(repo.findById(2).isPresent());
        assertTrue(repo.deleteById(2));
        assertFalse(repo.deleteById(2));
        assertTrue(repo.findById(2).isEmpty());
        repo.save(new Transaction(10, 1, "c", 5, TransactionType.INCOME, null, LocalDateTime.now()));
        assertEquals(11, repo.save(1, "d", 1, TransactionType.INCOME, null).getId());

        UserRepository users = new UserRepository();
        User u = users.save("andi", "h", "Andi", Role.USER);
        assertEquals(u, users.findByUsername("andi").orElseThrow());
        assertTrue(users.findByUsername("nope").isEmpty());
    }

    @Test
    void fileStorageSavesServesAndDeletes() throws IOException, ValidationException {
        Path dir = Files.createTempDirectory("cashflow-uploads");
        FileStorage storage = new FileStorage(dir.toString());
        String stored = storage.save("Bukti.PNG", PNG);
        assertTrue(stored.endsWith(".png"));
        assertTrue(storage.exists(stored));
        assertEquals("/uploads/" + stored, FileStorage.url(stored));
        assertNull(FileStorage.url(null));
        assertNull(FileStorage.url(" "));
        assertEquals(dir.toAbsolutePath().normalize(), storage.root());
        assertTrue(storage.delete(stored));
        assertFalse(storage.delete(stored));
        assertFalse(storage.exists(stored));
        assertFalse(storage.delete("../etc/passwd"));
        assertFalse(storage.delete(null));
        assertFalse(storage.exists(null));
    }

    @Test
    void fileStorageRejectsInvalidUploads() throws IOException {
        FileStorage storage = new FileStorage(Files.createTempDirectory("cashflow-uploads").toString());
        assertThrows(ValidationException.class, () -> storage.save("a.png", null));
        assertThrows(ValidationException.class, () -> storage.save("a.png", new byte[0]));
        assertThrows(ValidationException.class, () -> storage.save("a.png", new byte[(int) FileStorage.MAX_BYTES + 1]));
        assertThrows(ValidationException.class, () -> storage.save("a.exe", PNG));
        assertThrows(ValidationException.class, () -> storage.save(null, PNG));
        assertThrows(ValidationException.class, () -> storage.save("noext", PNG));
        assertThrows(ValidationException.class, () -> storage.save("a.png", "not an image".getBytes()));
        assertThrows(ValidationException.class, () -> storage.save("a.jpg", PNG));
    }

    @Test
    void fileStorageAcceptsOtherImageFormats() throws IOException, ValidationException {
        FileStorage storage = new FileStorage(Files.createTempDirectory("cashflow-uploads").toString());
        byte[] jpg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};
        byte[] gif = {'G', 'I', 'F', '8', '9', 'a'};
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V'};
        assertTrue(storage.save("a.jpg", jpg).endsWith(".jpg"));
        assertTrue(storage.save("a.jpeg", jpg).endsWith(".jpeg"));
        assertTrue(storage.save("a.gif", gif).endsWith(".gif"));
        assertTrue(storage.save("a.webp", webp).endsWith(".webp"));
        assertThrows(ValidationException.class, () -> storage.save("a.webp", gif));
    }
}
