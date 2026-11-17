package org.delcom.ifs25034springbootcashflow.framework;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.ifs25034springbootcashflow.framework.web.CacheControlFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CacheControlFilterTest {

    private final CacheControlFilter filter = new CacheControlFilter();

    private MockHttpServletResponse run(MockHttpServletRequest request, FilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private static final FilterChain OVERRIDING_CHAIN = (req, res) -> {
        HttpServletResponse out = (HttpServletResponse) res;
        out.setHeader("Cache-Control", "no-cache");
        out.addHeader("Cache-Control", "no-store");
        out.setHeader("Pragma", "no-cache");
        out.setDateHeader("Expires", 0L);
        out.addDateHeader("Expires", 0L);
        out.setHeader("X-Test", "ok");
        out.addHeader("X-Other", "yes");
        out.setDateHeader("Last-Modified", 1000L);
        out.addDateHeader("X-Date", 2000L);
    };

    @Test
    void hashedBuildFilesGetImmutableCacheAndCannotBeOverridden() throws Exception {
        MockHttpServletResponse response = run(
                new MockHttpServletRequest("GET", "/VAADIN/build/generated-flow-imports-B-aQruVm.js"),
                OVERRIDING_CHAIN);
        assertEquals("public, max-age=31536000, immutable", response.getHeader("Cache-Control"));
        assertNull(response.getHeader("Pragma"));
        assertNull(response.getHeader("Expires"));
        assertEquals("ok", response.getHeader("X-Test"));
        assertEquals("yes", response.getHeader("X-Other"));
        assertNotNull(response.getHeader("Last-Modified"));
        assertNotNull(response.getHeader("X-Date"));
    }

    @Test
    void versionedStylesheetsAreCachedLong() throws Exception {
        MockHttpServletRequest styles = new MockHttpServletRequest("GET", "/styles.css");
        styles.setParameter("v-c", "abc");
        assertEquals("public, max-age=31536000, immutable", run(styles, OVERRIDING_CHAIN).getHeader("Cache-Control"));
        MockHttpServletRequest lumo = new MockHttpServletRequest("GET", "/lumo/lumo.css");
        lumo.setParameter("v-c", "abc");
        assertEquals("public, max-age=31536000, immutable", run(lumo, OVERRIDING_CHAIN).getHeader("Cache-Control"));
    }

    @Test
    void iconsGetOneWeek() throws Exception {
        MockHttpServletResponse response = run(new MockHttpServletRequest("GET", "/icons/icon.png"), OVERRIDING_CHAIN);
        assertEquals("public, max-age=604800", response.getHeader("Cache-Control"));
    }

    @Test
    void otherRequestsAreLeftAlone() throws Exception {
        MockHttpServletResponse unversioned = run(new MockHttpServletRequest("GET", "/styles.css"), OVERRIDING_CHAIN);
        assertEquals("no-cache", unversioned.getHeaders("Cache-Control").get(0));
        MockHttpServletResponse page = run(new MockHttpServletRequest("GET", "/login"), OVERRIDING_CHAIN);
        assertEquals("no-cache", page.getHeaders("Cache-Control").get(0));
        MockHttpServletResponse unhashed = run(
                new MockHttpServletRequest("GET", "/VAADIN/build/vaadin-bundle.json"), OVERRIDING_CHAIN);
        assertEquals("no-cache", unhashed.getHeaders("Cache-Control").get(0));
    }

    @Test
    void nonHttpRequestsPassThrough() throws Exception {
        boolean[] called = {false};
        filter.doFilter(new org.springframework.mock.web.MockHttpServletRequest(), null,
                (req, res) -> called[0] = true);
        assertTrue(called[0]);
    }
}
