package org.delcom.ifs25034springbootcashflow.domain.entity;

/** Ringkasan transaksi: jumlah, total pemasukan, total pengeluaran dan saldo. */
public record FinanceSummary(int count, long totalIncome, long totalExpense) {
    public long balance() {
        return totalIncome - totalExpense;
    }
}
