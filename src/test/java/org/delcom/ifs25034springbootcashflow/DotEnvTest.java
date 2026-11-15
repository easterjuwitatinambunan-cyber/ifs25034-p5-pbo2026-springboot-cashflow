package org.delcom.ifs25034springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DotEnvTest {

    @Test
    void parsesKeysCommentsAndQuotes() throws IOException {
        Path file = Files.createTempFile("dotenv", ".env");
        Files.writeString(file, "# komentar\n\nCF_TEST_A=satu\nCF_TEST_B=\"dua tiga\"\nCF_TEST_C='empat'\nCF_TEST_D=\nbaris-tanpa-sama\n=nilai\nCF_TEST_E=\"x\n");
        Map<String, String> loaded = DotEnv.load(file);
        assertEquals("satu", loaded.get("CF_TEST_A"));
        assertEquals("dua tiga", loaded.get("CF_TEST_B"));
        assertEquals("empat", loaded.get("CF_TEST_C"));
        assertEquals("", loaded.get("CF_TEST_D"));
        assertEquals("\"x", loaded.get("CF_TEST_E"));
        assertEquals(5, loaded.size());
        assertEquals("satu", System.getProperty("CF_TEST_A"));
    }

    @Test
    void existingPropertyWinsAndMissingFileIsIgnored() throws IOException {
        System.setProperty("CF_TEST_KEEP", "asli");
        Path file = Files.createTempFile("dotenv", ".env");
        Files.writeString(file, "CF_TEST_KEEP=baru\n");
        DotEnv.load(file);
        assertEquals("asli", System.getProperty("CF_TEST_KEEP"));
        assertTrue(DotEnv.load(Path.of("tidak-ada.env")).isEmpty());
        assertNotNull(DotEnv.load());
        assertTrue(DotEnv.load(Files.createTempDirectory("dir")).isEmpty());
    }
}
