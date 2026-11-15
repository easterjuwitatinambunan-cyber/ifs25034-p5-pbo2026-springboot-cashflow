package org.delcom.ifs25034springbootcashflow.usecase;

import org.delcom.ifs25034springbootcashflow.domain.entity.FinanceSummary;
import org.delcom.ifs25034springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25034springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25034springbootcashflow.domain.repository.IUserRepository;
import java.util.List;
import java.util.Locale;

/** Proses bisnis cashflow. Setiap operasi dibatasi pada pemilik data (kecuali baca oleh ADMIN). */
public class FinanceUseCase {

    public static final long MAX_AMOUNT = 999_999_999_999L;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private final ITransactionRepository transactions;
    private final IUserRepository users;

    public FinanceUseCase(ITransactionRepository transactions, IUserRepository users) {
        this.transactions = transactions;
        this.users = users;
    }

    public Transaction addIncome(int userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.EXPENSE, proofPath);
    }

    private Transaction add(int userId, String description, String amountText, TransactionType type,
                            String proofPath) throws DomainException {
        users.findById(userId).orElseThrow(() -> new EntityNotFoundException("Pengguna", userId));
        String text = validateDescription(description);
        long amount = parseAmount(amountText);
        return transactions.save(userId, text, amount, type, proofPath);
    }

    public Transaction update(int userId, int transactionId, String description, String amountText,
                              TransactionType type) throws DomainException {
        Transaction tx = findOwned(userId, transactionId);
        String text = validateDescription(description);
        long amount = parseAmount(amountText);
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih");
        }
        tx.setDescription(text);
        tx.setAmount(amount);
        tx.setType(type);
        return transactions.save(tx);
    }

    /** Menghapus transaksi milik sendiri dan mengembalikannya (agar file bukti dapat dibersihkan). */
    public Transaction delete(int userId, int transactionId) throws DomainException {
        Transaction tx = findOwned(userId, transactionId);
        transactions.deleteById(transactionId);
        return tx;
    }

    public List<Transaction> list(User actor, int ownerId, String keyword, SortOption sort)
            throws DomainException {
        authorize(actor, ownerId);
        String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        SortOption option = sort == null ? SortOption.NEWEST : sort;
        return transactions.findByUserId(ownerId).stream()
                .filter(t -> key.isEmpty() || t.getDescription().toLowerCase(Locale.ROOT).contains(key))
                .sorted(option.comparator())
                .toList();
    }

    public FinanceSummary summary(User actor, int ownerId) throws DomainException {
        authorize(actor, ownerId);
        List<Transaction> all = transactions.findByUserId(ownerId);
        long income = 0;
        long expense = 0;
        for (Transaction t : all) {
            if (t.getType() == TransactionType.INCOME) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }
        return new FinanceSummary(all.size(), income, expense);
    }

    public long getBalance(User actor, int ownerId) throws DomainException {
        return summary(actor, ownerId).balance();
    }

    private void authorize(User actor, int ownerId) throws DomainException {
        if (actor == null || actor.getId() == null) {
            throw new ValidationException("Anda belum login");
        }
        if (actor.getId() != ownerId && !actor.isAdmin()) {
            throw new ValidationException("Anda tidak berhak mengakses data pengguna lain");
        }
        users.findById(ownerId).orElseThrow(() -> new EntityNotFoundException("Pengguna", ownerId));
    }

    private Transaction findOwned(int userId, int transactionId) throws DomainException {
        if (transactionId <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction tx = transactions.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", transactionId));
        if (tx.getUserId() != userId) {
            throw new ValidationException("Transaksi ini bukan milik Anda");
        }
        return tx;
    }

    private static String validateDescription(String description) throws ValidationException {
        String text = description == null ? "" : description.trim();
        if (text.isEmpty()) {
            throw new ValidationException("Keterangan wajib diisi");
        }
        if (text.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Keterangan maksimal " + MAX_DESCRIPTION_LENGTH + " karakter");
        }
        return text;
    }

    static long parseAmount(String amountText) throws ValidationException {
        String text = amountText == null ? "" : amountText.trim();
        if (text.isEmpty()) {
            throw new ValidationException("Jumlah wajib diisi");
        }
        long amount;
        try {
            amount = Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat tanpa titik atau koma");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        if (amount > MAX_AMOUNT) {
            throw new ValidationException("Jumlah terlalu besar (maksimal 999999999999)");
        }
        return amount;
    }
}
