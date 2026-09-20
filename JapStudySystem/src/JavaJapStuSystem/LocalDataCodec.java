package LanguageStudySystem.JavaJapStuSystem;

/**
 * 本地词库 7/12 列格式的纯数据编解码器。
 *
 * <p>不依赖 Swing，专门用于保证旧文件迁移和新文件回读的一致性。</p>
 */
final class LocalDataCodec {
    private static final int LOCAL_COLUMNS = 12;

    private LocalDataCodec() {}

    static JapStuJFrame.JaNode parse(String line, boolean jlpt) {
        String[] sp = line == null ? new String[0] : line.split("\\t", -1);
        JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
        node.japanese = field(sp, 0);
        node.chinese = field(sp, 1);
        node.type = intValue(sp, 2, 1);
        node.examTimes = intValue(sp, 3, 0);
        node.trueTimes = intValue(sp, 4, 0);
        node.example = field(sp, 5);
        node.exampleCh = field(sp, 6);
        node.example2 = field(sp, 8);
        node.example2Ch = field(sp, 9);
        // 本地 masteryState 只属于当前临时组；旧 7 列不从历史统计推导熟练度。
        node.masteryState = sp.length > 7 ? clampMastery(intValue(sp, 7, 0)) : 0;
        node.enrichState = jlpt ? 1 : (intValue(sp, 10, 0) == 1 ? 1 : 0);
        node.localId = jlpt ? 0L : longValue(sp, 11, 0L);
        node.jlptLevel = 0;
        node.wrongTimes = 0;
        return node;
    }

    static String serialize(JapStuJFrame.JaNode node) {
        if (node == null) return "";
        return safe(node.japanese) + "\t" + safe(node.chinese) + "\t" + node.type + "\t"
                + node.examTimes + "\t" + node.trueTimes + "\t" + safe(node.example)
                + "\t" + safe(node.exampleCh) + "\t" + clampMastery(node.masteryState)
                + "\t" + safe(node.example2) + "\t" + safe(node.example2Ch)
                + "\t" + (node.enrichState == 1 ? 1 : 0) + "\t" + node.localId;
    }

    static int columnCount(String line) {
        return line == null ? 0 : line.split("\\t", -1).length;
    }

    static int expectedColumnCount() {
        return LOCAL_COLUMNS;
    }

    private static String field(String[] values, int index) {
        return values != null && index >= 0 && index < values.length ? values[index] : "";
    }

    private static int intValue(String[] values, int index, int fallback) {
        try {
            return Integer.parseInt(field(values, index).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static long longValue(String[] values, int index, long fallback) {
        try {
            return Long.parseLong(field(values, index).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clampMastery(int value) {
        return Math.max(0, Math.min(2, value));
    }

    private static String safe(String value) {
        if (value == null) return "";
        return value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }
}
