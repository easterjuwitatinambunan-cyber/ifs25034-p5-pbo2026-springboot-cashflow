package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/** Modal pratinjau gambar: klik gambar untuk toggle zoom 1.0x dan 1.8x, plus tombol kontrol di footer kanan. */
public class ImagePreviewDialog extends Dialog {

    private static final double MIN_ZOOM = 0.6;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.4;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double CLICK_ZOOM = 1.8;

    private final Image image;
    private final Span zoomLabel = new Span();
    private final Button zoomOut = new Button("Perkecil");
    private final Button zoomIn = new Button("Perbesar");
    private double zoom = DEFAULT_ZOOM;

    public ImagePreviewDialog(String url, String altText) {
        addClassName("cf-dialog");
        setHeaderTitle("Bukti Transaksi");
        Span badge = new Span("Pratinjau");
        badge.addClassName("cf-dialog-badge");
        getHeader().add(badge);

        image = new Image(url, altText);
        image.addClassName("cf-preview-image");
        image.addClickListener(e -> setZoom(zoom == DEFAULT_ZOOM ? CLICK_ZOOM : DEFAULT_ZOOM));
        Div frame = new Div(image);
        frame.addClassName("cf-preview-frame");
        Span hint = new Span("Klik gambar untuk memperbesar atau mengembalikan ukuran.");
        hint.addClassName("cf-muted");
        add(frame, hint);

        zoomLabel.addClassName("cf-zoom-label");
        zoomOut.addClickListener(e -> setZoom(zoom - STEP));
        zoomIn.addClickListener(e -> setZoom(zoom + STEP));
        Button close = new Button("Tutup", e -> close());
        getFooter().add(zoomLabel, zoomOut, zoomIn, close);
        setZoom(DEFAULT_ZOOM);
    }

    private void setZoom(double value) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, Math.round(value * 10) / 10.0));
        image.getStyle().set("width", Math.round(zoom * 100) + "%");
        image.removeClassName("zoomed");
        if (zoom > DEFAULT_ZOOM) {
            image.addClassName("zoomed");
        }
        zoomLabel.setText(String.format("%.1fx", zoom));
        zoomOut.setEnabled(zoom > MIN_ZOOM);
        zoomIn.setEnabled(zoom < MAX_ZOOM);
    }
}
