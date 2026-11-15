package org.delcom.ifs25034springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import org.delcom.ifs25034springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25034springbootcashflow.domain.entity.Role;
import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tes repository JDBC memakai H2 (mode PostgreSQL) in-memory. */
class JdbcRepositoryTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource = ds;
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource);
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void userCrud() {
        User saved = users.save("andi", "hash", "Andi", Role.USER);
        assertNotNull(saved.getId());
        assertEquals("Andi", users.findById(saved.getId()).orElseThrow().getFullName());
        assertEquals(Role.USER, users.findByUsername("andi").orElseThrow().getRole());
        assertTrue(users.findByUsername("none").isEmpty());
        assertTrue(users.findById(999).isEmpty());

        saved.setFullName("Andi Baru");
        saved.setPhotoPath("p.png");
        saved.setRole(Role.ADMIN);
        saved.setPasswordHash("h2");
        users.save(saved);
        User reloaded = users.findById(saved.getId()).orElseThrow();
        assertEquals("Andi Baru", reloaded.getFullName());
        assertEquals("p.png", reloaded.getPhotoPath());
        assertEquals(Role.ADMIN, reloaded.getRole());
        assertEquals("h2", reloaded.getPasswordHash());

        users.save("budi", "h", "Budi", Role.USER);
        assertEquals(2, users.findAll().size());
        assertEquals(1, users.findBy(u -> u.getRole() == Role.ADMIN).size());
        assertTrue(users.deleteById(saved.getId()));
        assertFalse(users.deleteById(saved.getId()));
    }

    @Test
    void transactionCrud() {
        User owner = users.save("andi", "hash", "Andi", Role.USER);
        User other = users.save("budi", "hash", "Budi", Role.USER);
        Transaction a = transactions.save(owner.getId(), "Gaji", 5_000_000, TransactionType.INCOME, "x.png");
        transactions.save(other.getId(), "Makan", 25_000, TransactionType.EXPENSE, null);
        assertNotNull(a.getId());

        Transaction found = transactions.findById(a.getId()).orElseThrow();
        assertEquals("Gaji", found.getDescription());
        assertEquals(5_000_000, found.getAmount());
        assertEquals(TransactionType.INCOME, found.getType());
        assertEquals("x.png", found.getProofPath());
        assertNotNull(found.getCreatedAt());
        assertTrue(transactions.findById(999).isEmpty());

        assertEquals(1, transactions.findByUserId(owner.getId()).size());
        assertEquals(2, transactions.findAll().size());

        a.setDescription("Gaji Baru");
        a.setAmount(6_000_000);
        a.setType(TransactionType.EXPENSE);
        a.setProofPath(null);
        transactions.save(a);
        Transaction updated = transactions.findById(a.getId()).orElseThrow();
        assertEquals("Gaji Baru", updated.getDescription());
        assertEquals(6_000_000, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertNull(updated.getProofPath());

        assertTrue(transactions.deleteById(a.getId()));
        assertFalse(transactions.deleteById(a.getId()));
        transactions.save(new Transaction(null, owner.getId(), "Lagi", 1, TransactionType.INCOME, null,
                LocalDateTime.now()));
        assertEquals(2, transactions.findAll().size());
    }

    @Test
    void databaseFailuresBecomeIllegalStateException() {
        JdbcDataSource broken = new JdbcDataSource();
        broken.setURL("jdbc:h2:tcp://localhost:1/none");
        JdbcUserRepository u = new JdbcUserRepository(broken);
        JdbcTransactionRepository t = new JdbcTransactionRepository(broken);
        User user = new User(null, "x", "h", "X", Role.USER, null);
        Transaction tx = new Transaction(null, 1, "d", 1, TransactionType.INCOME, null, LocalDateTime.now());
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));
        assertThrows(IllegalStateException.class, () -> u.save(user));
        assertThrows(IllegalStateException.class, () -> u.findById(1));
        assertThrows(IllegalStateException.class, () -> u.findByUsername("x"));
        assertThrows(IllegalStateException.class, () -> u.findAll());
        assertThrows(IllegalStateException.class, () -> u.deleteById(1));
        assertThrows(IllegalStateException.class, () -> t.save(tx));
        assertThrows(IllegalStateException.class, () -> t.findById(1));
        assertThrows(IllegalStateException.class, () -> t.findAll());
        assertThrows(IllegalStateException.class, () -> t.findByUserId(1));
        assertThrows(IllegalStateException.class, () -> t.deleteById(1));
    }
}
