package LanguageStudySystem.JavaJapStuSystem.JLPT;

import java.io.*;
import java.util.*;

public class GroupGenerator {

    private static final String SRC_DIR = "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaJapStuSystem/JLPT/SortedJLPT/";
    private static final String OUT_DIR = "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaJapStuSystem/JLPT/GroupJLPT/";
    private static final int GROUP_SIZE = 15;

    static class WordLine {
        String line;
        WordLine(String l) { this.line = l; }
    }

    public static void main(String[] args) throws Exception {
        new File(OUT_DIR).mkdirs();
        String[] levels = {"N5", "N4", "N3", "N2", "N1"};
        Random rnd = new Random();

        for (String lv : levels) {
            File src = new File(SRC_DIR + lv + ".txt");
            if (!src.exists()) {
                System.err.println(lv + " 源文件不存在，跳过");
                continue;
            }

            List<WordLine> words = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(src))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) words.add(new WordLine(line));
                }
            }

            Collections.shuffle(words, rnd);

            int totalGroups = (words.size() + GROUP_SIZE - 1) / GROUP_SIZE;
            System.out.println(lv + ": " + words.size() + " words -> " + totalGroups + " groups");

            for (int g = 1; g <= totalGroups; g++) {
                int start = (g - 1) * GROUP_SIZE;
                int end = Math.min(g * GROUP_SIZE, words.size());
                String fileName = lv + "_Group" + g + ".txt";
                try (PrintWriter pw = new PrintWriter(new FileWriter(new File(OUT_DIR, fileName)))) {
                    pw.println((end - start) + "\t0");
                    for (int i = start; i < end; i++) {
                        pw.println(words.get(i).line);
                    }
                }
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(OUT_DIR, lv + "_progress.txt")))) {
                pw.println("1");  // 当前分组索引（从1开始）
                pw.println("0");  // 当前分组是否解锁（1=已解锁，0=未解锁，初始为0将在首次进入时自动设定）
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(OUT_DIR, lv + "_carryover.txt")))) {
                pw.println("0");
            }

            System.out.println(lv + " 完成：共 " + totalGroups + " 组，每组 " + GROUP_SIZE + " 词");
        }
        System.out.println("所有 JLPT 分组数据生成完毕！");
    }
}
