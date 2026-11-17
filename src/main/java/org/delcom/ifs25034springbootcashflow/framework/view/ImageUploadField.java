package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/** Kolom unggah satu gambar (bukti transaksi atau foto profil); isi file ditahan di memori. */
final class ImageUploadField extends Div {

    private static final String EMPTY = "Belum ada file dipilih";

    private final Upload upload;
    private final Span info = new Span(EMPTY);
    private volatile String fileName;
    private volatile byte[] data;

    ImageUploadField(String label, String buttonText) {
        UI ui = UI.getCurrent();
        addClassName("cf-upload");
        Span caption = new Span(label);
        caption.addClassName("cf-field-label");
        info.addClassName("cf-upload-info");

        upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            this.fileName = metadata.fileName();
            this.data = bytes;
            ui.access(() -> info.setText(metadata.fileName()));
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setUploadButton(new Button(buttonText));
        upload.setDropLabel(new Span("atau seret gambar ke sini (PNG, JPG, GIF, WEBP; maks. 5 MB)"));
        upload.addFileRejectedListener(e -> Notifications.error(e.getErrorMessage()));
        upload.addFileRemovedListener(e -> clear());
        add(caption, upload, info);
    }

    boolean hasFile() {
        return data != null;
    }

    String fileName() {
        return fileName;
    }

    byte[] data() {
        return data;
    }

    void reset() {
        clear();
        upload.clearFileList();
    }

    private void clear() {
        fileName = null;
        data = null;
        info.setText(EMPTY);
    }
}
