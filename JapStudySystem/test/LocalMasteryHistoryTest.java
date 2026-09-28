package LanguageStudySystem.JavaJapStuSystem;

/** 本地词库历史掌握数的持久化与结算回归测试。 */
public final class LocalMasteryHistoryTest {
    private LocalMasteryHistoryTest() {}

    public static void main(String[] args) {
        testHeaderCountIsAuthoritative();
        testMissingHeaderStartsAtZero();
        testOnlyCompletedMasteryIncrementsHistory();
        testProgressIncludesPendingAndCurrentMastery();
        testProgressRemainsContinuousAfterSettlement();
        testEmptyAndHistoricalProgressBoundaries();
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

    private static void testProgressIncludesPendingAndCurrentMastery() {
        JapStuJFrame.JaNode head = new JapStuJFrame.JaNode();
        head.next = localNode(0, 0);
        head.next.next = localNode(1, 1);
        head.next.next.next = localNode(1, 0);
        head.next.next.next.next = localNode(1, 2);

        LocalProgressSnapshot progress = LocalProgressSnapshot.calculate(head, 3);
        check(progress.liveEntries == 4 && progress.pendingEnrich == 1,
                "待富化词也属于现存词条与总量");
        check(progress.total == 7 && progress.mastered == 4,
                "总量应含历史掌握，掌握段应含历史与当前未结算掌握");
        check(progress.known == 1 && progress.strange == 2,
                "了解与陌生数只统计当前现存词条");
    }

    private static void testProgressRemainsContinuousAfterSettlement() {
        JapStuJFrame.JaNode before = new JapStuJFrame.JaNode();
        before.next = localNode(1, 2);
        before.next.next = localNode(1, 1);
        before.next.next.next = localNode(1, 0);
        LocalProgressSnapshot active = LocalProgressSnapshot.calculate(before, 2);

        JapStuJFrame.JaNode after = new JapStuJFrame.JaNode();
        after.next = localNode(1, 1);
        after.next.next = localNode(1, 0);
        LocalProgressSnapshot settled = LocalProgressSnapshot.calculate(after, 3);
        check(active.total == settled.total && active.mastered == settled.mastered,
                "掌握词从现存词库结算删除后，总量和掌握进度应保持连续");
    }

    private static void testEmptyAndHistoricalProgressBoundaries() {
        LocalProgressSnapshot empty = LocalProgressSnapshot.calculate(new JapStuJFrame.JaNode(), 0);
        check(empty.total == 0 && empty.mastered == 0 && empty.strange == 0,
                "空词库且没有历史记录时进度应为零");
        LocalProgressSnapshot historyOnly = LocalProgressSnapshot.calculate(
                new JapStuJFrame.JaNode(), 5);
        check(historyOnly.total == 5 && historyOnly.mastered == 5,
                "现存词库为空但保留历史累计数时，历史数应全部显示为已掌握");

        LocalProgressSnapshot maxHistory = LocalProgressSnapshot.calculateCounts(
                2, Integer.MAX_VALUE, 9, 9, 9);
        check(maxHistory.total == (long) Integer.MAX_VALUE + 2
                        && maxHistory.mastered == (long) Integer.MAX_VALUE + 2
                        && maxHistory.known == 0 && maxHistory.strange == 0,
                "历史计数边界与超范围分类输入应安全归一化");
        LocalProgressSnapshot negative = LocalProgressSnapshot.calculateCounts(-1, -9, -2, -3, -4);
        check(negative.total == 0 && negative.mastered == 0 && negative.pendingEnrich == 0,
                "负数历史或计数不得造成负进度");
    }

    private static JapStuJFrame.JaNode localNode(int enrichState, int masteryState) {
        JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
        node.enrichState = enrichState;
        node.masteryState = masteryState;
        return node;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
