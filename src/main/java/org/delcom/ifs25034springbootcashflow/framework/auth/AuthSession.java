package org.delcom.ifs25034springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.delcom.ifs25034springbootcashflow.domain.entity.User;
import org.delcom.ifs25034springbootcashflow.usecase.UserUseCase;

/**
 * Sesi login: ID pengguna disimpan di VaadinSession (bertahan setelah refresh) dan cookie
 * bertanda tangan HMAC (memulihkan sesi setelah restart server). LOGGED_OUT_KEY mencegah
 * pemulihan otomatis setelah logout.
 */
public class AuthSession {

    public static final String COOKIE_NAME = "cashflow_uid";
    static final String USER_KEY = "cashflow.userId";
    static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    private final UserUseCase users;
    private final byte[] secret;

    public AuthSession(UserUseCase users, String secret) {
        this.users = users;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public Optional<User> currentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return Optional.empty();
        }
        Object stored = session.getAttribute(USER_KEY);
        Integer id = stored instanceof Integer value ? value : idFromCookie();
        if (id == null) {
            return Optional.empty();
        }
        Optional<User> user = users.find(id);
        if (user.isPresent()) {
            session.setAttribute(USER_KEY, id);
        } else {
            session.setAttribute(USER_KEY, null);
        }
        return user;
    }

    public boolean isLoggedIn() {
        return currentUser().isPresent();
    }

    public void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, null);
        session.setAttribute(USER_KEY, user.getId());
        writeCookie(user.getId() + "." + sign(String.valueOf(user.getId())), COOKIE_MAX_AGE);
    }

    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        writeCookie("", 0);
    }

    private Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (!(request instanceof VaadinServletRequest servletRequest) || servletRequest.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : servletRequest.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    private Integer verify(String value) {
        int dot = value == null ? -1 : value.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idText = value.substring(0, dot);
        String expected = sign(idText);
        boolean valid = MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                value.substring(dot + 1).getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            return null;
        }
        try {
            return Integer.valueOf(idText);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sign(String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC tidak tersedia", e);
        }
    }

    private void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response instanceof VaadinServletResponse servletResponse) {
            Cookie cookie = new Cookie(COOKIE_NAME, value);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            VaadinRequest request = VaadinService.getCurrentRequest();
            cookie.setSecure(request != null && request.isSecure());
            cookie.setAttribute("SameSite", "Lax");
            servletResponse.addCookie(cookie);
        }
    }
}
