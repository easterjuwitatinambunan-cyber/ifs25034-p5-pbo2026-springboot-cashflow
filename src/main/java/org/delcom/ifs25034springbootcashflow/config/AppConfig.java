package org.delcom.ifs25034springbootcashflow.config;

import javax.sql.DataSource;
import org.delcom.ifs25034springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25034springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25034springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25034springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25034springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25034springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25034springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25034springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25034springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25034springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Composition Root: seluruh wiring bean aplikasi berada di sini. */
@Configuration
public class AppConfig {

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    @DependsOn("userRepository")
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository users, PasswordHasher hasher) {
        return new AuthUseCase(users, hasher);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactions, IUserRepository users) {
        return new FinanceUseCase(transactions, users);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository users, PasswordHasher hasher) {
        return new UserUseCase(users, hasher);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public AuthSession authSession(UserUseCase userUseCase, @Value("${app.session-secret}") String secret) {
        return new AuthSession(userUseCase, secret);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
                                           @Value("${app.root-username}") String rootUsername,
                                           @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Bean
    public WebMvcConfigurer uploadsResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String location = fileStorage.root().toUri().toString();
                if (!location.endsWith("/")) {
                    location = location + "/";
                }
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(location)
                        .setCachePeriod(3600);
            }
        };
    }
}
