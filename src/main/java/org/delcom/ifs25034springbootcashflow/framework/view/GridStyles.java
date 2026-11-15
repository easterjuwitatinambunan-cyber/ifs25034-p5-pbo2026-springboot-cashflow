package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;

/** Gaya grid yang konsisten di seluruh halaman. */
final class GridStyles {

    private GridStyles() {
    }

    static void apply(Grid<?> grid, String ariaLabel) {
        grid.addClassName("cf-grid");
        grid.setHeight("26rem");
        grid.getElement().setAttribute("aria-label", ariaLabel);
    }
}
