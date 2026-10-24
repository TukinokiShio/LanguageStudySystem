package LanguageStudySystem.JavaJapStuSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 将从本地活动词库结算移除的掌握词条追加到独立 UTF-8 TSV 档案。 */
final class LocalMasteryArchive {
    static final Path FILE_PATH = Paths.get("D:/JaStu_mastered.txt");

    private LocalMasteryArchive() {}

    static void append(List<JapStuJFrame.JaNode> nodes) throws IOException {
        append(FILE_PATH, nodes);
    }

    static void append(Path archivePath, List<JapStuJFrame.JaNode> nodes) throws IOException {
        if (nodes == null || nodes.isEmpty()) return;

        String archivedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        StringBuilder rows = new StringBuilder();
        for (JapStuJFrame.JaNode node : nodes) {
            if (node == null) continue;
            rows.append(LocalDataCodec.serialize(node))
                    .append('\t').append(archivedAt)
                    .append(System.lineSeparator());
        }
        if (rows.length() == 0) return;

        Path parent = archivePath.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(archivePath, rows.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
