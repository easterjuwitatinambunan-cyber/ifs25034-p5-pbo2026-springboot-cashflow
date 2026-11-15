package org.delcom.ifs25034springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.domain.entity.FinanceSummary;

/** Kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, dan saldo. */
public class SummaryCards extends Div {

    private final FinancePresenter presenter;
    private final Span count = new Span("0");
    private final Span income = new Span("Rp 0.00");
    private final Span expense = new Span("Rp 0.00");
    private final Span balance = new Span("Rp 0.00");

    public SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        addClassName("summary-grid");
        add(card("Total Transaksi", count, "summary-count", ""),
                card("Total Pemasukan", income, "summary-income", "income"),
                card("Total Pengeluaran", expense, "summary-expense", "expense"),
                card("Saldo", balance, "summary-balance", "balance"));
    }

    public void update(FinanceSummary summary) {
        count.setText(String.valueOf(summary.count()));
        income.setText(presenter.money(summary.totalIncome()));
        expense.setText(presenter.money(summary.totalExpense()));
        balance.setText(presenter.money(summary.balance()));
        balance.removeClassName("negative");
        if (summary.balance() < 0) {
            balance.addClassName("negative");
        }
    }

    private static Div card(String label, Span value, String id, String variant) {
        Span caption = new Span(label);
        caption.addClassName("summary-label");
        value.setId(id);
        value.addClassName("summary-value");
        Div card = new Div(caption, value);
        card.addClassName("summary-card");
        if (!variant.isEmpty()) {
            card.addClassName(variant);
        }
        return card;
    }
}
