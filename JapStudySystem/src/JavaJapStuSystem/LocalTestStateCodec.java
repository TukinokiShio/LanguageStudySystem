package LanguageStudySystem.JavaJapStuSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Version handling for persisted local-batch last-question state. */
final class LocalTestStateCodec {
    static final String CURRENT_VERSION = "3";

    private LocalTestStateCodec() {}

    static boolean supportsVersion(String version) {
        return "2".equals(version) || CURRENT_VERSION.equals(version);
    }

    static Map<String, String> parse(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        if (lines == null) return values;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            int split = line.indexOf('=');
            if (split <= 0) throw new IllegalArgumentException("状态行缺少等号");
            values.put(line.substring(0, split).trim(), line.substring(split + 1).trim());
        }
        return values;
    }

    static long restoreLastAskedId(String version, String value) {
        if (!CURRENT_VERSION.equals(version) || value == null) return 0L;
        try {
            long id = Long.parseLong(value.trim());
            return Math.max(0L, id);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    static String formatLastAskedId(long id) {
        return "lastAskedId=" + Math.max(0L, id);
    }
}
