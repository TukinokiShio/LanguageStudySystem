package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** 无 GUI 的回归测试入口，供发布前临时编译执行。 */
public final class LocalTestEngineTest {
    private LocalTestEngineTest() {}

    public static void main(String[] args) {
        testSmallGroupsIncludeAllEligibleWords();
        testRecentFiveAndBothSegmentsAreSampledWithoutReplacement();
        testShortSecondSegmentDoesNotBorrowFromFirst();
        testUnaskedUnfamiliarWordsAreCoveredFirst();
        testUnfamiliarWordsWaitBeforeKnownWords();
        testKnownStageStartsAfterUnfamiliarStage();
        testNoImmediateRepeatWhenAlternativesExist();
        testOnlyCandidateMayRepeat();
        testUnavailablePreviousWordDoesNotBlockSelection();
        testMasteryTransitionsAndCorrectThresholds();
        testHardWordRemainsKnownAndCompletesAtCorrectThreshold();
        testArchiveEligibility();
        System.out.println("LocalTestEngineTest: PASS");
    }

    private static void testSmallGroupsIncludeAllEligibleWords() {
        for (int count : new int[] {0, 1, 15}) {
            List<JapStuJFrame.JaNode> nodes = nodes(count);
            List<JapStuJFrame.JaNode> group = LocalTestEngine.chooseGroup(nodes, new Random(7));
            check(group.size() == count, "15条及以下应纳入全部可测词");
            check(ids(group).size() == count, "小组内 localId 不应重复");
        }
    }

    private static void testRecentFiveAndBothSegmentsAreSampledWithoutReplacement() {
        List<JapStuJFrame.JaNode> nodes = nodes(20);
        Collections.reverse(nodes);
        JapStuJFrame.JaNode pending = node(21, 0);
        nodes.add(pending);

        List<JapStuJFrame.JaNode> group = LocalTestEngine.chooseGroup(nodes, new Random(7));
        Set<Long> selected = ids(group);
        check(group.size() == 15, "20条可测词应组成15词临时组");
        check(selected.size() == group.size(), "临时组不得重复抽取");
        for (long id = 16; id <= 20; id++) {
            check(selected.contains(id), "最近追加的5条词必须进入临时组");
        }
        check(countInRange(selected, 1, 10) == 5,
                "剩余词前70%分段应随机抽取5条");
        check(countInRange(selected, 11, 15) == 5,
                "剩余词后30%分段应随机抽取5条");
        check(!selected.contains(21L), "待富化词不得进入临时组");
    }

    private static void testShortSecondSegmentDoesNotBorrowFromFirst() {
        for (int count = 16; count <= 18; count++) {
            List<JapStuJFrame.JaNode> group = LocalTestEngine.chooseGroup(
                    nodes(count), new Random(count));
            Set<Long> selected = ids(group);
            int remaining = count - 5;
            int split = (int) Math.floor(remaining * 0.70d);
            int secondStart = split + 1;
            check(group.size() == 14,
                    "16至18条可测词时，后30%不足5条，不应从前段补位");
            check(countInRange(selected, 1, split) == 5,
                    "前70%分段仍应抽取5条");
            for (int id = secondStart; id <= remaining; id++) {
                check(selected.contains((long) id), "后30%不足5条时应全部纳入");
            }
            for (int id = count - 4; id <= count; id++) {
                check(selected.contains((long) id), "最近追加的5条词必须纳入");
            }
        }
    }

    private static void testUnaskedUnfamiliarWordsAreCoveredFirst() {
        int picked = choose(
                new int[] {0, 0, 1}, new int[] {1, 0, 0}, new int[] {0, 0, 0},
                new long[] {1, 2, 3}, 0, 1);
        check(picked == 1, "已答错的陌生词应等其他未出题陌生词覆盖后再抽");
    }

    private static void testUnfamiliarWordsWaitBeforeKnownWords() {
        int picked = choose(
                new int[] {0, 1}, new int[] {1, 0}, new int[] {0, 0},
                new long[] {1, 2}, -1, 2);
        check(picked == 0, "仍有陌生词时，了解词应等待");
    }

    private static void testKnownStageStartsAfterUnfamiliarStage() {
        int picked = choose(
                new int[] {1, 1}, new int[] {1, 0}, new int[] {1, 0},
                new long[] {1, 2}, 0, 3);
        check(picked == 1, "全组达到了解后，应在未完成了解词中随机抽题");
    }

    private static void testNoImmediateRepeatWhenAlternativesExist() {
        int picked = choose(
                new int[] {0, 0}, new int[] {1, 1}, new int[] {0, 0},
                new long[] {1, 2}, 0, 1);
        check(picked == 1, "存在两个候选时不得连续抽到同一槽位");
    }

