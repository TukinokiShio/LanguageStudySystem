package LanguageStudySystem.JavaJapStuSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 本地掌握词条归档格式与追加行为回归测试。 */
public final class LocalMasteryArchiveTest {
    private LocalMasteryArchiveTest() {}

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("jss-local-mastery-archive-");
        Path archive = directory.resolve("mastered.txt");
        try {
            JapStuJFrame.JaNode first = node("勉強", 41L);
            LocalMasteryArchive.append(archive, Collections.singletonList(first));

            List<String> rows = Files.readAllLines(archive, StandardCharsets.UTF_8);
            check(rows.size() == 1, "one mastered entry should create one archive row");
            String[] fields = rows.get(0).split("\\t", -1);
            check(fields.length == LocalDataCodec.expectedColumnCount() + 1,
                    "archive row should preserve all local columns and add an archive timestamp");
            check("勉強".equals(fields[0]) && "学习".equals(fields[1]),
                    "Japanese and Chinese fields should be preserved");
            check("2".equals(fields[7]) && "41".equals(fields[11]),
                    "mastery state and local id should be preserved");
            OffsetDateTime.parse(fields[12], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            JapStuJFrame.JaNode second = node("準備", 42L);
            LocalMasteryArchive.append(archive, Arrays.asList(second));
            rows = Files.readAllLines(archive, StandardCharsets.UTF_8);
            check(rows.size() == 2 && rows.get(0).startsWith("勉強\t"),
                    "appending another entry should retain earlier archive rows");

            LocalMasteryArchive.append(archive, Collections.<JapStuJFrame.JaNode>emptyList());
            check(Files.readAllLines(archive, StandardCharsets.UTF_8).size() == 2,
                    "an empty batch should not modify the archive");

            expectIOException(directory, first);
            System.out.println("LocalMasteryArchiveTest: PASS");
        } finally {
            Files.deleteIfExists(archive);
            Files.deleteIfExists(directory);
        }
    }

    private static JapStuJFrame.JaNode node(String japanese, long localId) {
        JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
        node.japanese = japanese;
        node.chinese = "学习";
        node.type = 1;
        node.examTimes = 4;
        node.trueTimes = 3;
        node.example = "例文";
        node.exampleCh = "例句";
        node.masteryState = 2;
        node.example2 = "第二例文";
        node.example2Ch = "第二例句";
        node.enrichState = 1;
        node.localId = localId;
        return node;
    }

    private static void expectIOException(Path directory, JapStuJFrame.JaNode node)
            throws IOException {
        try {
            LocalMasteryArchive.append(directory, Collections.singletonList(node));
            throw new AssertionError("writing to a directory path should fail");
        } catch (IOException expected) {
            // Failure must be visible to the settlement flow so it can keep the entry active.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
