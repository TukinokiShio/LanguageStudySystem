package LanguageStudySystem.JavaJapStuSystem;

/** 汇总本地词库中已完成掌握的累计词条数。 */
final class LocalMasteryHistory {
    private LocalMasteryHistory() {}

    static int restoreCount(boolean headerParsed, int savedCount) {
        return headerParsed ? Math.max(0, savedCount) : 0;
    }

    static String formatHeader(int totalItems, int masteredItems) {
        return totalItems + "\t" + Math.max(0, masteredItems);
    }

    static int afterMasteredEntryRemoval(int savedCount, int groupState, int groupWrong) {
        int count = Math.max(0, savedCount);
        if (groupState < 2 || groupWrong > LocalTestEngine.HARD_WRONG_LIMIT
                || count == Integer.MAX_VALUE) {
            return count;
        }
        return count + 1;
    }
}
