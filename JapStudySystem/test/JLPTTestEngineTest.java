package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** No-GUI regression coverage for fair JLPT group selection. */
public final class JLPTTestEngineTest {
    private static final String ID_A = "00000000-0000-0000-0000-000000000001";
    private static final String ID_B = "00000000-0000-0000-0000-000000000002";

    private JLPTTestEngineTest() {}

    public static void main(String[] args) {
        testLastWordExcludedWhenAnotherIdentityExists();
        testOnlyIdentityMayRepeat();
        testMasteredEntriesAreNotCandidates();
        testRecentWeightRemovedButErrorWeightRemains();
        System.out.println("JLPTTestEngineTest: PASS");
    }

    private static void testLastWordExcludedWhenAnotherIdentityExists() {
        List<JapStuJFrame.JaNode> group = new ArrayList<>();
        group.add(node(ID_A, 0, 0));
        group.add(node(ID_B, 0, 0));
        JapStuJFrame.JaNode selected = JLPTTestEngine.choose(group, ID_A, new Random(1));
        check(ID_B.equals(selected.jlptGroupItemId),
                "至少有两个可测词时不得连续抽中上一个词");
    }

    private static void testOnlyIdentityMayRepeat() {
        List<JapStuJFrame.JaNode> group = new ArrayList<>();
        group.add(node(ID_A, 0, 0));
        group.add(node(ID_A, 0, 0));
        JapStuJFrame.JaNode selected = JLPTTestEngine.choose(group, ID_A, new Random(2));
        check(selected != null && ID_A.equals(selected.jlptGroupItemId),
                "同一词条的重复副本不能阻止唯一可测词再次出现");
    }

    private static void testMasteredEntriesAreNotCandidates() {
        List<JapStuJFrame.JaNode> group = new ArrayList<>();
        group.add(node(ID_A, 2, 0));
        group.add(node(ID_B, 0, 0));
        JapStuJFrame.JaNode selected = JLPTTestEngine.choose(group, ID_A, new Random(3));
        check(selected != null && ID_B.equals(selected.jlptGroupItemId),
                "已掌握词应退出候选集合，剩余词可正常抽取");
    }

    private static void testRecentWeightRemovedButErrorWeightRemains() {
        check(JLPTTestEngine.weight(0, 0) == 5.0,
                "陌生词权重应保留基础值，不再叠加近期抽中加权");
        check(JLPTTestEngine.weight(1, 2) == 8.0,
                "了解词仍应保留错题优先级");
    }

    private static JapStuJFrame.JaNode node(String id, int mastery, int wrong) {
        JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
        node.jlptGroupItemId = id;
        node.masteryState = mastery;
        node.wrongTimes = wrong;
        return node;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
