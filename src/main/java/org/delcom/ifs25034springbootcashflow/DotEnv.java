package org.delcom.ifs25034springbootcashflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Memuat berkas .env ke system properties sebelum Spring Boot dijalankan. */
public final class DotEnv {

    private DotEnv() {
    }

    public static Map<String, String> load() {
        return load(Path.of(".env"));
    }

    /**
     * Membaca KEY=VALUE per baris. Variabel environment OS dan system property yang sudah ada
     * tidak ditimpa, sehingga konfigurasi server cloud tetap menang atas .env lokal.
     */
    public static Map<String, String> load(Path file) {
        Map<String, String> loaded = new LinkedHashMap<>();
        if (!Files.isRegularFile(file)) {
            return loaded;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return loaded;
        }
        for (String raw : lines) {
            String line = raw.trim();
            int eq = line.indexOf('=');
            if (line.isEmpty() || line.startsWith("#") || eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            loaded.put(key, value);
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
        return loaded;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
