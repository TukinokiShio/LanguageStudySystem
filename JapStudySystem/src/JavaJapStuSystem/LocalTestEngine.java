package LanguageStudySystem.JavaJapStuSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 本地词库临时组的纯规则部分。组成员、组错误次数和组熟练度由窗口层保存，
 * 这里不读取文件，也不依赖 Swing，方便做确定性边界测试。
 */
final class LocalTestEngine {
    static final int BATCH_SIZE = 15;
    static final int HARD_WRONG_LIMIT = 3;
    private static final int RECENT_COUNT = 5;
    private static final int SEGMENT_SAMPLE_COUNT = 5;

    private LocalTestEngine() {}

    static int requiredCorrect(int masteryState, int groupWrong) {
        if (masteryState <= 0) return 1;
        if (masteryState == 1) return 2 + Math.max(0, groupWrong) / 2;
        return 0;
    }

    static int remainingCorrect(int masteryState, int groupCorrect, int groupWrong) {
        int effectiveState = nextMasteryState(masteryState, groupCorrect, groupWrong);
        if (effectiveState >= 2) return 0;
        return Math.max(0, requiredCorrect(effectiveState, groupWrong)
                - Math.max(0, groupCorrect));
    }

    static int nextMasteryState(int masteryState, int groupCorrect, int groupWrong) {
        int state = Math.max(0, Math.min(2, masteryState));
        int correct = Math.max(0, groupCorrect);
        int wrong = Math.max(0, groupWrong);
        if (wrong > HARD_WRONG_LIMIT) return 1;
        if (state >= 2) return 2;
        if (state == 0 && correct >= 1) return 1;
        if (state == 1 && correct >= requiredCorrect(1, wrong)) return 2;
        return state;
    }

    /**
     * 困难词在本组达到正确次数后完成，但状态仍为“了解”；普通了解词只有升级到掌握才完成。
     */
    static boolean isSlotComplete(int masteryState, int groupCorrect, int groupWrong) {
        int state = nextMasteryState(masteryState, groupCorrect, groupWrong);
        if (state >= 2) return true;
        return state == 1 && groupWrong > HARD_WRONG_LIMIT
                && groupCorrect >= requiredCorrect(1, groupWrong);
    }

    static boolean shouldArchive(int masteryState, int groupWrong) {
        return masteryState >= 2 && groupWrong <= HARD_WRONG_LIMIT;
    }

    static boolean isEnriched(JapStuJFrame.JaNode node) {
        return node != null && node.enrichState == 1;
    }

    /**
     * 均匀随机选题：先覆盖未出过题的陌生词，再重抽尚未了解的陌生词，
     * 最后练习了解词。每个阶段内均匀抽取，并尽量避开上一题。
     */
    static int chooseSlot(int[] states, int[] groupWrong, int[] groupCorrect,
                          long[] ids, int size, int lastSlot, Random random) {
        if (states == null || groupWrong == null || groupCorrect == null || ids == null) {
            return -1;
        }
        int limit = Math.min(Math.max(0, size), Math.min(states.length,
                Math.min(groupWrong.length, Math.min(groupCorrect.length, ids.length))));
        if (limit == 0) return -1;

        List<Integer> unaskedUnfamiliar = new ArrayList<>();
        List<Integer> unfamiliar = new ArrayList<>();
        List<Integer> known = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (ids[i] <= 0L) continue;
            int state = nextMasteryState(states[i], groupCorrect[i], groupWrong[i]);
            if (isSlotComplete(state, groupCorrect[i], groupWrong[i])) continue;
            if (state == 0) {
                unfamiliar.add(i);
                if (groupCorrect[i] <= 0 && groupWrong[i] <= 0) {
                    unaskedUnfamiliar.add(i);
                }
            } else if (state == 1) {
                known.add(i);
            }
        }

        List<Integer> candidates = !unaskedUnfamiliar.isEmpty()
                ? unaskedUnfamiliar : !unfamiliar.isEmpty() ? unfamiliar : known;
        if (candidates.isEmpty()) return -1;
        if (candidates.size() >= 2 && candidates.contains(lastSlot)) {
            candidates.remove(Integer.valueOf(lastSlot));
        }
        Random rng = random == null ? new Random() : random;
        return candidates.get(rng.nextInt(candidates.size()));
    }

    /**
     * 取词优先包含最近追加的5条已富化词；其余词按 localId 分为前70%和后30%，
     * 各自在本段内无放回抽取最多5条。不足时不跨段补位。
     */
    static List<JapStuJFrame.JaNode> chooseGroup(List<JapStuJFrame.JaNode> all,
                                                  Random random) {
        List<JapStuJFrame.JaNode> eligible = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        if (all != null) {
            for (JapStuJFrame.JaNode node : all) {
                if (isEnriched(node) && node.localId > 0L && seenIds.add(node.localId)) {
                    eligible.add(node);
                }
            }
        }
        eligible.sort((left, right) -> Long.compare(left.localId, right.localId));
        if (eligible.size() <= BATCH_SIZE) return eligible;

        Random rng = random == null ? new Random() : random;
        int recentStart = eligible.size() - RECENT_COUNT;
        List<JapStuJFrame.JaNode> result = new ArrayList<>(BATCH_SIZE);
        result.addAll(eligible.subList(recentStart, eligible.size()));

        List<JapStuJFrame.JaNode> remaining = eligible.subList(0, recentStart);
        int firstSegmentSize = (int) Math.floor(remaining.size() * 0.70d);
        List<JapStuJFrame.JaNode> firstSegment = remaining.subList(0, firstSegmentSize);
        List<JapStuJFrame.JaNode> secondSegment = remaining.subList(
                firstSegmentSize, remaining.size());
        appendRandomSample(result, firstSegment, SEGMENT_SAMPLE_COUNT, rng);
        appendRandomSample(result, secondSegment, SEGMENT_SAMPLE_COUNT, rng);
        return result;
    }

    private static void appendRandomSample(List<JapStuJFrame.JaNode> result,
                                           List<JapStuJFrame.JaNode> source,
                                           int maximum, Random random) {
        List<JapStuJFrame.JaNode> pool = new ArrayList<>(source);
        int count = Math.min(maximum, pool.size());
        for (int i = 0; i < count; i++) {
            result.add(pool.remove(random.nextInt(pool.size())));
        }
    }
}
