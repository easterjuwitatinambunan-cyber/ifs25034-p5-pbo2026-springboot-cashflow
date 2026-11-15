package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.Comparator;
import java.util.function.Consumer;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;

/** Grid transaksi: ID, keterangan, jumlah, tipe, bukti (klik untuk zoom), waktu, dan aksi hapus. */
public class TransactionGrid extends Grid<Transaction> {

    public TransactionGrid(FinancePresenter presenter, boolean readOnly, Consumer<Transaction> onDelete) {
        super(Transaction.class, false);
        GridStyles.apply(this, "Daftar transaksi");

        addColumn(Transaction::getId).setHeader("ID").setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getId)).setAutoWidth(true).setFlexGrow(0);
        addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true)
                .setComparator(Comparator.comparing(t -> t.getDescription().toLowerCase())).setFlexGrow(2);
        addColumn(new ComponentRenderer<>(t -> {
            Span amount = new Span(presenter.money(t.getAmount()));
            amount.addClassName(t.getType() == TransactionType.INCOME ? "amount-income" : "amount-expense");
            return amount;
        })).setHeader("Jumlah").setSortable(true).setComparator(Comparator.comparingLong(Transaction::getAmount))
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        addColumn(new ComponentRenderer<>(t -> {
            Span badge = new Span(t.getType().label());
            badge.addClassName("type-badge");
            badge.addClassName(t.getType() == TransactionType.INCOME ? "income" : "expense");
            return badge;
        })).setHeader("Tipe").setSortable(true).setComparator(Comparator.comparing(Transaction::getType))
                .setAutoWidth(true);
        addColumn(new ComponentRenderer<>(this::proofCell)).setHeader("Bukti").setAutoWidth(true);
        addColumn(t -> presenter.dateTime(t.getCreatedAt())).setHeader("Waktu").setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);
        if (!readOnly) {
            addColumn(new ComponentRenderer<>(t -> {
                Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(t));
                delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                delete.getElement().setAttribute("aria-label", "Hapus transaksi " + t.getId());
                delete.getElement().setAttribute("title", "Hapus");
                return delete;
            })).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        }
    }

    private com.vaadin.flow.component.Component proofCell(Transaction t) {
        String url = FileStorage.url(t.getProofPath());
        if (url == null) {
            Span none = new Span("-");
            none.getElement().setAttribute("aria-label", "Tanpa bukti");
            return none;
        }
        String alt = "Bukti transaksi " + t.getId() + ": " + t.getDescription();
        Image thumb = new Image(url, alt);
        thumb.addClassName("cf-thumb");
        Button open = new Button(thumb, e -> new ImagePreviewDialog(url, alt).open());
        open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        open.getElement().setAttribute("aria-label", "Perbesar bukti transaksi " + t.getId());
        return open;
    }
}
