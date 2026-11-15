package org.delcom.ifs25034springbootcashflow.adapter.repository;

import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25034springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(Transaction t) {
        try (Connection c = dataSource.getConnection()) {
            if (t.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, t.getUserId());
                    ps.setString(2, t.getDescription());
                    ps.setLong(3, t.getAmount());
                    ps.setString(4, t.getType().name());
                    ps.setString(5, t.getProofPath());
                    ps.setTimestamp(6, Timestamp.valueOf(t.getCreatedAt()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            t.setId(keys.getInt(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?")) {
                    ps.setString(1, t.getDescription());
                    ps.setLong(2, t.getAmount());
                    ps.setString(3, t.getType().name());
                    ps.setString(4, t.getProofPath());
                    ps.setInt(5, t.getId());
                    ps.executeUpdate();
                }
            }
            return t;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan transaksi: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM transactions WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca transaksi: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", null);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id", userId);
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus transaksi: " + e.getMessage(), e);
        }
    }

    private List<Transaction> query(String sql, Integer param) {
        List<Transaction> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca transaksi: " + e.getMessage(), e);
        }
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")), rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