    private static void testOnlyCandidateMayRepeat() {
        int picked = choose(
                new int[] {0}, new int[] {1}, new int[] {0},
                new long[] {7}, 0, 2);
        check(picked == 0, "只剩一个可测词时应允许再次抽到该词");
    }

    private static void testUnavailablePreviousWordDoesNotBlockSelection() {
        int picked = choose(
                new int[] {2, 0, 0}, new int[] {0, 0, 0}, new int[] {0, 0, 0},
                new long[] {7, 8, 9}, 0, 2);
        check(picked == 1 || picked == 2,
                "上次抽中的词已不可测时仍应从当前可测词中抽取");
    }

    private static void testMasteryTransitionsAndCorrectThresholds() {
        check(LocalTestEngine.nextMasteryState(0, 1, 0) == 1,
                "陌生答对一次应变为了解");
        check(LocalTestEngine.requiredCorrect(1, 0) == 2,
                "了解阶段初始需要2次正确");
        check(LocalTestEngine.requiredCorrect(1, 2) == 3,
                "错误两次后需要3次正确");
        check(LocalTestEngine.nextMasteryState(1, 1, 0) == 1,
                "了解阶段一次正确不应直接掌握");
        check(LocalTestEngine.nextMasteryState(1, 2, 0) == 2,
                "了解阶段达到正确次数应掌握");
        check(LocalTestEngine.nextMasteryState(1, 2, 2) == 1,
                "错误两次时未达到额外正确次数不应掌握");
        check(LocalTestEngine.remainingCorrect(1, 1, 2) == 2,
                "了解阶段答对1次、答错2次后还需答对2次");
    }

    private static void testHardWordRemainsKnownAndCompletesAtCorrectThreshold() {
        check(LocalTestEngine.nextMasteryState(0, 0, 3) == 0,
                "本组答错3次仍处于陌生状态");
        check(LocalTestEngine.nextMasteryState(0, 0, 4) == 1,
                "本组答错超过3次后状态应封顶为了解");
        check(LocalTestEngine.nextMasteryState(2, 4, 4) == 1,
                "超过3次错误时，即使旧状态异常为掌握也应封顶为了解");
        check(LocalTestEngine.nextMasteryState(1, 4, 4) == 1,
                "困难词达到正确次数后仍只能记为了解");
        check(LocalTestEngine.requiredCorrect(1, 4) == 4,
                "困难词错误4次时需要答对4次");
        check(!LocalTestEngine.isSlotComplete(1, 3, 4),
                "困难词未达到本组正确次数前仍应继续抽取");
        check(LocalTestEngine.isSlotComplete(1, 4, 4),
                "困难词达到本组正确次数后应完成本组");
        check(LocalTestEngine.remainingCorrect(1, 3, 4) == 1,
                "困难词答错4次后还需答对1次才完成本组");

        check(choose(new int[] {1}, new int[] {4}, new int[] {3},
                new long[] {7}, -1, 1) == 0,
                "困难词未达标时应继续作为候选");
        check(choose(new int[] {1}, new int[] {4}, new int[] {4},
                new long[] {7}, -1, 1) == -1,
                "困难词达标后本组不应再次抽到");
    }

    private static void testArchiveEligibility() {
        check(LocalTestEngine.shouldArchive(2, 3),
                "正常达到掌握且错误不超过3次的词应归档");
        check(!LocalTestEngine.shouldArchive(1, 4),
                "困难词即使完成本组也不得归档");
        check(!LocalTestEngine.shouldArchive(2, 4),
                "本组错误超过3次的词不得归档");
    }

    private static int choose(int[] states, int[] wrong, int[] correct,
                              long[] ids, int lastSlot, long seed) {
        return LocalTestEngine.chooseSlot(states, wrong, correct, ids,
                ids.length, lastSlot, new Random(seed));
    }

    private static List<JapStuJFrame.JaNode> nodes(int count) {
        List<JapStuJFrame.JaNode> nodes = new ArrayList<>();
        for (int i = 1; i <= count; i++) nodes.add(node(i, 1));
        return nodes;
    }

    private static JapStuJFrame.JaNode node(long id, int enrichState) {
        JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
        node.localId = id;
        node.enrichState = enrichState;
        return node;
    }

    private static Set<Long> ids(List<JapStuJFrame.JaNode> nodes) {
        Set<Long> ids = new HashSet<>();
        for (JapStuJFrame.JaNode node : nodes) ids.add(node.localId);
        return ids;
    }

    private static int countInRange(Set<Long> ids, long first, long last) {
        int count = 0;
        for (long id = first; id <= last; id++) {
            if (ids.contains(id)) count++;
        }
        return count;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
