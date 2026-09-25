package LanguageStudySystem.JavaJapStuSystem;

/** 本地词库历史掌握数的持久化与结算回归测试。 */
public final class LocalMasteryHistoryTest {
    private LocalMasteryHistoryTest() {}

    public static void main(String[] args) {
        testHeaderCountIsAuthoritative();
        testMissingHeaderStartsAtZero();
        testOnlyCompletedMasteryIncrementsHistory();
        System.out.println("LocalMasteryHistoryTest: PASS");
    }

    private static void testHeaderCountIsAuthoritative() {
        int restored = LocalMasteryHistory.restoreCount(true, 29);
        // 临时组状态可以为陌生；它不参与累计历史数的恢复。
        int currentGroupState = 0;
        check(currentGroupState == 0 && restored == 29,
                "读取文件头后不能用当前临时组状态覆盖历史掌握数");
        String savedHeader = LocalMasteryHistory.formatHeader(310, restored);
        String[] fields = savedHeader.split("\\t", -1);
        int reloaded = LocalMasteryHistory.restoreCount(
                fields.length == 2, Integer.parseInt(fields[1]));
        check(reloaded == 29,
                "保存并重读文件头后应保留历史掌握数");
    }

    private static void testMissingHeaderStartsAtZero() {
        check(LocalMasteryHistory.restoreCount(false, 29) == 0,
                "无有效文件头时应从零开始，不能沿用未读到的旧值");
        check(LocalMasteryHistory.restoreCount(true, -1) == 0,
                "负数历史掌握数应归一为零");
    }

    private static void testOnlyCompletedMasteryIncrementsHistory() {
        check(LocalMasteryHistory.afterMasteredEntryRemoval(29, 0, 0) == 29,
                "陌生词不能计入已掌握历史数");
        check(LocalMasteryHistory.afterMasteredEntryRemoval(29, 1, 0) == 29,
                "了解词不能计入已掌握历史数");
        check(LocalMasteryHistory.afterMasteredEntryRemoval(29, 2, 3) == 30,
                "达到掌握状态且未超困难阈值的词条应计数一次");
        check(LocalMasteryHistory.afterMasteredEntryRemoval(29, 2, 4) == 29,
                "超过困难阈值的词条不应计入已完成掌握数");
        check(LocalMasteryHistory.afterMasteredEntryRemoval(Integer.MAX_VALUE, 2, 0)
                        == Integer.MAX_VALUE,
                "计数达到整数上限时不应溢出");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
