package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Pure weighted selection rules for one JLPT group. */
final class JLPTTestEngine {
    private JLPTTestEngine() {}

    static double weight(int masteryState, int wrongTimes) {
        double result = masteryState == 0 ? 5.0 : 2.0;
        result += Math.max(0, wrongTimes) * 3.0;
        return Math.max(0.01, result);
    }

    static JapStuJFrame.JaNode choose(List<JapStuJFrame.JaNode> group,
                                      String lastAskedId, Random random) {
        List<JapStuJFrame.JaNode> candidates = new ArrayList<>();
        Set<String> distinctIds = new HashSet<>();
        if (group != null) {
            for (JapStuJFrame.JaNode node : group) {
                if (node == null || node.masteryState >= 2
                        || !JLPTGroupItemId.isValid(node.jlptGroupItemId)) continue;
                candidates.add(node);
                distinctIds.add(node.jlptGroupItemId);
            }
        }
        if (candidates.isEmpty()) return null;

        if (distinctIds.size() >= 2 && JLPTGroupItemId.isValid(lastAskedId)) {
            candidates.removeIf(node -> lastAskedId.equals(node.jlptGroupItemId));
        }
        if (candidates.isEmpty()) return null;

        double totalWeight = 0.0;
        for (JapStuJFrame.JaNode node : candidates) {
            totalWeight += weight(node.masteryState, node.wrongTimes);
        }
        Random rng = random == null ? new Random() : random;
        double roll = rng.nextDouble() * totalWeight;
        for (JapStuJFrame.JaNode node : candidates) {
            roll -= weight(node.masteryState, node.wrongTimes);
            if (roll <= 0.0) return node;
        }
        return candidates.get(candidates.size() - 1);
    }
}
