package LanguageStudySystem.JavaJapStuSystem;

/** 本地词库旧格式迁移、新格式保存和回读的无 GUI 回归测试。 */
public final class LocalDataCodecTest {
    private LocalDataCodecTest() {}

    public static void main(String[] args) {
        testLegacySevenColumns();
        testTwelveColumnRoundTrip();
        testDuplicateIdsRemainDistinct();
        System.out.println("LocalDataCodecTest: PASS");
    }

    private static void testLegacySevenColumns() {
        JapStuJFrame.JaNode node = LocalDataCodec.parse(
                "学校\t学校\t1\t8\t5\t例句\t译文", false);
        check(node.examTimes == 8 && node.trueTimes == 5,
                "旧7列历史统计必须保留");
        check(node.masteryState == 0 && node.enrichState == 0 && node.localId == 0L,
                "旧7列必须迁移为陌生、待富化、待分配编号");
        check(LocalDataCodec.columnCount(LocalDataCodec.serialize(node))
                        == LocalDataCodec.expectedColumnCount(),
                "旧7列保存后必须扩展为12列");
    }

    private static void testTwelveColumnRoundTrip() {
        JapStuJFrame.JaNode source = new JapStuJFrame.JaNode();
        source.japanese = "今日";
        source.chinese = "今天";
        source.type = 1;
        source.examTimes = 11;
        source.trueTimes = 7;
        source.example = "今日（きょう）は晴（は）れです。";
        source.exampleCh = "今天是晴天。";
        source.masteryState = 1;
        source.example2 = "今日（きょう）も勉強（べんきょう）します。";
        source.example2Ch = "今天也学习。";
        source.enrichState = 1;
        source.localId = 42L;

        JapStuJFrame.JaNode restored = LocalDataCodec.parse(
                LocalDataCodec.serialize(source), false);
        check(restored.japanese.equals(source.japanese)
                        && restored.example2Ch.equals(source.example2Ch)
                        && restored.localId == 42L
                        && restored.enrichState == 1
                        && restored.masteryState == 1,
                "12列词条保存后必须完整回读");
    }

    private static void testDuplicateIdsRemainDistinct() {
        JapStuJFrame.JaNode first = LocalDataCodec.parse(
                "同じ\t一\t1\t0\t0\t\t\t0\t\t\t1\t7", false);
        JapStuJFrame.JaNode second = LocalDataCodec.parse(
                "同じ\t二\t1\t0\t0\t\t\t0\t\t\t1\t8", false);
        check(first.japanese.equals(second.japanese) && first.localId != second.localId,
                "重复日文必须由不同localId区分");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
