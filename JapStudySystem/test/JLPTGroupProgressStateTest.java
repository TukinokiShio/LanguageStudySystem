package LanguageStudySystem.JavaJapStuSystem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Temporary-file checks for legacy and persisted JLPT last-question state. */
public final class JLPTGroupProgressStateTest {
    private static final String ID = "00000000-0000-0000-0000-000000000009";

    private JLPTGroupProgressStateTest() {}

    public static void main(String[] args) throws Exception {
        testLegacyProgressFile();
        testLastAskedRoundTrip();
        System.out.println("JLPTGroupProgressStateTest: PASS");
    }

    private static void testLegacyProgressFile() throws Exception {
        Path fixture = Files.createTempFile("jss-jlpt-progress-legacy-", ".txt");
        try {
            Files.write(fixture, List.of("4", "0"), StandardCharsets.UTF_8);
            JLPTGroupProgressState.Snapshot state = JLPTGroupProgressState.parse(
                    Files.readAllLines(fixture, StandardCharsets.UTF_8));
            check(state.groupIndex == 4 && state.lastAskedId.isEmpty(),
                    "旧版组索引与解锁行应读取成功，并默认无上次抽题ID");
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static void testLastAskedRoundTrip() throws Exception {
        Path fixture = Files.createTempFile("jss-jlpt-progress-v3-", ".txt");
        try {
            Files.write(fixture, JLPTGroupProgressState.format(2, ID), StandardCharsets.UTF_8);
            JLPTGroupProgressState.Snapshot state = JLPTGroupProgressState.parse(
                    Files.readAllLines(fixture, StandardCharsets.UTF_8));
            check(state.groupIndex == 2 && ID.equals(state.lastAskedId),
                    "当前组与上次抽题ID应跨重启保留");
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
