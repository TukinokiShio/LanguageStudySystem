package LanguageStudySystem.JavaEngStuSystem;

import java.io.*;
import java.util.*;

/**
 * 英语等级分组词库生成器
 * 从 SortedLevel 读取词库，随机打乱后按每组 15 词分组
 * 格式与日文系统 GroupJLPT 兼容：
 *   首行: count\t0
 *   后续行: word\tchinese\ttype\texamTimes\ttrueTimes\texample\texampleCh\tmasteryState
 */
public class EngGroupGenerator {

    static final String SRC_DIR =
        "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaEngStuSystem/LevelWords/SortedLevel/";
    static final String WORK_SRC_DIR = "D:/EngStudy/LevelWords/SortedLevel/";
    static final String GROUP_SRC_DIR =
        "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaEngStuSystem/LevelWords/GroupLevel/";
    static final String GROUP_WORK_DIR = "D:/EngStudy/LevelWords/GroupLevel/";

    static final int GROUP_SIZE = 15;

    public static void main(String[] args) {
        System.out.println("=== 英语等级分组词库生成器 ===");

        ensureDir(GROUP_SRC_DIR);
        ensureDir(GROUP_WORK_DIR);

        String[] levels = {"CET4", "CET6", "IELTS", "TOEFL"};

        for (String level : levels) {
            generateGroups(level);
        }

        System.out.println("\n分组词库生成完毕！");
    }

    static void generateGroups(String levelName) {
        File srcFile = new File(SRC_DIR + levelName + ".txt");
        if (!srcFile.exists()) {
            System.out.println("源文件不存在，跳过: " + srcFile.getAbsolutePath());
            return;
        }

        // 读取所有单词
        List<String> wordLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
            br.readLine(); // 跳过首行 header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) wordLines.add(line);
            }
        } catch (Exception e) {
            System.err.println("读取失败: " + srcFile.getAbsolutePath());
            return;
        }

        int totalWords = wordLines.size();
        // 使用固定种子打乱，保证每次生成一致
        Collections.shuffle(wordLines, new Random(levelName.hashCode()));

        int totalGroups = (int) Math.ceil((double) totalWords / GROUP_SIZE);

        System.out.println("\n" + levelName + ": 共 " + totalWords + " 词, " + totalGroups + " 组");

        for (int g = 1; g <= totalGroups; g++) {
            int start = (g - 1) * GROUP_SIZE;
            int end = Math.min(start + GROUP_SIZE, totalWords);
            int groupCount = end - start;

            // 写入源目录
            writeGroupFile(new File(GROUP_SRC_DIR + levelName + "_Group" + g + ".txt"),
                           wordLines.subList(start, end), groupCount);
            // 写入工作目录
            writeGroupFile(new File(GROUP_WORK_DIR + levelName + "_Group" + g + ".txt"),
                           wordLines.subList(start, end), groupCount);
        }

        System.out.println("  " + levelName + " 分组完成");
    }

    static void writeGroupFile(File file, List<String> lines, int count) {
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // 首行: count\t0
            pw.println(count + "\t0");
            // 写入每行（去掉最后的 levelValue 字段，只留前 8 个字段）
            for (String line : lines) {
                String[] sp = line.split("\t");
                // 输出: word\tchinese\ttype\texamTimes\ttrueTimes\texample\texampleCh\tmasteryState
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8; i++) {
                    if (i > 0) sb.append("\t");
                    if (i < sp.length) sb.append(sp[i]);
                }
                pw.println(sb.toString());
            }
        } catch (Exception e) {
            System.err.println("写入失败: " + file.getAbsolutePath());
        }
    }

    static void ensureDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("创建目录: " + path);
        }
    }
}
