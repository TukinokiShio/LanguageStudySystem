package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** 无 GUI 的回归测试入口，供发布前临时编译执行。 */
public final class LocalTestEngineTest {
    private LocalTestEngineTest() {}

    public static void main(String[] args) {
        testGroupSizeAndRatio();
        testMasteryTransitions();
        testHardWordIsNotSelectable();
        testNoImmediateRepeatWhenAlternativesExist();
        System.out.println("LocalTestEngineTest: PASS");
    }

    private static void testGroupSizeAndRatio() {
        List<JapStuJFrame.JaNode> nodes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
            node.localId = i + 1;
            node.enrichState = 1;
            node.examTimes = i < 12 ? 0 : 1;
            nodes.add(node);
        }
        JapStuJFrame.JaNode pending = new JapStuJFrame.JaNode();
        pending.localId = 100;
        pending.enrichState = 0;
        nodes.add(pending);

        List<JapStuJFrame.JaNode> group = LocalTestEngine.chooseGroup(nodes, new Random(7));
        check(group.size() == 15, "临时组应最多包含15个词");
        Set<Long> ids = new HashSet<>();
        int fresh = 0;
        int reviewed = 0;
        for (JapStuJFrame.JaNode node : group) {
            check(ids.add(node.localId), "临时组不应有重复 localId");
            if (node.examTimes == 0) fresh++;
            else reviewed++;
            check(node.enrichState == 1, "待富化词不得进入临时组");
        }
        check(fresh == 9 && reviewed == 6, "新旧词比例应按12:8分配为9:6");
    }

    private static void testMasteryTransitions() {
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
    }

    private static void testHardWordIsNotSelectable() {
        int[] states = {0, 0};
        int[] wrong = {4, 0};
        long[] ids = {1, 2};
        int[] totalWrong = {0, 0};
        int picked = LocalTestEngine.chooseWeightedSlot(
                states, wrong, ids, 2, -1, new ArrayList<>(), new Random(1));
        check(picked == 1, "本组错误超过3次的困难词不得再次抽取");
    }

    private static void testNoImmediateRepeatWhenAlternativesExist() {
        int[] states = {0, 0};
        int[] wrong = {0, 0};
        long[] ids = {1, 2};
        int[] totalWrong = {0, 0};
        List<Integer> recent = new ArrayList<>();
        recent.add(0);
        int picked = LocalTestEngine.chooseWeightedSlot(
                states, wrong, ids, 2, 0, recent, new Random(1));
        check(picked == 1, "存在两个候选时不得连续抽到同一槽位");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
