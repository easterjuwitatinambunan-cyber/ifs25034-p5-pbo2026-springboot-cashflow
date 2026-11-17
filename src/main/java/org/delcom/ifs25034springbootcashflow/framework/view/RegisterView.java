package org.delcom.ifs25034springbootcashflow.framework.view;

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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import java.util.List;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends Main implements BeforeEnterObserver {

    private final AuthSession auth;
    private final AuthUseCase authUseCase;
    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final Button submit = new Button("Daftar");
    private final ProgressBar progress = Busy.newBar();

    public RegisterView(AuthSession auth, AuthUseCase authUseCase) {
        this.auth = auth;
        this.authUseCase = authUseCase;
        addClassName("auth-page");

        fullName.setWidthFull();
        username.setHelperText("3-50 karakter: huruf, angka, titik, garis bawah, atau strip");
        username.setWidthFull();
        password.setHelperText("Minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");
        password.setWidthFull();
        confirm.setWidthFull();
        for (var field : List.of(fullName, username)) {
            field.setRequiredIndicatorVisible(true);
        }
        password.setRequiredIndicatorVisible(true);
        confirm.setRequiredIndicatorVisible(true);
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.setWidthFull();
        submit.addClickListener(e -> doRegister());

        H1 title = new H1("Buat Akun Cashflow");
        Paragraph subtitle = new Paragraph("Daftar gratis untuk mulai mencatat keuangan Anda.");
        subtitle.addClassName("cf-muted");
        RouterLink login = new RouterLink("Sudah punya akun? Masuk di sini", LoginView.class);

        VerticalLayout form = new VerticalLayout(title, subtitle, fullName, username, password, confirm,
                progress, submit, login);
        form.setPadding(false);
        PageCard card = new PageCard();
        card.addClassName("auth-card");
        card.add(form);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (auth.isLoggedIn()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void doRegister() {
        String name = fullName.getValue();
        String user = username.getValue();
        String pass = password.getValue();
        String again = confirm.getValue();
        Busy.run(progress, List.of(submit, fullName, username, password, confirm), () -> {
            if (!pass.equals(again)) {
                Notifications.error("Konfirmasi password tidak sama");
                return;
            }
            try {
                User created = authUseCase.register(user, pass, name);
                auth.login(created);
                Notifications.success("Akun berhasil dibuat. Selamat datang, " + created.getFullName() + "!");
                UI.getCurrent().navigate(FinanceView.class);
            } catch (DomainException e) {
                Notifications.error(e.getMessage());
            }
        });
    }
}
