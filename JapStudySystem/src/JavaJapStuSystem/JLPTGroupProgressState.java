package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.List;

/** Backward-compatible parser/formatter for per-level JLPT group progress. */
final class JLPTGroupProgressState {
    static final class Snapshot {
        final int groupIndex;
        final String lastAskedId;

        Snapshot(int groupIndex, String lastAskedId) {
            this.groupIndex = groupIndex;
            this.lastAskedId = lastAskedId;
        }
    }

    private JLPTGroupProgressState() {}

    static Snapshot parse(List<String> lines) {
        int groupIndex = 1;
        String lastAskedId = "";
        if (lines != null && !lines.isEmpty()) {
            try {
                groupIndex = Math.max(1, Integer.parseInt(lines.get(0).trim()));
            } catch (RuntimeException ignored) {
                groupIndex = 1;
            }
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line != null && line.startsWith("lastAskedId=")) {
                    lastAskedId = line.substring("lastAskedId=".length()).trim();
                    if (!JLPTGroupItemId.isValid(lastAskedId)) lastAskedId = "";
                    break;
                }
            }
        }
        return new Snapshot(groupIndex, lastAskedId);
    }

    static List<String> format(int groupIndex, String lastAskedId) {
        List<String> lines = new ArrayList<>();
        lines.add(Integer.toString(Math.max(1, groupIndex)));
        lines.add("lastAskedId=" + (JLPTGroupItemId.isValid(lastAskedId) ? lastAskedId : ""));
        return lines;
    }
}
