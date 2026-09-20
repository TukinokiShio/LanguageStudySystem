package LanguageStudySystem.JavaJapStuSystem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** 用生产同一套 Java 编解码器回读 staging 文件。 */
public final class LocalStagingReadbackTest {
    private LocalStagingReadbackTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("需要 staging 文件路径");
        List<String> lines = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
        if (lines.size() != 239) throw new AssertionError("staging 行数异常：" + lines.size());
        if (!"238\t0".equals(lines.get(0))) {
            throw new AssertionError("staging header 异常：" + lines.get(0));
        }
        long previousId = 0L;
        for (int i = 1; i < lines.size(); i++) {
            if (LocalDataCodec.columnCount(lines.get(i)) != LocalDataCodec.expectedColumnCount()) {
                throw new AssertionError("第 " + (i + 1) + " 行不是12列");
            }
            JapStuJFrame.JaNode node = LocalDataCodec.parse(lines.get(i), false);
            if (node.localId <= previousId || node.enrichState != 1) {
                throw new AssertionError("第 " + (i + 1) + " 行 localId/enrichState 异常");
            }
            previousId = node.localId;
            if (LocalDataCodec.columnCount(LocalDataCodec.serialize(node))
                    != LocalDataCodec.expectedColumnCount()) {
                throw new AssertionError("第 " + (i + 1) + " 行 Java 回写列数异常");
            }
        }
        System.out.println("LocalStagingReadbackTest: PASS rows=" + (lines.size() - 1));
    }
}
