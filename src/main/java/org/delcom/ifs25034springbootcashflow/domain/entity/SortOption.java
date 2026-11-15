package org.delcom.ifs25034springbootcashflow.domain.entity;

import java.util.Comparator;

/** Opsi pengurutan transaksi; setiap opsi membawa Comparator. */
public enum SortOption {
    NEWEST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt).thenComparing(Transaction::getId).reversed()),
    OLDEST("Terlama",
            Comparator.comparing(Transaction::getCreatedAt).thenComparing(Transaction::getId)),
    AMOUNT_DESC("Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).reversed().thenComparing(Transaction::getId)),
    AMOUNT_ASC("Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount).thenComparing(Transaction::getId)),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.getType().ordinal())
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed())
                    .thenComparing(Transaction::getId)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.getType().ordinal()).reversed()
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed())
                    .thenComparing(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
