package org.delcom.ifs25034springbootcashflow.adapter.presenter;

import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format tampilan uang, baris transaksi, tanggal, dan pesan. Bukan tanggung jawab entity. */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"));

    /** Contoh: Rp 1,000,000.00 (negatif: -Rp 1,000.00). */
    public String money(long amount) {
        String text = String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(Math.abs(amount)));
        return amount < 0 ? "-" + text : text;
    }

    /** Contoh: [3] Gaji | Rp 5,000,000.00 | [Pemasukan] */
    public String line(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + money(t.getAmount())
                + " | [" + t.getType().label() + "]";
    }

    public String balanceLine(long balance) {
        return "Saldo: " + money(balance);
    }

    public String dateTime(LocalDateTime time) {
        return time == null ? "-" : DATE_TIME.format(time);
    }

    public String addedMessage(Transaction t) {
        return t.getType().label() + " \"" + t.getDescription() + "\" sebesar " + money(t.getAmount())
                + " berhasil ditambahkan";
    }

    public String deletedMessage(Transaction t) {
        return "Transaksi \"" + t.getDescription() + "\" berhasil dihapus";
    }

    public String profileUpdatedMessage() {
        return "Profil berhasil diperbarui";
    }

    public String errorMessage(DomainException e) {
        return e.getMessage();
    }
}
