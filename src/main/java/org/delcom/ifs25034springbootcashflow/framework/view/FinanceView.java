package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.FinanceUseCase;

/** Dashboard cashflow milik pengguna yang sedang login (USER dan ADMIN). */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final AuthSession auth;

    private final SummaryCards summary;
    private final TextField search = new TextField("Cari keterangan");
    private final Select<SortOption> sort = new Select<>();
    private final TransactionGrid grid;
    private final Paragraph emptyNote = new Paragraph("Belum ada transaksi yang cocok. Tambahkan pemasukan atau pengeluaran pertama Anda.");

    public FinanceView(FinanceUseCase finance, FinancePresenter presenter, FileStorage storage, AuthSession auth) {
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        this.auth = auth;
        setPadding(true);

        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, false, this::openDeleteDialog);

        search.setPlaceholder("Ketik keterangan transaksi");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> reload());

        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST);
        sort.setEmptySelectionAllowed(false);
        sort.addValueChangeListener(e -> reload());

        Button refresh = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> reload());
        Button addIncome = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openAddDialog(TransactionType.INCOME));
        addIncome.setId("add-income");
        addIncome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIncome.addClassName("cf-btn-income");
        Button addExpense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        addExpense.setId("add-expense");
        addExpense.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addExpense.addClassName("cf-btn-expense");

        Div toolbar = new Div(search, sort, refresh, addIncome, addExpense);
        toolbar.addClassName("cf-toolbar");
        emptyNote.addClassName("cf-muted");

        PageCard card = new PageCard();
        card.add(new H2("Dashboard Cashflow"), summary, toolbar, grid, emptyNote);
        add(card);
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        reload();
    }

    private void reload() {
        User actor = auth.currentUser().orElse(null);
        if (actor == null) {
            UI.getCurrent().navigate(LoginView.class);
            return;
        }
        try {
            List<Transaction> items = finance.list(actor, actor.getId(), search.getValue(), sort.getValue());
            grid.setItems(items);
            summary.update(finance.summary(actor, actor.getId()));
            emptyNote.setVisible(items.isEmpty());
        } catch (DomainException e) {
            Notifications.error(presenter.errorMessage(e));
        }
    }

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;
        Dialog dialog = Dialogs.create(income ? "Tambah Pemasukan" : "Tambah Pengeluaran", type.label());

        TextField description = new TextField("Keterangan");
        description.setRequiredIndicatorVisible(true);
        description.setMaxLength(FinanceUseCase.MAX_DESCRIPTION_LENGTH);
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setRequiredIndicatorVisible(true);
        amount.setPlaceholder("Contoh: 150000");
        amount.setHelperText("Angka bulat tanpa titik atau koma");
        amount.setAllowedCharPattern("[0-9]");
        amount.setWidthFull();
        ImageUploadField proof = new ImageUploadField("Bukti transaksi (opsional)", "Pilih Gambar");
        ProgressBar progress = Busy.newBar();

        VerticalLayout content = new VerticalLayout(description, amount, proof, progress);
        content.setPadding(false);
        content.addClassName("cf-dialog-content");
        dialog.add(content);

        Button cancel = new Button("Batal", e -> dialog.close());
        Button save = new Button("Simpan");
        save.setId(income ? "save-income" : "save-expense");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> Busy.run(progress, List.of(save, cancel, description, amount), () -> {
            String stored = null;
            try {
                User actor = auth.currentUser().orElse(null);
                if (actor == null) {
                    dialog.close();
                    UI.getCurrent().navigate(LoginView.class);
                    return;
                }
                if (proof.hasFile()) {
                    stored = storage.save(proof.fileName(), proof.data());
                }
                Transaction created = income
                        ? finance.addIncome(actor.getId(), description.getValue(), amount.getValue(), stored)
                        : finance.addExpense(actor.getId(), description.getValue(), amount.getValue(), stored);
                Notifications.success(presenter.addedMessage(created));
                dialog.close();
                reload();
            } catch (DomainException ex) {
                if (stored != null) {
                    storage.delete(stored);
                }
                Notifications.error(presenter.errorMessage(ex));
            }
        }));
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = Dialogs.create("Hapus Transaksi", "Konfirmasi");
        Paragraph question = new Paragraph("Anda yakin ingin menghapus transaksi berikut? Tindakan ini tidak dapat dibatalkan.");
        Span detail = new Span(presenter.line(transaction));
        detail.addClassName("cf-strong");
        ProgressBar progress = Busy.newBar();
        VerticalLayout content = new VerticalLayout(question, detail, progress);
        content.setPadding(false);
        dialog.add(content);

        Button cancel = new Button("Batal", e -> dialog.close());
        Button confirm = new Button("Hapus");
        confirm.setId("confirm-delete");
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirm.addClickListener(e -> Busy.run(progress, List.of(confirm, cancel), () -> {
            try {
                User actor = auth.currentUser().orElse(null);
                if (actor == null) {
                    dialog.close();
                    UI.getCurrent().navigate(LoginView.class);
                    return;
                }
                Transaction removed = finance.delete(actor.getId(), transaction.getId());
                storage.delete(removed.getProofPath());
                Notifications.success(presenter.deletedMessage(removed));
                dialog.close();
                reload();
            } catch (DomainException ex) {
                Notifications.error(presenter.errorMessage(ex));
                dialog.close();
                reload();
            }
        }));
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }
}
