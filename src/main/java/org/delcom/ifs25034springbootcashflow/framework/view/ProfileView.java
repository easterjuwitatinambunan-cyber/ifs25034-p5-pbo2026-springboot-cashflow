package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;

/** Profil pengguna: nama, password, dan foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthSession auth;
    private final UserUseCase users;
    private final FileStorage storage;
    private final FinancePresenter presenter;

    private final Avatar avatar = new Avatar();
    private final TextField username = new TextField("Username");
    private final TextField role = new TextField("Peran");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField password = new PasswordField("Password baru (opsional)");
    private final PasswordField confirm = new PasswordField("Ulangi password baru");
    private final ImageUploadField photo = new ImageUploadField("Foto profil", "Pilih Foto");
    private final Button save = new Button("Simpan Profil");
    private final ProgressBar progress = Busy.newBar();

    public ProfileView(AuthSession auth, UserUseCase users, FileStorage storage, FinancePresenter presenter) {
        this.auth = auth;
        this.users = users;
        this.storage = storage;
        this.presenter = presenter;
        setPadding(true);

        avatar.addClassName("cf-profile-avatar");
        username.setReadOnly(true);
        role.setReadOnly(true);
        fullName.setRequiredIndicatorVisible(true);
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        confirm.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        for (var field : List.of(username, role, fullName)) {
            field.setWidthFull();
        }
        password.setWidthFull();
        confirm.setWidthFull();
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> doSave());

        Paragraph hint = new Paragraph("Kosongkan kolom password bila tidak ingin mengubahnya.");
        hint.addClassName("cf-muted");
        VerticalLayout form = new VerticalLayout(avatar, username, role, fullName, hint, password, confirm,
                photo, progress, save);
        form.setPadding(false);
        form.addClassName("cf-form");
        PageCard card = new PageCard();
        card.add(new H2("Profil Saya"), form);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var user = auth.currentUser();
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        show(user.get());
    }

    private void show(User user) {
        username.setValue(user.getUsername());
        role.setValue(user.isAdmin() ? "ADMIN" : "USER");
        fullName.setValue(user.getFullName());
        avatar.setName(user.getFullName());
        String url = FileStorage.url(user.getPhotoPath());
        avatar.setImage(url);
    }

    private void doSave() {
        String name = fullName.getValue();
        String newPassword = password.getValue();
        String again = confirm.getValue();
        Busy.run(progress, List.of(save, fullName, password, confirm), () -> {
            if (!newPassword.equals(again)) {
                Notifications.error("Konfirmasi password baru tidak sama");
                return;
            }
            User actor = auth.currentUser().orElse(null);
            if (actor == null) {
                UI.getCurrent().navigate(LoginView.class);
                return;
            }
            String newPhoto = null;
            try {
                User updated = users.updateProfile(actor.getId(), name, newPassword);
                if (photo.hasFile()) {
                    newPhoto = storage.save(photo.fileName(), photo.data());
                    String oldPhoto = updated.getPhotoPath();
                    updated = users.updatePhoto(actor.getId(), newPhoto);
                    storage.delete(oldPhoto);
                }
                password.clear();
                confirm.clear();
                photo.reset();
                show(updated);
                getParent().filter(MainLayout.class::isInstance).map(MainLayout.class::cast)
                        .ifPresent(MainLayout::refresh);
                Notifications.success(presenter.profileUpdatedMessage());
            } catch (DomainException e) {
                if (newPhoto != null) {
                    storage.delete(newPhoto);
                }
                Notifications.error(presenter.errorMessage(e));
            }
        });
    }
}
