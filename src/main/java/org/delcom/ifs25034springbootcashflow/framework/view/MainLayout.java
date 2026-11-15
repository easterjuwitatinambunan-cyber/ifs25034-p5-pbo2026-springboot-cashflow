package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import java.util.Optional;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;

/** Layout aplikasi: sidebar, menu avatar (Profil/Keluar), dan pemeriksaan login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthSession auth;
    private final SideNav nav = new SideNav();
    private final Div userArea = new Div();

    public MainLayout(AuthSession auth) {
        this.auth = auth;
        setPrimarySection(Section.DRAWER);

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Buka atau tutup menu navigasi");
        H1 title = new H1("Cashflow");
        title.addClassName("app-title");
        Div spacer = new Div();
        spacer.addClassName("app-spacer");
        userArea.addClassName("app-user");
        addToNavbar(toggle, title, spacer, userArea);

        nav.setLabel("Menu");
        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = auth.currentUser();
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        build(user.get());
    }

    /** Dipanggil setelah profil berubah agar avatar dan nama ikut diperbarui. */
    public void refresh() {
        auth.currentUser().ifPresent(this::build);
    }

    private void build(User user) {
        nav.removeAll();
        nav.addItem(new SideNavItem("Dashboard", FinanceView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }

        Avatar avatar = new Avatar(user.getFullName());
        String photo = FileStorage.url(user.getPhotoPath());
        if (photo != null) {
            avatar.setImage(photo);
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menu.addItem(avatar);
        root.getElement().setAttribute("aria-label", "Menu akun " + user.getFullName());
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> logout());

        Span name = new Span(user.getFullName() + (user.isAdmin() ? " (Admin)" : ""));
        name.addClassName("app-user-name");
        userArea.removeAll();
        userArea.add(name, menu);
    }

    private void logout() {
        auth.logout();
        UI.getCurrent().navigate(LoginView.class);
    }
}
