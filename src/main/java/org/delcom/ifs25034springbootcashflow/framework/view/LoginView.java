package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends Main implements BeforeEnterObserver {

    private final AuthSession auth;
    private final AuthUseCase authUseCase;
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button submit = new Button("Masuk");
    private final ProgressBar progress = Busy.newBar();

    public LoginView(AuthSession auth, AuthUseCase authUseCase) {
        this.auth = auth;
        this.authUseCase = authUseCase;
        addClassName("auth-page");

        username.setValueChangeMode(ValueChangeMode.EAGER);
        username.setRequiredIndicatorVisible(true);
        username.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.USERNAME);
        username.setWidthFull();
        idOnInput(username, "login-username");

        password.setValueChangeMode(ValueChangeMode.EAGER);
        password.setRequiredIndicatorVisible(true);
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.CURRENT_PASSWORD);
        password.setWidthFull();
        idOnInput(password, "login-password");

        submit.setId("login-submit");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.setWidthFull();
        submit.addClickListener(e -> doLogin());

        H1 title = new H1("Cashflow");
        Paragraph subtitle = new Paragraph("Masuk untuk mengelola pemasukan, pengeluaran, dan saldo Anda.");
        subtitle.addClassName("cf-muted");
        RouterLink register = new RouterLink("Belum punya akun? Daftar di sini", RegisterView.class);

        VerticalLayout form = new VerticalLayout(title, subtitle, username, password, progress, submit, register);
        form.setPadding(false);
        PageCard card = new PageCard();
        card.addClassName("auth-card");
        card.add(form);
        add(card);
    }

    /** Memasang id pada elemen input asli (bukan pembungkus web component) agar mudah diotomasi. */
    private static void idOnInput(Component field, String id) {
        field.getElement().executeJs(
                "const host = this; const id = $0; let tries = 0;"
                        + "const apply = () => {"
                        + "  const input = host.querySelector('input');"
                        + "  if (!input) { return false; }"
                        + "  input.id = id;"
                        + "  const label = host.querySelector('[slot=\"label\"]');"
                        + "  if (label) { label.setAttribute('for', id); }"
                        + "  return true;"
                        + "};"
                        + "if (!apply()) {"
                        + "  const timer = setInterval(() => {"
                        + "    tries++;"
                        + "    if (apply() || tries > 100) { clearInterval(timer); }"
                        + "  }, 50);"
                        + "}",
                id);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (auth.isLoggedIn()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void doLogin() {
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            auth.login(user);
            password.clear();
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException e) {
            Notifications.error(e.getMessage());
            password.clear();
        }
    }
}