package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 本地词库临时组的纯规则部分。组成员、组错误次数和组熟练度由窗口层保存，
 * 这里不读取文件，也不依赖 Swing，方便做确定性边界测试。
 */
final class LocalTestEngine {
    static final int BATCH_SIZE = 15;
    static final int HARD_WRONG_LIMIT = 3;

    private LocalTestEngine() {}

    static int requiredCorrect(int masteryState, int groupWrong) {
        if (masteryState == 0) return 1;
        if (masteryState == 1) return 2 + Math.max(0, groupWrong) / 2;
        return 0;
    }

    static int remainingCorrect(int masteryState, int groupCorrect, int groupWrong) {
        if (masteryState >= 2) return 0;
        return Math.max(0, requiredCorrect(masteryState, groupWrong)
                - Math.max(0, groupCorrect));
    }

    static int nextMasteryState(int masteryState, int groupCorrect, int groupWrong) {
        if (masteryState == 0 && groupCorrect >= 1) return 1;
        if (masteryState == 1
                && groupCorrect >= requiredCorrect(1, groupWrong)) return 2;
        return Math.max(0, Math.min(2, masteryState));
    }

    static boolean isEnriched(JapStuJFrame.JaNode node) {
        return node != null && node.enrichState == 1;
    }

    static double weight(int masteryState, int groupWrong, int recencyDistance) {
        double result = masteryState == 0 ? 5.0 : 2.0;
        // 只使用当前组信息；历史 examTimes/trueTimes 不参与掌握或抽题权重。
        result += Math.max(0, groupWrong) * 3.0;
        if (recencyDistance > 0 && recencyDistance <= 10) {
            result += (11 - recencyDistance) * 1.5;
        }
        return Math.max(0.01, result);
    }

    static int chooseWeightedSlot(int[] states, int[] groupWrong,
                                  long[] ids, int size,
                                  int lastSlot, List<Integer> recentSlots,
                                  Random random) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (ids[i] <= 0L || states[i] >= 2) continue;
            if (groupWrong[i] > HARD_WRONG_LIMIT) continue;
            candidates.add(i);
        }
        if (candidates.size() >= 2 && candidates.contains(lastSlot)) {
            candidates.remove(Integer.valueOf(lastSlot));
        }
        if (candidates.isEmpty()) return -1;

        double total = 0.0;
        for (int slot : candidates) {
            total += weight(states[slot], groupWrong[slot], recencyDistance(slot, recentSlots));
        }
        Random rng = random == null ? new Random() : random;
        double roll = rng.nextDouble() * total;
        for (int slot : candidates) {
            roll -= weight(states[slot], groupWrong[slot], recencyDistance(slot, recentSlots));
            if (roll <= 0) return slot;
        }
        return candidates.get(candidates.size() - 1);
    }

    private static int recencyDistance(int slot, List<Integer> recentSlots) {
        if (recentSlots == null) return 0;
        int index = recentSlots.lastIndexOf(slot);
        return index < 0 ? 0 : recentSlots.size() - index;
    }

    static List<JapStuJFrame.JaNode> chooseGroup(List<JapStuJFrame.JaNode> all,
                                                  Random random) {
        List<JapStuJFrame.JaNode> eligible = new ArrayList<>();
        if (all != null) {
            for (JapStuJFrame.JaNode node : all) {
                // masteryState 对本地词库只表示当前临时组状态，下一组重新从陌生开始。
                if (isEnriched(node)) eligible.add(node);
            }
        }
        int size = Math.min(BATCH_SIZE, eligible.size());
        List<JapStuJFrame.JaNode> result = new ArrayList<>();
        if (size == 0) return result;

        List<JapStuJFrame.JaNode> fresh = new ArrayList<>();
        List<JapStuJFrame.JaNode> reviewed = new ArrayList<>();
        for (JapStuJFrame.JaNode node : eligible) {
            if (node.examTimes == 0) fresh.add(node);
            else reviewed.add(node);
        }

        int freshTarget = (int) Math.round((double) size * fresh.size() / eligible.size());
        if (!fresh.isEmpty() && !reviewed.isEmpty() && size >= 2) {
            freshTarget = Math.max(1, Math.min(size - 1, freshTarget));
        } else if (reviewed.isEmpty()) {
            freshTarget = size;
        } else if (fresh.isEmpty()) {
            freshTarget = 0;
        }

        Random rng = random == null ? new Random() : random;
        for (int i = 0; i < freshTarget; i++) {
            result.add(removeRandom(fresh, rng));
        }
        while (result.size() < size && !reviewed.isEmpty()) {
            result.add(removeRandom(reviewed, rng));
        }
        while (result.size() < size && !fresh.isEmpty()) {
            result.add(removeRandom(fresh, rng));
        }
        return result;
    }

    private static JapStuJFrame.JaNode removeRandom(
            List<JapStuJFrame.JaNode> values, Random random) {
        return values.remove(random.nextInt(values.size()));
    }
}
