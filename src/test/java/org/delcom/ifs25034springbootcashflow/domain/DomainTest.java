package org.delcom.ifs25034springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.domain.entity.*;
import org.delcom.ifs25034springbootcashflow.domain.exception.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {

    private static Transaction tx(int id, long amount, TransactionType type, int hour) {
        return new Transaction(id, 1, "t" + id, amount, type, null, LocalDateTime.of(2026, 1, 1, hour, 0));
    }

    @Test
    void passwordHasherUsesSha256AndVerifies() {
        PasswordHasher hasher = new PasswordHasher();
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", hasher.hash("password"));
        assertTrue(hasher.matches("password", hasher.hash("password")));
        assertFalse(hasher.matches("other", hasher.hash("password")));
        assertFalse(hasher.matches(null, "x"));
        assertFalse(hasher.matches("x", null));
    }

    @Test
    void exceptionsCarryMessages() {
        assertEquals("boom", new DomainException("boom").getMessage());
        assertEquals("Pengguna dengan ID 7 tidak ditemukan", new EntityNotFoundException("Pengguna", 7).getMessage());
        assertEquals("bad", new ValidationException("bad").getMessage());
        assertTrue(new ValidationException("x") instanceof DomainException);
    }

    @Test
    void entitiesExposeStateAndSignedAmount() {
        User user = new User(null, "andi", "h", "Andi", Role.USER, null);
        user.setId(3);
        user.setPasswordHash("h2");
        user.setFullName("Andi B");
        user.setPhotoPath("p.png");
        assertEquals(3, user.getId());
        assertEquals("andi", user.getUsername());
        assertEquals("h2", user.getPasswordHash());
        assertEquals("Andi B", user.getFullName());
        assertEquals("p.png", user.getPhotoPath());
        assertFalse(user.isAdmin());
        user.setRole(Role.ADMIN);
        assertTrue(user.isAdmin());

        Transaction t = tx(1, 500, TransactionType.INCOME, 1);
        t.setId(9);
        t.setDescription("gaji");
        t.setAmount(700);
        t.setProofPath("b.png");
        assertEquals(9, t.getId());
        assertEquals(1, t.getUserId());
        assertEquals("gaji", t.getDescription());
        assertEquals("b.png", t.getProofPath());
        assertEquals(700, t.signedAmount());
        t.setType(TransactionType.EXPENSE);
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals(-700, t.signedAmount());
        assertNotNull(t.getCreatedAt());
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
    }

    @Test
    void summaryComputesBalance() {
        FinanceSummary s = new FinanceSummary(3, 1000, 400);
        assertEquals(600, s.balance());
        assertEquals(3, s.count());
    }

    @Test
    void everySortOptionOrdersAsLabelled() {
        Transaction a = tx(1, 300, TransactionType.EXPENSE, 8);
        Transaction b = tx(2, 100, TransactionType.INCOME, 9);
        Transaction c = tx(3, 200, TransactionType.INCOME, 10);
        assertEquals(6, SortOption.values().length);
        assertEquals(List.of(3, 2, 1), ids(SortOption.NEWEST, a, b, c));
        assertEquals(List.of(1, 2, 3), ids(SortOption.OLDEST, c, b, a));
        assertEquals(List.of(1, 3, 2), ids(SortOption.AMOUNT_DESC, b, c, a));
        assertEquals(List.of(2, 3, 1), ids(SortOption.AMOUNT_ASC, a, c, b));
        assertEquals(List.of(3, 2, 1), ids(SortOption.INCOME_FIRST, a, b, c));
        assertEquals(List.of(1, 3, 2), ids(SortOption.EXPENSE_FIRST, b, c, a));
        assertEquals("Terbaru", SortOption.NEWEST.label());
        assertNotNull(SortOption.NEWEST.comparator());
    }

    private static List<Integer> ids(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }
}
