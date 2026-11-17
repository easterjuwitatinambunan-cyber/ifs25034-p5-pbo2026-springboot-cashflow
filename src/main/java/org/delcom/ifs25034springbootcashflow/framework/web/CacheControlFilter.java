package org.delcom.ifs25034springbootcashflow.framework.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Memberi Cache-Control panjang untuk aset statis berhash (VAADIN/build, styles.css dan lumo.css
 * ber-versi) agar kunjungan ulang cepat dan audit "efficient cache policy" Lighthouse lulus.
 */
public class CacheControlFilter implements Filter {

    static final String IMMUTABLE = "public, max-age=31536000, immutable";
    static final String ONE_WEEK = "public, max-age=604800";
    private static final Pattern HASHED_BUILD_FILE =
            Pattern.compile("^/VAADIN/build/.+-[A-Za-z0-9_-]{8}\\.[A-Za-z0-9]+$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest http && response instanceof HttpServletResponse out) {
            String policy = policyFor(http);
            if (policy != null) {
                out.setHeader("Cache-Control", policy);
                chain.doFilter(request, new FixedCacheResponse(out));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    static String policyFor(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean versioned = request.getParameter("v-c") != null;
        if (HASHED_BUILD_FILE.matcher(path).matches()) {
            return IMMUTABLE;
        }
        if (versioned && (path.equals("/styles.css") || path.startsWith("/lumo/"))) {
            return IMMUTABLE;
        }
        if (path.startsWith("/icons/")) {
            return ONE_WEEK;
        }
        return null;
    }

    /** Mencegah komponen hilir menimpa header cache yang sudah ditetapkan filter ini. */
    private static final class FixedCacheResponse extends HttpServletResponseWrapper {

        FixedCacheResponse(HttpServletResponse response) {
            super(response);
        }

        private static boolean locked(String name) {
            return "Cache-Control".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name)
                    || "Pragma".equalsIgnoreCase(name);
        }

        @Override
        public void setHeader(String name, String value) {
            if (!locked(name)) {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if (!locked(name)) {
                super.addHeader(name, value);
            }
        }

        @Override
        public void setDateHeader(String name, long date) {
            if (!locked(name)) {
                super.setDateHeader(name, date);
            }
        }

        @Override
        public void addDateHeader(String name, long date) {
            if (!locked(name)) {
                super.addDateHeader(name, date);
            }
        }
    }
}
