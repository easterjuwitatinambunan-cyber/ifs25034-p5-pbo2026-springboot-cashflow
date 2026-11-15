package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;

/** Transaksi pengguna lain, read-only (khusus ADMIN). Rute: /user-transactions?userId=ID */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthSession auth;
    private final FinanceUseCase finance;
    private final UserUseCase users;
    private final FinancePresenter presenter;

    private final H2 heading = new H2("Transaksi Pengguna");
    private final Paragraph readOnlyNote = new Paragraph();
    private final SummaryCards summary;
    private final TextField search = new TextField("Cari keterangan");
    private final Select<SortOption> sort = new Select<>();
    private final TransactionGrid grid;
    private User actor;
    private User target;

    public UserTransactionsView(AuthSession auth, FinanceUseCase finance, UserUseCase users,
                                FinancePresenter presenter) {
        this.auth = auth;
        this.finance = finance;
        this.users = users;
        this.presenter = presenter;
        setPadding(true);

        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, true, t -> { });
        readOnlyNote.addClassName("cf-readonly-note");

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
        Button back = new Button("Kembali ke Daftar Pengguna", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(UsersView.class));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Div toolbar = new Div(search, sort, back);
        toolbar.addClassName("cf-toolbar");
        PageCard card = new PageCard();
        card.add(heading, readOnlyNote, summary, toolbar, grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var current = auth.currentUser();
        if (current.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!current.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        actor = current.get();
        try {
            target = users.getUser(parseUserId(event));
        } catch (DomainException e) {
            Notifications.error(presenter.errorMessage(e));
            event.forwardTo(UsersView.class);
            return;
        }
        heading.setText("Transaksi " + target.getFullName() + " (" + target.getUsername() + ")");
        readOnlyNote.setText("Mode baca saja: ADMIN tidak dapat menambah atau menghapus transaksi pengguna lain.");
        reload();
    }

    private static int parseUserId(BeforeEnterEvent event) throws ValidationException {
        String raw = event.getLocation().getQueryParameters().getSingleParameter("userId").orElse("");
        try {
            int id = Integer.parseInt(raw.trim());
            if (id <= 0) {
                throw new ValidationException("ID pengguna tidak valid");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("ID pengguna tidak valid");
        }
    }

    private void reload() {
        if (actor == null || target == null) {
            return;
        }
        try {
            List<Transaction> items = finance.list(actor, target.getId(), search.getValue(), sort.getValue());
            grid.setItems(items);
            summary.update(finance.summary(actor, target.getId()));
        } catch (DomainException e) {
            Notifications.error(presenter.errorMessage(e));
        }
    }
}
