package LanguageStudySystem.JavaJapStuSystem;

import java.util.UUID;

/** Stable identity marker stored as an optional trailing JLPT group column. */
final class JLPTGroupItemId {
    private static final String PREFIX = "jss-group-id:";

    private JLPTGroupItemId() {}

    static String create() {
        return UUID.randomUUID().toString();
    }

    static String encode(String value) {
        return isValid(value) ? PREFIX + value : "";
    }

    static String decode(String field) {
        if (field == null || !field.startsWith(PREFIX)) return "";
        String value = field.substring(PREFIX.length());
        return isValid(value) ? value : "";
    }

    static boolean isValid(String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
