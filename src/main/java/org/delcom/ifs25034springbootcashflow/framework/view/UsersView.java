package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Comparator;
import java.util.Map;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;

/** Daftar seluruh pengguna (khusus ADMIN). */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthSession auth;
    private final UserUseCase users;
    private final Grid<User> grid = new Grid<>();

    public UsersView(AuthSession auth, UserUseCase users) {
        this.auth = auth;
        this.users = users;
        setPadding(true);
        GridStyles.apply(grid, "Daftar pengguna");

        grid.addColumn(User::getId).setHeader("ID").setSortable(true)
                .setComparator(Comparator.comparing(User::getId)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername));
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true)
                .setComparator(Comparator.comparing(u -> u.getFullName().toLowerCase())).setFlexGrow(2);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Span badge = new Span(u.isAdmin() ? "ADMIN" : "USER");
            badge.addClassName("type-badge");
            badge.addClassName(u.isAdmin() ? "admin" : "user");
            return badge;
        })).setHeader("Peran").setSortable(true).setComparator(Comparator.comparing(User::getRole))
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Button open = new Button(u.getId().equals(currentId()) ? "Dashboard Saya" : "Lihat Transaksi",
                    e -> openTransactions(u));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            open.getElement().setAttribute("aria-label", "Lihat transaksi " + u.getUsername());
            return open;
        })).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);

        Paragraph info = new Paragraph("Anda dapat melihat transaksi pengguna lain secara read-only.");
        info.addClassName("cf-muted");
        PageCard card = new PageCard();
        card.add(new H2("Daftar Pengguna"), info, grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var actor = auth.currentUser();
        if (actor.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!actor.get().isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        try {
            grid.setItems(users.listUsers(actor.get()));
        } catch (DomainException e) {
            Notifications.error(e.getMessage());
        }
    }

    private Integer currentId() {
        return auth.currentUser().map(User::getId).orElse(null);
    }

    private void openTransactions(User target) {
        if (target.getId().equals(currentId())) {
            UI.getCurrent().navigate(FinanceView.class);
        } else {
            UI.getCurrent().navigate("user-transactions",
                    QueryParameters.simple(Map.of("userId", String.valueOf(target.getId()))));
        }
    }
}
