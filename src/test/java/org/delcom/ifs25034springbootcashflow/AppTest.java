package org.delcom.ifs25034springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.FinanceSummary;
import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25034springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: konteks Spring berjalan dengan H2 (profil test), admin ter-seed, alur utama bekerja. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private AuthUseCase auth;
    @Autowired
    private FinanceUseCase finance;
    @Autowired
    private UserUseCase users;
    @Autowired
    private FileStorage storage;

    @Test
    void contextLoadsAndRootAdminIsSeeded() throws DomainException {
        User admin = auth.login("admin", "admin");
        assertEquals(Role.ADMIN, admin.getRole());
        assertNotNull(storage.root());
    }

    @Test
    void registerAddTransactionAndReadBalanceThroughDatabase() throws DomainException {
        User user = auth.register("smoke-user", "rahasia1", "Smoke User");
        finance.addIncome(user.getId(), "Gaji", "1000000", null);
        finance.addExpense(user.getId(), "Makan", "250000", null);
        FinanceSummary summary = finance.summary(user, user.getId());
        assertEquals(750_000, summary.balance());
        assertEquals(1, users.listUsers(auth.login("admin", "admin")).stream()
                .filter(u -> u.getUsername().equals("smoke-user")).count());
    }
}
