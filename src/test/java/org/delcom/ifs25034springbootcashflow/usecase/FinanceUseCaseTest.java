package org.delcom.ifs25034springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25034springbootcashflow.domain.entity.*;
import org.delcom.ifs25034springbootcashflow.domain.exception.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FinanceUseCaseTest {

    private FinanceUseCase finance;
    private User andi;
    private User budi;
    private User admin;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        finance = new FinanceUseCase(new TransactionRepository(), users);
        andi = users.save("andi", "h", "Andi", Role.USER);
        budi = users.save("budi", "h", "Budi", Role.USER);
        admin = users.save("admin", "h", "Admin", Role.ADMIN);
    }

    @Test
    void addIncomeAndExpenseUpdateBalance() throws DomainException {
        finance.addIncome(andi.getId(), " Gaji ", "5000000", "a.png");
        finance.addExpense(andi.getId(), "Makan", "25000", null);
        FinanceSummary s = finance.summary(andi, andi.getId());
        assertEquals(2, s.count());
        assertEquals(5_000_000, s.totalIncome());
        assertEquals(25_000, s.totalExpense());
        assertEquals(4_975_000, finance.getBalance(andi, andi.getId()));
        assertEquals("Gaji", finance.list(andi, andi.getId(), "gaji", null).get(0).getDescription());
    }

    @Test
    void validationRejectsBadInput() {
        int id = andi.getId();
        assertThrows(ValidationException.class, () -> finance.addIncome(id, " ", "100", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, null, "100", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, "x".repeat(256), "100", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, "ok", null, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, "ok", " ", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, "ok", "abc", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(id, "ok", "10.5", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(id, "ok", "0", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(id, "ok", "-5", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(id, "ok", "1000000000000", null));
        assertThrows(EntityNotFoundException.class, () -> finance.addIncome(999, "ok", "100", null));
    }

    @Test
    void searchAndSortWork() throws DomainException {
        finance.addIncome(andi.getId(), "Gaji Bulanan", "300", null);
        finance.addExpense(andi.getId(), "Beli Gula", "100", null);
        finance.addExpense(andi.getId(), "Beli Beras", "200", null);
        assertEquals(2, finance.list(andi, andi.getId(), "  BELI ", SortOption.AMOUNT_ASC).size());
        assertEquals("Beli Gula", finance.list(andi, andi.getId(), "beli", SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals(3, finance.list(andi, andi.getId(), null, SortOption.INCOME_FIRST).size());
        assertEquals("Gaji Bulanan", finance.list(andi, andi.getId(), "", SortOption.INCOME_FIRST).get(0).getDescription());
        assertTrue(finance.list(andi, andi.getId(), "tidak ada", SortOption.NEWEST).isEmpty());
    }

    @Test
    void userCannotReadOthersButAdminCan() throws DomainException {
        finance.addIncome(andi.getId(), "Rahasia", "100", null);
        assertThrows(ValidationException.class, () -> finance.list(budi, andi.getId(), "", null));
        assertThrows(ValidationException.class, () -> finance.summary(budi, andi.getId()));
        assertThrows(ValidationException.class, () -> finance.list(null, andi.getId(), "", null));
        assertThrows(ValidationException.class, () -> finance.list(new User(null, "x", "h", "x", Role.ADMIN, null), 1, "", null));
        assertEquals(1, finance.list(admin, andi.getId(), "", null).size());
        assertThrows(EntityNotFoundException.class, () -> finance.list(admin, 999, "", null));
        assertEquals(0, finance.list(admin, admin.getId(), "", null).size());
    }

    @Test
    void deleteIsOwnerOnlyEvenForAdmin() throws DomainException {
        Transaction t = finance.addIncome(andi.getId(), "Hapus aku", "100", "b.png");
        int id = t.getId();
        assertThrows(ValidationException.class, () -> finance.delete(budi.getId(), id));
        assertThrows(ValidationException.class, () -> finance.delete(admin.getId(), id));
        assertThrows(ValidationException.class, () -> finance.delete(andi.getId(), 0));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(andi.getId(), 12345));
        assertEquals("b.png", finance.delete(andi.getId(), id).getProofPath());
        assertEquals(0, finance.summary(andi, andi.getId()).count());
    }

    @Test
    void updateChangesFieldsForOwnerOnly() throws DomainException {
        Transaction t = finance.addIncome(andi.getId(), "Lama", "100", null);
        int id = t.getId();
        Transaction updated = finance.update(andi.getId(), id, "Baru", "250", TransactionType.EXPENSE);
        assertEquals("Baru", updated.getDescription());
        assertEquals(250, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertThrows(ValidationException.class, () -> finance.update(andi.getId(), id, "Baru", "250", null));
        assertThrows(ValidationException.class, () -> finance.update(budi.getId(), id, "x", "1", TransactionType.INCOME));
    }

    @Test
    void listReturnsMutableIndependentResult() throws DomainException {
        finance.addIncome(andi.getId(), "A", "1", null);
        List<Transaction> list = finance.list(andi, andi.getId(), "", null);
        assertEquals(1, list.size());
    }
}
