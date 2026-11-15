package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.progressbar.ProgressBar;
import java.util.List;

/**
 * Menampilkan ProgressBar dan menonaktifkan kontrol, lalu menjalankan pekerjaan pada request
 * berikutnya agar indikator benar-benar sempat tampil sebelum proses selesai.
 */
final class Busy {

    private Busy() {
    }

    static ProgressBar newBar() {
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        bar.setVisible(false);
        bar.getElement().setAttribute("aria-label", "Sedang memproses");
        return bar;
    }

    static void run(ProgressBar bar, List<? extends HasEnabled> locks, Runnable work) {
        UI ui = UI.getCurrent();
        bar.setVisible(true);
        locks.forEach(c -> c.setEnabled(false));
        ui.getPage().executeJs("return true;").then(Boolean.class, ready -> {
            try {
                work.run();
            } catch (RuntimeException e) {
                Notifications.error("Terjadi kesalahan pada sistem. Silakan coba lagi.");
            } finally {
                bar.setVisible(false);
                locks.forEach(c -> c.setEnabled(true));
            }
        });
    }
}
