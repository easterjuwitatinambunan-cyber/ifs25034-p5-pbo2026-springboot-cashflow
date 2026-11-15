package org.delcom.ifs25034springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
@Viewport("width=device-width, initial-scale=1")
public class App implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Cashflow");
        settings.addMetaTag("description",
                "Cashflow: aplikasi pencatat pemasukan, pengeluaran, dan saldo keuangan pribadi.");
    }
}
