package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

/** Pembuat dialog seragam: header biru #2563EB, footer abu-abu #F3F4F6 (lihat styles.css). */
final class Dialogs {

    private Dialogs() {
    }

    static Dialog create(String title, String badge) {
        Dialog dialog = new Dialog();
        dialog.addClassName("cf-dialog");
        dialog.setHeaderTitle(title);
        dialog.setCloseOnOutsideClick(false);
        Span badgeSpan = new Span(badge);
        badgeSpan.addClassName("cf-dialog-badge");
        dialog.getHeader().add(badgeSpan);
        return dialog;
    }
}
