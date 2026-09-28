package LanguageStudySystem.JavaJapStuSystem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Migration checks for persisted local-batch last-question state. */
public final class LocalTestStateCodecTest {
    private LocalTestStateCodecTest() {}

    public static void main(String[] args) throws Exception {
        check(LocalTestStateCodec.supportsVersion("2"), "v2状态文件必须继续读取");
        check(LocalTestStateCodec.supportsVersion("3"), "v3状态文件必须读取");
        check(!LocalTestStateCodec.supportsVersion("1"), "未知旧状态版本不得误读");
        check(LocalTestStateCodec.restoreLastAskedId("2", "19") == 0L,
                "旧v2状态没有上次抽题ID，应从无记录开始");
        check(LocalTestStateCodec.restoreLastAskedId("3", "19") == 19L,
                "v3状态应恢复上次抽中的localId");
        check(LocalTestStateCodec.restoreLastAskedId("3", "bad") == 0L,
                "无效的上次抽题ID应忽略，不影响其他批次状态");
        check(LocalTestStateCodec.formatLastAskedId(19L).equals("lastAskedId=19"),
                "v3状态应写出上次抽题ID");
        testLegacyAndCurrentFiles();
        System.out.println("LocalTestStateCodecTest: PASS");
    }

    private static void testLegacyAndCurrentFiles() throws Exception {
        Path fixture = Files.createTempFile("jss-local-state-migration-", ".txt");
        try {
            Files.write(fixture, java.util.List.of(
                    "version=2", "status=ACTIVE", "batch=7", "slots=19:0:0:0", "hardIds="),
                    StandardCharsets.UTF_8);
            Map<String, String> oldState = LocalTestStateCodec.parse(
                    Files.readAllLines(fixture, StandardCharsets.UTF_8));
            check(LocalTestStateCodec.supportsVersion(oldState.get("version"))
                            && "7".equals(oldState.get("batch"))
                            && "19:0:0:0".equals(oldState.get("slots"))
                            && LocalTestStateCodec.restoreLastAskedId(oldState.get("version"),
                                    oldState.get("lastAskedId")) == 0L,
                    "临时v2状态文件应保留批次与槽位，并将未保存的上次抽题视为空");

            Files.write(fixture, java.util.List.of(
                    "version=3", "status=ACTIVE", "batch=7", "slots=19:0:0:0",
                    "hardIds=", LocalTestStateCodec.formatLastAskedId(19L)),
                    StandardCharsets.UTF_8);
            Map<String, String> currentState = LocalTestStateCodec.parse(
                    Files.readAllLines(fixture, StandardCharsets.UTF_8));
            check(LocalTestStateCodec.restoreLastAskedId(currentState.get("version"),
                            currentState.get("lastAskedId")) == 19L,
                    "临时v3状态文件应跨重启恢复上次抽中的localId");
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
