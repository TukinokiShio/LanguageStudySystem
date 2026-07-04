package LanguageStudySystem.JavaEngStuSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EngStuJFrame extends JFrame implements ActionListener, EngImageRecognition.ImageRecognitionCallback {

    // ==================== 核心数据 ====================
    private static final String FILE_PATH = "D:/EngStu.txt";

    // 等级词库路径
    private static final String LEVEL_SRC_DIR =
        "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaEngStuSystem/LevelWords/SortedLevel/";
    private static final String LEVEL_DIR = "D:/EngStudy/LevelWords/SortedLevel/";
    private static final String GROUP_LEVEL_SRC_DIR =
        "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaEngStuSystem/LevelWords/GroupLevel/";
    private static final String GROUP_LEVEL_DIR = "D:/EngStudy/LevelWords/GroupLevel/";
    private static final int GROUP_SIZE = 15;

    static class EngNode {
        String word;
        String chinese;
        int examTimes;
        int trueTimes;
        int masteryState;  // 0=陌生, 1=了解, 2=掌握 (等级词库专用)
        int engLevel;      // 0=本地, 1=CET4, 2=CET6, 3=IELTS, 4=TOEFL
        int wrongTimes;    // 本次会话连续答错次数
        EngNode next;
    }

    private EngNode globalList;
    private int state = 0;
    private String tmpWord = "", tmpCn = "";
    private int tmpStep = 0;               // 不再使用，保留以免影响其他代码
    private EngNode currentTest = null;
    private EngNode editTarget = null;
    private int editField = 0;
    private int[] testedIndex = new int[1000];
    private int testedCount = 0;

    // 等级模式
    private boolean levelMode = false;
    private EngNode levelList;
    private String currentEngLevel = "CET4";
    private String lastEngLevel = "CET4";

    // 进度条组件
    private JPanel levelProgressPanel;
    private JProgressBar levelProgressBar;      // 词书整体进度
    private JProgressBar levelGroupProgressBar;  // 当前分组进度
    private JLabel levelProgressLabel;
    private JLabel levelGroupLabel;

    // 测试状态面板
    private JPanel levelTestStatePanel;
    private JLabel levelTestStateLabel;
    private JLabel levelTestWordLabel;

    // 统计标签
    private JLabel statsLabel;
    private JPanel statsPanel;

    // 分组学习
    private int currentGroupIndex = 1;
    private int totalGroups = 0;
    private boolean groupMastered = false;
    private int allWrongOver3ThisSession = 0;

    // 最近测试的词汇（用于加权随机）
    private List<EngNode> recentTestedWords = new ArrayList<>();
    private static final int RECENT_WINDOW = 10;

    // 等级按钮
    private JButton btnLevelStudy;
    private JButton btnLevelSwitch;

    // 编辑行面板
    private JPanel editBtnRow;

    // 状态常量
    private static final int STATE_IMAGE_RECOGNITION = 10;
    private static final int STATE_ADD_EN_CONFIRM = 11;   // 等待第二次回车确认英文
    private static final int STATE_ADD_CN_INPUT   = 12;   // 等待输入中文释义（第一次回车前）
    private static final int STATE_ADD_CN_CONFIRM = 13;   // 等待第二次回车确认释义

    // ==================== 界面组件 ====================
    private JButton btnShowAll;
    private JButton btnAddWord;
    private JButton btnEditWord;
    private JButton btnRandomTest;
    private JButton btnExit;
    private JButton btnImageReco;

    private JButton btnShowAnswer;
    private JButton btnYes;
    private JButton btnNo;
    private JButton btnContinueTest;
    private JButton btnMarkMastered;

    private JButton btnEditWordField;
    private JButton btnEditCnField;
    private JButton btnFinishEdit;
    private JButton btnDeleteWord;

    private JTextField inputField;
    private JButton btnInputConfirm;
    private JTextArea showArea;
    private JPanel cardPanel;
    private JScrollPane cardScrollPane;
    private JPanel topBtnPanel;
    private JPanel secondBtnPanel;
    private JPanel inputPanel;
    private JPanel northPanel;
    private JPanel centerWrapPanel;

    // ==================== 构造方法 ====================
    public EngStuJFrame() {
        initJFrame();
        initNorthPanel();
        initCenterView();
        globalList = initList();
        levelList = initList();
        readFromFile();
        ensureLevelWorkDir();
        ensureGroupLevelWorkDir();
        updateStatsLabel();
        setStatsLabelVisible(false);
        loadLevelPreference();
        showStartupStatus();
        setSize(900, 650);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
    }

    private void initJFrame() {
        setTitle("英语单词系统 V2.1.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
    }

    private void initNorthPanel() {
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        initTopButtonPanel();
        initSecondButtonPanel();
        initInputPanel();

        // 统计标签
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 20));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statsLabel.setForeground(new Color(60, 120, 180));
        statsPanel.add(statsLabel);

        // 等级进度面板
        levelProgressPanel = new JPanel();
        levelProgressPanel.setLayout(new BoxLayout(levelProgressPanel, BoxLayout.Y_AXIS));
        levelProgressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelProgressPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 20));
        levelProgressPanel.setVisible(false);

        JPanel groupBarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        levelGroupProgressBar = new JProgressBar(0, 100);
        levelGroupProgressBar.setPreferredSize(new Dimension(300, 18));
        levelGroupProgressBar.setStringPainted(true);
        levelGroupProgressBar.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        levelGroupLabel = new JLabel();
        levelGroupLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        levelGroupLabel.setForeground(new Color(80, 80, 80));
        groupBarRow.add(new JLabel("词书 "));
        groupBarRow.add(levelGroupProgressBar);
        groupBarRow.add(Box.createHorizontalStrut(10));
        groupBarRow.add(levelGroupLabel);

        JPanel bookBarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        levelProgressBar = new JProgressBar(0, 100);
        levelProgressBar.setPreferredSize(new Dimension(300, 18));
        levelProgressBar.setStringPainted(true);
        levelProgressBar.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        levelProgressLabel = new JLabel();
        levelProgressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        levelProgressLabel.setForeground(new Color(80, 80, 80));
        levelProgressLabel.setPreferredSize(new Dimension(500, 20));
        bookBarRow.add(new JLabel("分组 "));
        bookBarRow.add(levelProgressBar);
        bookBarRow.add(Box.createHorizontalStrut(10));
        bookBarRow.add(levelProgressLabel);

        levelProgressPanel.add(groupBarRow);
        levelProgressPanel.add(bookBarRow);

        // 测试状态面板
        levelTestStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        levelTestStatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelTestStatePanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 20));
        levelTestStateLabel = new JLabel();
        levelTestStateLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        levelTestStateLabel.setForeground(new Color(0, 120, 60));
        levelTestWordLabel = new JLabel();
        levelTestWordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        levelTestWordLabel.setForeground(new Color(80, 80, 80));
        levelTestStatePanel.add(levelTestStateLabel);
        levelTestStatePanel.add(levelTestWordLabel);
        levelTestStatePanel.setVisible(false);

        northPanel.add(topBtnPanel);
        northPanel.add(secondBtnPanel);
        northPanel.add(inputPanel);
        northPanel.add(statsPanel);
        northPanel.add(levelProgressPanel);
        northPanel.add(levelTestStatePanel);
        add(northPanel, BorderLayout.NORTH);
    }

    private void initTopButtonPanel() {
        topBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnShowAll = new JButton("查看全部");
        btnAddWord = new JButton("添加单词");
        btnEditWord = new JButton("查找单词");
        btnRandomTest = new JButton("随机测试");
        btnExit = new JButton("保存退出");
        btnImageReco = new JButton("图片识别");
        btnLevelStudy = new JButton("等级词书");
        btnLevelSwitch = new JButton("CET4");

        // 设置顶部按钮字体（调小为13号）
        Font topBtnFont = new Font("微软雅黑", Font.BOLD, 13);
        btnShowAll.setFont(topBtnFont);
        btnAddWord.setFont(topBtnFont);
        btnEditWord.setFont(topBtnFont);
        btnRandomTest.setFont(topBtnFont);
        btnExit.setFont(topBtnFont);
        btnImageReco.setFont(topBtnFont);
        btnLevelStudy.setFont(topBtnFont);
        btnLevelSwitch.setFont(topBtnFont);

        topBtnPanel.add(btnShowAll);
        topBtnPanel.add(btnAddWord);
        topBtnPanel.add(btnEditWord);
        topBtnPanel.add(btnRandomTest);
        topBtnPanel.add(btnLevelStudy);
        topBtnPanel.add(btnLevelSwitch);
        topBtnPanel.add(btnImageReco);
        topBtnPanel.add(btnExit);

        btnShowAll.addActionListener(this);
        btnAddWord.addActionListener(this);
        btnEditWord.addActionListener(this);
        btnRandomTest.addActionListener(this);
        btnExit.addActionListener(this);
        btnImageReco.addActionListener(this);
        btnLevelStudy.addActionListener(this);
        btnLevelSwitch.addActionListener(this);

        // 等级切换按钮初始隐藏
        btnLevelSwitch.setVisible(false);
    }

    private void initSecondButtonPanel() {
        secondBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        secondBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnShowAnswer = new JButton("显示答案");
        btnYes = new JButton("记得");
        btnNo = new JButton("不记得");
        btnEditWordField = new JButton("修改单词");
        btnEditCnField = new JButton("修改释义");
        btnFinishEdit = new JButton("完成修改");
        btnDeleteWord = new JButton("删除单词");
        btnContinueTest = new JButton("继续测试");
        btnMarkMastered = new JButton("掌握单词");

        Color btnBlue = new Color(235, 245, 255);
        btnShowAnswer.setBackground(btnBlue);
        btnYes.setBackground(btnBlue);
        btnNo.setBackground(btnBlue);
        btnEditWordField.setBackground(btnBlue);
        btnEditCnField.setBackground(btnBlue);
        btnFinishEdit.setBackground(btnBlue);
        btnDeleteWord.setBackground(new Color(255, 235, 235));
        btnContinueTest.setBackground(new Color(235, 255, 235));
        btnMarkMastered.setBackground(new Color(255, 245, 220));

        // 设置第二行按钮字体（调小为12号）
        Font secBtnFont = new Font("微软雅黑", Font.PLAIN, 12);
        btnShowAnswer.setFont(secBtnFont);
        btnYes.setFont(secBtnFont);
        btnNo.setFont(secBtnFont);
        btnEditWordField.setFont(secBtnFont);
        btnEditCnField.setFont(secBtnFont);
        btnFinishEdit.setFont(secBtnFont);
        btnDeleteWord.setFont(secBtnFont);
        btnContinueTest.setFont(secBtnFont);
        btnMarkMastered.setFont(secBtnFont);

        secondBtnPanel.add(btnShowAnswer);
        secondBtnPanel.add(btnYes);
        secondBtnPanel.add(btnNo);
        secondBtnPanel.add(btnEditWordField);
        secondBtnPanel.add(btnEditCnField);
        secondBtnPanel.add(btnFinishEdit);
        secondBtnPanel.add(btnDeleteWord);
        secondBtnPanel.add(btnContinueTest);
        secondBtnPanel.add(btnMarkMastered);

        btnShowAnswer.addActionListener(this);
        btnYes.addActionListener(this);
        btnNo.addActionListener(this);
        btnEditWordField.addActionListener(this);
        btnEditCnField.addActionListener(this);
        btnFinishEdit.addActionListener(this);
        btnDeleteWord.addActionListener(this);
        btnContinueTest.addActionListener(this);
        btnMarkMastered.addActionListener(this);

        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        btnContinueTest.setVisible(false);
        btnMarkMastered.setVisible(false);
    }

    private void initInputPanel() {
        inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 20));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        inputField.addActionListener(e -> doInputConfirm(false));

        btnInputConfirm = new JButton("输入完成");
        btnInputConfirm.setBackground(new Color(235, 245, 255));
        btnInputConfirm.setFont(new Font("微软雅黑", Font.BOLD, 15)); // 调小为15号
        btnInputConfirm.addActionListener(e -> doInputConfirm(true));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(btnInputConfirm, BorderLayout.EAST);

        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
    }

    private void initCenterView() {
        centerWrapPanel = new JPanel(new BorderLayout());
        centerWrapPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 20));

        showArea = new JTextArea();
        showArea.setFont(new Font("微软雅黑", Font.PLAIN, 20));   // 输出文本字号加大
        showArea.setEditable(false);
        showArea.setLineWrap(true);

        cardPanel = new JPanel();
        cardPanel.setLayout(new WrapLayout(FlowLayout.LEADING, 15, 15));
        cardScrollPane = new JScrollPane(cardPanel);
        cardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cardScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerWrapPanel.add(new JScrollPane(showArea), BorderLayout.CENTER);
        add(centerWrapPanel, BorderLayout.CENTER);
    }

    @Override
    public void onCancel() {
        showAllWordsAsCards();
    }

    @Override
    public void onAddWords(List<EngNode> words) {
        if (words == null || words.isEmpty()) return;
        EngNode tail = globalList;
        while (tail.next != null) tail = tail.next;
        for (EngNode w : words) {
            EngNode node = new EngNode();
            node.word = w.word;
            node.chinese = w.chinese;
            node.examTimes = 0;
            node.trueTimes = 0;
            tail.next = node;
            tail = node;
        }
        saveToFile();
        JOptionPane.showMessageDialog(this, "成功添加 " + words.size() + " 个单词！");
        showAllWordsAsCards();
    }

    private void showAllWordsAsCards() {
        clearAll();
        levelMode = false;
        setLevelProgressVisible(false);
        setStatsLabelVisible(false);
        levelTestStatePanel.setVisible(false);
        state = 0;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        btnContinueTest.setVisible(false);
        btnMarkMastered.setVisible(false);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
        editTarget = null;

        centerWrapPanel.removeAll();
        centerWrapPanel.add(cardScrollPane, BorderLayout.CENTER);
        cardPanel.removeAll();

        EngNode p = globalList.next;
        if (p == null) {
            showArea.setText("暂无单词数据");
            centerWrapPanel.removeAll();
            centerWrapPanel.add(new JScrollPane(showArea), BorderLayout.CENTER);
            centerWrapPanel.revalidate();
            centerWrapPanel.repaint();
            return;
        }

        while (p != null) {
            final EngNode node = p;
            cardPanel.add(createWordCard(node));
            p = p.next;
        }
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    private JPanel createWordCard(EngNode node) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(390, 120));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel wordLabel = new JLabel(node.word);
        wordLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        JLabel cnLabel = new JLabel("释义：" + node.chinese);
        cnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        String rate = node.examTimes == 0 ? "0%" : (node.trueTimes * 100 / node.examTimes) + "%";
        JLabel statLabel = new JLabel("考核：" + node.examTimes + "  正确：" + node.trueTimes + "  正确率：" + rate);
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statLabel.setForeground(new Color(60, 120, 180));

        infoPanel.add(wordLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(cnLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(statLabel);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 5, 10, 10));

        JButton editBtn = new JButton("修改");
        JButton delBtn = new JButton("删除");
        editBtn.setBackground(new Color(240, 245, 255));
        delBtn.setBackground(new Color(255, 240, 240));
        editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13)); // 调小为13号
        delBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13)); // 调小为13号

        editBtn.addActionListener(e -> {
            editTarget = node;
            clearAll();
            switchToTextArea();
            print("===== 修改单词 =====");
            print("当前单词：" + editTarget.word);
            print("当前释义：" + editTarget.chinese);
            state = 7;
            setEditButtonsVisible(true);
            btnDeleteWord.setVisible(true);
            inputField.setVisible(false);
            btnInputConfirm.setVisible(false);
        });
        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定删除【" + node.word + "】？", "删除确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteNode(node);
                showAllWordsAsCards();
            }
        });

        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    private void deleteNode(EngNode target) {
        EngNode pre = globalList;
        while (pre.next != null && pre.next != target) pre = pre.next;
        if (pre.next == target) {
            pre.next = target.next;
            saveToFile();
        }
    }

    private void removeFromLevelList(EngNode target) {
        EngNode pre = levelList;
        while (pre.next != null && pre.next != target) pre = pre.next;
        if (pre.next == target) {
            pre.next = target.next;
        }
    }

    private void clearAll() {
        showArea.setText("");
        inputField.setText("");
        cardPanel.removeAll();
    }

    private void print(String s) { showArea.append(s + "\n"); }

    private void setTestButtonsVisible(boolean showAnswer, boolean showYesNo) {
        btnShowAnswer.setVisible(showAnswer);
        btnYes.setVisible(showYesNo);
        btnNo.setVisible(showYesNo);
    }

    private void setEditButtonsVisible(boolean visible) {
        btnEditWordField.setVisible(visible);
        btnEditCnField.setVisible(visible);
        btnFinishEdit.setVisible(visible);
    }

    private EngNode initList() {
        EngNode head = new EngNode();
        head.next = null;
        return head;
    }

    private int listLen() {
        int c = 0;
        EngNode p = globalList.next;
        while (p != null) { c++; p = p.next; }
        return c;
    }

    private int listLen(EngNode list) {
        int c = 0;
        EngNode p = list.next;
        while (p != null) { c++; p = p.next; }
        return c;
    }

    private boolean isTested(int idx) {
        for (int i = 0; i < testedCount; i++)
            if (testedIndex[i] == idx) return true;
        return false;
    }

    private void readFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            EngNode tail = globalList;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] sp = line.split("\t");
                EngNode node = new EngNode();
                node.word = sp[0];
                node.chinese = sp.length > 1 ? sp[1] : "";
                node.examTimes = sp.length > 2 ? Integer.parseInt(sp[2]) : 0;
                node.trueTimes = sp.length > 3 ? Integer.parseInt(sp[3]) : 0;
                tail.next = node;
                tail = node;
            }
            print("数据读取成功！");
        } catch (Exception e) {
            print("文件打开失败！使用空链表");
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            EngNode p = globalList.next;
            while (p != null) {
                pw.println(p.word + "\t" + p.chinese + "\t" + p.examTimes + "\t" + p.trueTimes);
                p = p.next;
            }
        } catch (Exception ignored) {}
    }

    private void addWordMode() {
        levelMode = false;
        setLevelProgressVisible(false);
        setStatsLabelVisible(false);
        levelTestStatePanel.setVisible(false);
        clearAll();
        switchToTextArea();
        state = 1;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        inputField.setVisible(true);
        btnInputConfirm.setVisible(true);
        print("===== 添加单词 =====");
        print("请输入英文单词后点击“输入完成”或连续按两次回车键：");
    }

    private void editWordMode() {
        levelMode = false;
        setLevelProgressVisible(false);
        setStatsLabelVisible(false);
        levelTestStatePanel.setVisible(false);
        clearAll();
        switchToTextArea();
        state = 4;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        inputField.setVisible(true);
        btnInputConfirm.setVisible(true);
        if (globalList.next == null) {
            print("暂无数据");
            state = 0;
            inputField.setVisible(false);
            btnInputConfirm.setVisible(false);
            return;
        }
        print("===== 查找单词 =====");
        print("输入单词/中文进行搜索：");
    }

    private void randomTest() {
        // 退出等级模式
        levelMode = false;
        setLevelProgressVisible(false);
        setStatsLabelVisible(false);
        levelTestStatePanel.setVisible(false);

        // 恢复"显示答案"按钮的文本与颜色
        btnShowAnswer.setText("显示答案");
        btnShowAnswer.setBackground(new Color(235, 245, 255));

        clearAll();
        switchToTextArea();
        state = 2;
        setTestButtonsVisible(true, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
        editTarget = null;
        int len = listLen();
        if (len == 0) { print("无数据"); state = 0; return; }
        if (testedCount >= len) {
            testedCount = 0;
            testedIndex = new int[1000];
        }
        Random r = new Random();
        int idx;
        do idx = r.nextInt(len) + 1; while (isTested(idx));
        testedIndex[testedCount++] = idx;
        EngNode p = globalList;
        for (int i = 0; i < idx; i++) p = p.next;
        currentTest = p;
        print("===== 单词测试 =====");
        print(p.word);
        print("\n点击上方按钮：【显示答案】");
    }

    private void switchToTextArea() {
        centerWrapPanel.removeAll();
        JScrollPane sp = new JScrollPane(showArea);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        centerWrapPanel.add(sp, BorderLayout.CENTER);
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    private void enterImageRecognitionMode() {
        levelMode = false;
        setLevelProgressVisible(false);
        setStatsLabelVisible(false);
        levelTestStatePanel.setVisible(false);
        clearAll();
        state = STATE_IMAGE_RECOGNITION;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);

        centerWrapPanel.removeAll();
        centerWrapPanel.add(new EngImageRecognition(this), BorderLayout.CENTER);
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    // ==================== 统一输入确认逻辑（含双回车机制） ====================
    private void doInputConfirm(boolean fromButton) {
        String input = inputField.getText().trim();
        inputField.setText("");

        // ========== 添加单词流程（双回车确认，按钮直接确认） ==========
        if (state == 1) {                     // 英文第一次输入
            if (input.isEmpty()) return;
            if (fromButton) {
                tmpWord = input;
                print("英文单词已确认: " + tmpWord);
                state = STATE_ADD_CN_INPUT;
                print("请输入中文释义后按回车两次或点击“输入完成”：");
            } else {
                tmpWord = input;
                state = STATE_ADD_EN_CONFIRM;
                print("英文单词: " + tmpWord);
                print("再次按回车键确认英文单词（如需修改，可直接在输入框中修改后按回车）。");
            }
            return;
        }
        if (state == STATE_ADD_EN_CONFIRM) {  // 英文二次确认
            if (!input.isEmpty()) tmpWord = input;
            print("英文单词已确认: " + tmpWord);
            state = STATE_ADD_CN_INPUT;
            print("请输入中文释义后按回车两次或点击“输入完成”：");
            return;
        }
        if (state == STATE_ADD_CN_INPUT) {    // 中文第一次输入
            if (input.isEmpty()) {
                print("释义不能为空，请重新输入中文释义：");
                return;
            }
            if (fromButton) {
                tmpCn = input;
                print("中文释义已确认: " + tmpCn);
                createAndSaveWord();
            } else {
                tmpCn = input;
                state = STATE_ADD_CN_CONFIRM;
                print("中文释义: " + tmpCn);
                print("再次按回车键确认释义（如需修改，可直接在输入框中修改后按回车）。");
            }
            return;
        }
        if (state == STATE_ADD_CN_CONFIRM) {  // 中文二次确认
            if (!input.isEmpty()) tmpCn = input;
            print("中文释义已确认: " + tmpCn);
            createAndSaveWord();
            return;
        }

        // ========== 其他状态（查找、编辑等） ==========
        if (state == 4) {
            clearAll();
            EngNode p = globalList.next;
            int found = 0;
            editTarget = null;
            print("===== 查找结果 =====");
            while (p != null) {
                if (p.word.contains(input) || p.chinese.contains(input)) {
                    found++;
                    editTarget = p;
                    print(found + ". " + p.word + " | " + p.chinese);
                }
                p = p.next;
            }
            if (found == 0) {
                print("未找到匹配的单词。\n请重新输入关键词：");
            } else if (found == 1) {
                print("\n已选中该单词。");
                state = 7;
                setEditButtonsVisible(true);
                btnDeleteWord.setVisible(true);
                inputField.setVisible(false);
                btnInputConfirm.setVisible(false);
            } else {
                print("\n找到 " + found + " 个结果，请输入更精确的关键词：");
            }
        } else if (state == 8) {
            if (editTarget != null) {
                if (editField == 1) editTarget.word = input;
                else editTarget.chinese = input;
                print("修改成功！");
                state = 7;
                inputField.setVisible(false);
                btnInputConfirm.setVisible(false);
            }
        }
    }

    private void createAndSaveWord() {
        EngNode node = new EngNode();
        node.word = tmpWord;
        node.chinese = tmpCn;
        node.examTimes = 0;
        node.trueTimes = 0;
        node.next = globalList.next;
        globalList.next = node;
        saveToFile();
        print("添加成功！");
        print("单词:  " + node.word);
        print("释义:  " + node.chinese);
        print("您可以继续操作其他功能。");
        state = 0;
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
    }

    // ==================== 统计与启动状态 ====================

    private void updateStatsLabel() {
        if (levelMode && levelList != null) {
            int total = listLen(levelList);
            int mastered = 0, known = 0, strange = 0;
            EngNode p = levelList.next;
            while (p != null) {
                switch (p.masteryState) {
                    case 2: mastered++; break;
                    case 1: known++; break;
                    default: strange++; break;
                }
                p = p.next;
            }
            statsLabel.setText("当前词书: " + currentEngLevel + " | 共 " + total
                    + " 词 | 掌握 " + mastered + "  了解 " + known + "  陌生 " + strange);
        } else {
            int total = listLen(globalList);
            int mastered = 0;
            EngNode p = globalList.next;
            while (p != null) {
                int wrong = p.examTimes - p.trueTimes;
                if (p.trueTimes >= 5 + wrong / 2) mastered++;
                p = p.next;
            }
            statsLabel.setText("当前在库单词: " + total + "  已掌握: " + mastered);
        }
    }

    private void setStatsLabelVisible(boolean visible) {
        statsPanel.setVisible(visible);
        if (visible) updateStatsLabel();
    }

    private void showStartupStatus() {
        switchToTextArea();
        print("===== 系统启动状态 =====");
        File localFile = new File(FILE_PATH);
        if (localFile.exists()) {
            int total = listLen(globalList);
            int mastered = 0;
            EngNode p = globalList.next;
            while (p != null) {
                int wrong = p.examTimes - p.trueTimes;
                if (p.trueTimes >= 5 + wrong / 2) mastered++;
                p = p.next;
            }
            print("本地词库 D:\\EngStu.txt: 存在 | 共 " + total + " 项 | 已掌握 " + mastered);
            if (total == 0) print("  提示：本地词库为空，请添加单词。");
        } else {
            print("本地词库 D:\\EngStu.txt: 未找到（将在保存时创建）");
        }
        print("");
        print("----- 等级词书状态（D:\\EngStudy\\LevelWords\\SortedLevel） -----");
        File levelWorkDir = new File(LEVEL_DIR);
        if (!levelWorkDir.exists()) {
            print("等级词书工作目录不存在，尝试初始化...");
            ensureLevelWorkDir();
        }
        if (levelWorkDir.exists()) {
            int totalWords = 0;
            String[] levels = {"CET4", "CET6", "IELTS", "TOEFL"};
            for (String lv : levels) {
                File lf = new File(LEVEL_DIR + lv + ".txt");
                if (lf.exists()) {
                    int wordCount = 0, masteredCount = 0, knownCount = 0, strangeCount = 0;
                    try (BufferedReader br = new BufferedReader(new FileReader(lf))) {
                        br.readLine(); // header
                        String line;
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            wordCount++;
                            String[] sp = line.split("\t");
                            if (sp.length > 7) {
                                int ms = Integer.parseInt(sp[7].trim());
                                switch (ms) {
                                    case 2: masteredCount++; break;
                                    case 1: knownCount++; break;
                                    default: strangeCount++; break;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    totalWords += wordCount;
                    if (masteredCount + knownCount + strangeCount > 0) {
                        print(lv + ": 共 " + wordCount + " 词 | 掌握 " + masteredCount
                                + "  了解 " + knownCount + "  陌生 " + strangeCount);
                    } else {
                        print(lv + ": 共 " + wordCount + " 词（尚未开始学习）");
                    }
                } else {
                    print(lv + ": 未找到");
                }
            }
            print("等级词书总计: 共 " + totalWords + " 词 | 上次使用等级: " + lastEngLevel);
            loadGroupProgress();
            print("分组状态: " + currentEngLevel + " 共 " + totalGroups + " 组，当前第 " + currentGroupIndex + " 组");
        } else {
            print("等级词书目录初始化失败！");
        }
        print("");
        print("===== 系统就绪 =====");
        print("点击「等级词书」按钮开始等级学习，或使用本地词库功能。");
    }

    // ==================== 等级词库文件管理 ====================

    private void ensureLevelWorkDir() {
        File workDir = new File(LEVEL_DIR);
        if (workDir.exists()) return;
        workDir.mkdirs();
        File srcDir = new File(LEVEL_SRC_DIR);
        if (!srcDir.exists()) {
            System.err.println("等级词库源目录不存在: " + LEVEL_SRC_DIR);
            return;
        }
        File[] files = srcDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return;
        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f));
                 PrintWriter pw = new PrintWriter(new FileWriter(new File(workDir, f.getName())))) {
                String line;
                while ((line = br.readLine()) != null) pw.println(line);
            } catch (Exception e) {
                System.err.println("复制等级词库文件失败: " + f.getName());
            }
        }
        System.out.println("等级词库已初始化到 " + LEVEL_DIR);
    }

    private void ensureGroupLevelWorkDir() {
        File workDir = new File(GROUP_LEVEL_DIR);
        if (workDir.exists() && new File(GROUP_LEVEL_DIR + "CET4_Group1.txt").exists()) return;
        workDir.mkdirs();
        File srcDir = new File(GROUP_LEVEL_SRC_DIR);
        if (!srcDir.exists()) {
            System.err.println("分组源目录不存在: " + GROUP_LEVEL_SRC_DIR);
            return;
        }
        File[] files = srcDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return;
        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f));
                 PrintWriter pw = new PrintWriter(new FileWriter(new File(workDir, f.getName())))) {
                String line;
                while ((line = br.readLine()) != null) pw.println(line);
            } catch (Exception e) {
                System.err.println("复制分组文件失败: " + f.getName());
            }
        }
        System.out.println("分组词库已初始化到 " + GROUP_LEVEL_DIR);
    }

    private EngNode parseLevelNode(String line) {
        String[] sp = line.split("\t");
        EngNode node = new EngNode();
        node.word = sp[0];
        node.chinese = sp.length > 1 ? sp[1] : "";
        node.examTimes = sp.length > 3 ? Integer.parseInt(sp[3].trim()) : 0;
        node.trueTimes = sp.length > 4 ? Integer.parseInt(sp[4].trim()) : 0;
        node.masteryState = sp.length > 7 ? Integer.parseInt(sp[7].trim()) : 0;
        return node;
    }

    // ==================== 分组管理 ====================

    private void loadGroupProgress() {
        File progFile = new File(GROUP_LEVEL_DIR + currentEngLevel + "_progress.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(progFile))) {
            String line = br.readLine();
            if (line != null) currentGroupIndex = Integer.parseInt(line.trim());
            if (currentGroupIndex < 1) currentGroupIndex = 1;
        } catch (Exception e) {
            currentGroupIndex = 1;
        }
        countTotalGroups();
    }

    private void saveGroupProgress() {
        File outDir = new File(GROUP_LEVEL_DIR);
        if (!outDir.exists()) outDir.mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(
                GROUP_LEVEL_DIR + currentEngLevel + "_progress.txt"))) {
            pw.println(currentGroupIndex);
        } catch (Exception ignored) {}
    }

    private void countTotalGroups() {
        totalGroups = 0;
        while (new File(GROUP_LEVEL_DIR + currentEngLevel + "_Group" + (totalGroups + 1) + ".txt").exists()) {
            totalGroups++;
        }
        if (totalGroups == 0) totalGroups = 1;
    }

    private void loadCurrentGroup() {
        levelList = initList();
        String groupFile = GROUP_LEVEL_DIR + currentEngLevel + "_Group" + currentGroupIndex + ".txt";
        EngNode tail = levelList;
        try (BufferedReader br = new BufferedReader(new FileReader(groupFile))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                EngNode node = parseLevelNode(line);
                node.engLevel = getLevelValue(currentEngLevel);
                tail.next = node;
                tail = node;
            }
        } catch (Exception e) {
            print("读取分组文件失败: " + e.getMessage());
        }
        loadCarryOver(tail);
        if (listLen(levelList) > 0 && countNonMastered() == 0) {
            groupMastered = true;
        } else {
            groupMastered = false;
        }
        updateLevelProgress();
    }

    private void loadCarryOver(EngNode tail) {
        File coFile = new File(GROUP_LEVEL_DIR + currentEngLevel + "_carryover.txt");
        if (!coFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(coFile))) {
            br.readLine(); // count line
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                EngNode node = parseLevelNode(line);
                node.engLevel = getLevelValue(currentEngLevel);
                tail.next = node;
                tail = node;
            }
        } catch (Exception ignored) {}
    }

    private void saveCarryOver() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(
                GROUP_LEVEL_DIR + currentEngLevel + "_carryover.txt"))) {
            pw.println(allWrongOver3ThisSession);
        } catch (Exception ignored) {}
    }

    private void saveCurrentGroup() {
        String groupFile = GROUP_LEVEL_DIR + currentEngLevel + "_Group" + currentGroupIndex + ".txt";
        int len = listLen(levelList);
        try (PrintWriter pw = new PrintWriter(new FileWriter(groupFile))) {
            pw.println(len + "\t0");
            EngNode p = levelList.next;
            while (p != null) {
                pw.println(p.word + "\t" + p.chinese + "\t1\t" + p.examTimes + "\t" + p.trueTimes
                        + "\t\t\t" + p.masteryState);
                p = p.next;
            }
        } catch (Exception ignored) {}
    }

    private void appendToNextGroup(EngNode node) {
        String coFile = GROUP_LEVEL_DIR + currentEngLevel + "_carryover.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(coFile, true))) {
            pw.println(node.word + "\t" + node.chinese + "\t1\t0\t0\t\t\t0");
        } catch (Exception ignored) {}
        allWrongOver3ThisSession++;
        saveCarryOver();
    }

    // ==================== 等级偏好 ====================

    private int getLevelValue(String levelName) {
        switch (levelName) {
            case "CET4":  return 1;
            case "CET6":  return 2;
            case "IELTS": return 3;
            case "TOEFL": return 4;
            default:      return 0;
        }
    }

    private String getLevelName(int value) {
        switch (value) {
            case 1: return "CET4";
            case 2: return "CET6";
            case 3: return "IELTS";
            case 4: return "TOEFL";
            default: return "CET4";
        }
    }

    private void loadLevelPreference() {
        String prefFile = LEVEL_DIR + "last_level.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(prefFile))) {
            String level = br.readLine();
            if (level != null && level.matches("CET[46]|IELTS|TOEFL")) {
                currentEngLevel = level.trim();
                lastEngLevel = level.trim();
            }
        } catch (Exception ignored) {}
    }

    private void saveLevelPreference() {
        String prefFile = LEVEL_DIR + "last_level.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(prefFile))) {
            pw.println(lastEngLevel);
        } catch (Exception ignored) {}
    }

    // ==================== 等级模式 ====================

    private void startLevelMode() {
        levelMode = true;
        clearAll();
        switchToTextArea();
        loadGroupProgress();
        loadCurrentGroup();
        updateStatsLabel();
        setStatsLabelVisible(true);
        setLevelProgressVisible(true);
        btnLevelSwitch.setText(currentEngLevel);
        btnLevelSwitch.setVisible(true);

        print("===== " + currentEngLevel + " 分组学习 =====");
        print("共 " + totalGroups + " 组，当前第 " + currentGroupIndex + " 组，共 " + listLen(levelList) + " 词");
        if (groupMastered) {
            print("本组已全部掌握，点击「继续测试」进入下一组");
        } else {
            print("点击「继续测试」开始测试");
        }
        print("点击「查看全部」浏览当前组词汇");

        state = 0;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        btnContinueTest.setVisible(true);
        btnLevelSwitch.setVisible(true);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
    }

    private void levelTestMode() {
        clearAll();
        switchToTextArea();
        state = 2;
        setTestButtonsVisible(true, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        btnContinueTest.setVisible(false);
        btnLevelSwitch.setVisible(true);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);

        int totalNonMastered = countNonMastered();
        if (totalNonMastered == 0) {
            groupMastered = true;
            saveCurrentGroup();
            print("恭喜！当前组 " + currentEngLevel + " Group" + currentGroupIndex + " 全部掌握！");
            if (currentGroupIndex < totalGroups) {
                print("点击「继续测试」进入下一组（Group" + (currentGroupIndex + 1) + "）");
            } else {
                print("恭喜！" + currentEngLevel + " 所有分组已全部掌握！");
            }
            updateLevelProgress();
            state = 9;
            setTestButtonsVisible(false, false);
            btnContinueTest.setVisible(true);
            btnLevelSwitch.setVisible(true);
            return;
        }

        EngNode selected = weightedRandomSelect();
        if (selected == null) {
            print("无可测试的词汇");
            state = 0;
            return;
        }

        currentTest = selected;
        recentTestedWords.add(selected);
        if (recentTestedWords.size() > RECENT_WINDOW * 2) {
            recentTestedWords = new ArrayList<>(recentTestedWords.subList(
                    recentTestedWords.size() - RECENT_WINDOW, recentTestedWords.size()));
        }

        String stateStr = getMasteryLabel(currentTest.masteryState);
        levelTestStateLabel.setText("状态: " + stateStr);
        levelTestWordLabel.setText("当前单词: " + currentTest.word);
        levelTestStatePanel.setVisible(true);

        print("===== " + currentEngLevel + " Group" + currentGroupIndex + " 测试 =====");
        print("单词: " + currentTest.word);
        print("\n请点击上方「显示答案」按钮查看答案");
    }

    // ==================== 加权随机 ====================

    private int countNonMastered() {
        int c = 0;
        EngNode p = levelList.next;
        while (p != null) {
            if (p.masteryState < 2) c++;
            p = p.next;
        }
        return c;
    }

    private EngNode weightedRandomSelect() {
        List<EngNode> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        EngNode p = levelList.next;
        int index = 0;
        while (p != null) {
            if (p.masteryState < 2) {
                candidates.add(p);
                double w = getWeight(p, index);
                weights.add(w);
            }
            index++;
            p = p.next;
        }

        if (candidates.isEmpty()) return null;

        double totalWeight = 0;
        for (double w : weights) totalWeight += w;

        Random r = new Random();
        double randomVal = r.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (randomVal <= cumulative) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    private double getWeight(EngNode node, int index) {
        double baseWeight;
        switch (node.masteryState) {
            case 0: baseWeight = 5.0; break;
            case 1: baseWeight = 2.0; break;
            default: baseWeight = 0.0; break;
        }
        baseWeight += node.wrongTimes * 3.0;

        int recencyIndex = recentTestedWords.indexOf(node);
        if (recencyIndex >= 0) {
            int distFromEnd = recentTestedWords.size() - recencyIndex;
            if (distFromEnd <= RECENT_WINDOW) {
                baseWeight += (RECENT_WINDOW - distFromEnd + 1) * 1.5;
            }
        }
        return baseWeight;
    }

    // ==================== 测试结果与掌握逻辑 ====================

    private void handleTestResult(EngNode node, boolean correct) {
        if (correct) {
            node.trueTimes++;
            node.wrongTimes = 0;
        } else {
            node.wrongTimes++;
        }
        node.examTimes++;

        double correctRate = node.examTimes == 0 ? 0
                : (double) node.trueTimes / node.examTimes * 100;

        if (levelMode) {
            handleLevelTestResult(node, correct);
        } else {
            handleLocalTestResult(node, correct, correctRate);
        }
    }

    private void handleLocalTestResult(EngNode node, boolean correct, double correctRate) {
        int wrong = node.examTimes - node.trueTimes;
        int requiredCorrect = 5 + wrong / 2;
        boolean mastered = node.trueTimes >= requiredCorrect;

        print("\n=====================================");
        print("              本次测试结果");
        print("=====================================");
        print("英文: " + node.word);
        print("释义: " + node.chinese);
        print("-------------------------------------");
        print("累计考核: " + node.examTimes + " 次");
        print("正确次数: " + node.trueTimes + " 次");
        print("正确率: " + String.format("%.1f", correctRate) + " %");
        print("-------------------------------------");
        print("【掌握规则】起始需答对5次，每答错2次则需多答对1次");
        print("当前需要答对: " + requiredCorrect + " 次");

        if (mastered) {
            print("\n已达到掌握要求，自动从词库移除！");
            deleteNode(node);
            saveToFile();
            showAllWordsAsCards();
            return;
        } else {
            int remaining = requiredCorrect - node.trueTimes;
            print("\n还需答对 " + remaining + " 次即可掌握，继续加油！");
        }

        saveToFile();
        state = 9;
        editTarget = node;
        setTestButtonsVisible(true, false);
        btnYes.setVisible(false);
        btnNo.setVisible(false);
        setEditButtonsVisible(false);
        btnEditWordField.setVisible(true);
        btnDeleteWord.setVisible(true);
        btnEditCnField.setVisible(false);
        btnFinishEdit.setVisible(false);

        btnShowAnswer.setText("继续测试");
        btnShowAnswer.setBackground(new Color(220, 255, 220));
        print("\n可点击【继续测试】进入下一题，或使用【修改单词】【删除单词】操作该单词。");
    }

    private void handleLevelTestResult(EngNode node, boolean correct) {
        print("\n=====================================");
        print(currentEngLevel + " Group" + currentGroupIndex + " 测试结果");
        print("=====================================");
        print("累计考核: " + node.examTimes + " 次");
        print("正确次数: " + node.trueTimes + " 次");

        if (correct) {
            switch (node.masteryState) {
                case 0:
                    node.masteryState = 1;
                    print("状态提升: 陌生 -> 了解");
                    break;
                case 1:
                    if (node.trueTimes >= getLevelRequiredCorrect(node)) {
                        node.masteryState = 2;
                        print("状态提升: 了解 -> 掌握！");
                    } else {
                        int need = getLevelRequiredCorrect(node) - node.trueTimes;
                        print("还需答对 " + need + " 次即可达到掌握");
                    }
                    break;
                default:
                    print("当前状态: 掌握");
                    break;
            }
            node.wrongTimes = 0;
        } else {
            print("回答错误！");
            int wrongTimes = node.examTimes - node.trueTimes;
            if (wrongTimes >= 2 && wrongTimes % 2 == 0) {
                print("已累计答错 " + wrongTimes + " 次，需要额外多答对 " + (wrongTimes / 2) + " 次");
            }
            if (node.examTimes - node.trueTimes > 3) {
                if (currentGroupIndex < totalGroups) {
                    appendToNextGroup(node);
                    print("\n累计答错超过3次，已自动追加至下一分组复习！");
                } else {
                    print("\n累计答错超过3次，但已是最后一组，无法继续追加。");
                }
            }
        }

        print("\n当前状态: " + getMasteryLabel(node.masteryState));
        print("\n===== 单词信息 =====");
        print("单词: " + node.word);
        print("释义: " + node.chinese);

        levelTestStateLabel.setText("状态: " + getMasteryLabel(node.masteryState));
        levelTestWordLabel.setText("当前单词: " + node.word);
        levelTestStatePanel.setVisible(true);

        saveCurrentGroup();
        updateLevelProgress();

        state = 9;
        setTestButtonsVisible(false, false);
        btnContinueTest.setVisible(true);
        btnEditWordField.setVisible(true);
        btnMarkMastered.setVisible(true);
        btnLevelSwitch.setVisible(true);
        btnEditCnField.setVisible(false);
        btnFinishEdit.setVisible(false);
        setStatsLabelVisible(true);
        print("\n可点击上方【继续测试】【修改单词】【掌握单词】（标记为掌握跳过该词）");
    }

    private int getLevelRequiredCorrect(EngNode node) {
        int wrong = node.examTimes - node.trueTimes;
        if (node.masteryState == 0) return 1;
        if (node.masteryState == 1) return 2 + wrong / 2;
        return 0;
    }

    private String getMasteryLabel(int state) {
        switch (state) {
            case 0: return "陌生";
            case 1: return "了解";
            case 2: return "掌握";
            default: return "未知";
        }
    }

    // ==================== 添加到本地 ====================

    private void addCurrentToLocal() {
        if (currentTest == null) return;
        int cfm = JOptionPane.showConfirmDialog(this,
                "将「" + currentTest.word + "」添加到本地词库 D:\\EngStu.txt？",
                "添加到本地", JOptionPane.YES_NO_OPTION);
        if (cfm != JOptionPane.YES_OPTION) return;

        EngNode newNode = new EngNode();
        newNode.word = currentTest.word;
        newNode.chinese = currentTest.chinese;
        newNode.examTimes = 0;
        newNode.trueTimes = 0;
        newNode.engLevel = 0;

        EngNode tail = globalList;
        while (tail.next != null) tail = tail.next;
        tail.next = newNode;
        saveToFile();
        updateStatsLabel();
        print("\n已添加到本地词库！");
    }

    // ==================== 等级切换 ====================

    private void switchLevel() {
        String[] options = {"CET4", "CET6", "IELTS", "TOEFL"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "请选择要学习的等级:", "切换等级词书",
                JOptionPane.QUESTION_MESSAGE, null, options, currentEngLevel);
        if (selected == null || selected.equals(currentEngLevel)) return;
        currentEngLevel = selected;
        lastEngLevel = selected;
        saveLevelPreference();
        btnLevelSwitch.setText(currentEngLevel);
        currentGroupIndex = 1;
        saveCurrentGroup();
        saveGroupProgress();
        saveCarryOver();
        allWrongOver3ThisSession = 0;
        saveCarryOver();
        startLevelMode();
    }

    // ==================== 等级进度条 ====================

    private void updateLevelProgress() {
        if (levelList == null) return;
        // 分组进度
        int groupMastered = 0, groupKnown = 0, groupStrange = 0, groupTotal = 0;
        EngNode p = levelList.next;
        while (p != null) {
            groupTotal++;
            switch (p.masteryState) {
                case 2: groupMastered++; break;
                case 1: groupKnown++; break;
                default: groupStrange++; break;
            }
            p = p.next;
        }

        // 词书整体进度（扫描所有分组文件）
        int bookTotal = 0, bookMastered = 0, bookKnown = 0;
        for (int g = 1; g <= totalGroups; g++) {
            File gf = new File(GROUP_LEVEL_DIR + currentEngLevel + "_Group" + g + ".txt");
            if (!gf.exists()) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(gf))) {
                br.readLine(); // header
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    bookTotal++;
                    String[] sp = line.split("\t");
                    if (sp.length > 7) {
                        int ms = Integer.parseInt(sp[7].trim());
                        if (ms == 2) bookMastered++;
                        else if (ms == 1) bookKnown++;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 更新词书进度条（第一行）
        if (bookTotal == 0) {
            levelGroupProgressBar.setValue(0);
            levelGroupProgressBar.setString("无数据");
            levelGroupLabel.setText("");
        } else {
            double bMasteredPct = (double) bookMastered / bookTotal * 100;
            double bKnownPct = (double) bookKnown / bookTotal * 100;
            double bStrangePct = (double) (bookTotal - bookMastered - bookKnown) / bookTotal * 100;
            levelGroupProgressBar.setString(String.format("%s  %.1f%%/%.1f%%/%.1f%%",
                    currentEngLevel, bMasteredPct, bKnownPct, bStrangePct));
            paintDualBar(levelGroupProgressBar, bookMastered, bookKnown,
                    bookTotal - bookMastered - bookKnown, bookTotal);
            levelGroupLabel.setText(String.format("共%d组  词书共%d词 已掌握%d  陌生%d 了解%d",
                    totalGroups, bookTotal, bookMastered,
                    bookTotal - bookMastered - bookKnown, bookKnown));
        }

        // 更新分组进度条（第二行）
        if (groupTotal == 0) {
            levelProgressBar.setValue(0);
            levelProgressBar.setString("无数据");
            levelProgressLabel.setText("");
        } else {
            double gMasteredPct = (double) groupMastered / groupTotal * 100;
            double gKnownPct = (double) groupKnown / groupTotal * 100;
            double gStrangePct = (double) groupStrange / groupTotal * 100;
            levelProgressBar.setString(String.format("Group%d  %.1f%%/%.1f%%/%.1f%%",
                    currentGroupIndex, gMasteredPct, gKnownPct, gStrangePct));
            paintDualBar(levelProgressBar, groupMastered, groupKnown, groupStrange, groupTotal);
            levelProgressLabel.setText(String.format("分组进度%d/%d  掌握%d 了解%d 陌生%d",
                    currentGroupIndex, totalGroups, groupMastered, groupKnown, groupStrange));
        }
    }

    private void paintDualBar(JProgressBar bar, int mastered, int known, int strange, int total) {
        if (total == 0) return;
        final double masteredPctExact = (double) mastered / total * 100;
        final double knownPctExact = (double) known / total * 100;

        bar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() { return Color.WHITE; }
            @Override
            protected Color getSelectionForeground() { return Color.WHITE; }
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Insets b = bar.getInsets();
                int w = bar.getWidth() - b.left - b.right;
                int h = bar.getHeight() - b.top - b.bottom;
                Graphics2D g2 = (Graphics2D) g.create();

                int masteredW = (int)(w * masteredPctExact / 100);
                int knownW = (int)(w * knownPctExact / 100);
                int strangeW = w - masteredW - knownW;

                g2.setColor(new Color(30, 80, 180));       // 掌握深蓝 (左侧)
                g2.fillRect(b.left, b.top, masteredW, h);
                g2.setColor(new Color(100, 180, 255));     // 了解浅蓝 (中间)
                g2.fillRect(b.left + masteredW, b.top, knownW, h);
                g2.setColor(new Color(180, 180, 180));     // 陌生灰 (右侧)
                g2.fillRect(b.left + masteredW + knownW, b.top, strangeW, h);

                g2.setColor(Color.WHITE);
                String txt = bar.getString();
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(txt);
                int tx = b.left + (w - tw) / 2;
                int ty = b.top + (h + fm.getAscent()) / 2 - 2;
                g2.drawString(txt, tx, ty);
                g2.dispose();
            }
        });
    }

    private void setLevelProgressVisible(boolean visible) {
        levelProgressPanel.setVisible(visible);
        if (visible) updateLevelProgress();
    }

    // ==================== 等级卡片视图 ====================

    private void showLevelAsCards() {
        clearAll();
        state = 0;
        setTestButtonsVisible(false, false);
        setEditButtonsVisible(false);
        btnDeleteWord.setVisible(false);
        btnContinueTest.setVisible(false);
        btnMarkMastered.setVisible(false);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
        editTarget = null;

        centerWrapPanel.removeAll();
        centerWrapPanel.add(cardScrollPane, BorderLayout.CENTER);
        cardPanel.removeAll();

        EngNode p = levelList.next;
        if (p == null) {
            showArea.setText("暂无等级词汇数据");
            centerWrapPanel.removeAll();
            centerWrapPanel.add(new JScrollPane(showArea), BorderLayout.CENTER);
            centerWrapPanel.revalidate();
            centerWrapPanel.repaint();
            return;
        }

        while (p != null) {
            final EngNode node = p;
            cardPanel.add(createLevelWordCard(node));
            p = p.next;
        }
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    private JPanel createLevelWordCard(EngNode node) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(390, 130));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel wordLabel = new JLabel("【" + getMasteryLabel(node.masteryState) + "】" + node.word);
        wordLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        JLabel cnLabel = new JLabel("释义: " + node.chinese);
        cnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        String rate = node.examTimes == 0 ? "0%" : (node.trueTimes * 100 / node.examTimes) + "%";
        JLabel statLabel = new JLabel("考核: " + node.examTimes + "  正确: " + node.trueTimes + "  正确率: " + rate);
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statLabel.setForeground(new Color(60, 120, 180));
        JLabel levelLabel = new JLabel("等级: " + getLevelName(node.engLevel));
        levelLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        levelLabel.setForeground(new Color(120, 120, 120));

        infoPanel.add(wordLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(cnLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(statLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(levelLabel);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    // ==================== ActionListener ====================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == btnImageReco) {
            enterImageRecognitionMode();
        } else if (obj == btnLevelStudy) {
            startLevelMode();
        } else if (obj == btnLevelSwitch) {
            switchLevel();
        } else if (obj == btnShowAll) {
            if (levelMode) {
                showLevelAsCards();
            } else {
                showAllWordsAsCards();
            }
        } else if (obj == btnAddWord) {
            addWordMode();
        } else if (obj == btnEditWord) {
            editWordMode();
        } else if (obj == btnRandomTest) {
            if (levelMode) {
                levelTestMode();
            } else {
                randomTest();
            }
        } else if (obj == btnExit) {
            if (levelMode) {
                saveCurrentGroup();
                saveGroupProgress();
                saveCarryOver();
            }
            saveToFile();
            System.exit(0);
        } else if (obj == btnShowAnswer) {
            if (state == 2 && currentTest != null) {
                // 显示答案
                print("\n--- 参考答案 ---");
                print("释义：" + currentTest.chinese);
                print("\n***是否记得？（点击上方按钮：记得、不记得）***");
                state = 3;
                setTestButtonsVisible(true, true);   // 只显示"记得"和"不记得"
                // 隐藏"显示答案"按钮，不显示继续测试
                btnShowAnswer.setVisible(false);
            } else if (state == 9 && btnShowAnswer.getText().equals("继续测试")) {
                // 在结果展示环节点击"继续测试"进入下一题
                if (levelMode) {
                    levelTestMode();
                } else {
                    randomTest();
                }
            }
        } else if (obj == btnYes || obj == btnNo) {
            if (state != 3 || currentTest == null) return;
            boolean ok = (obj == btnYes);
            handleTestResult(currentTest, ok);
        } else if (obj == btnContinueTest) {
            if (levelMode) {
                testedCount = 0;
                testedIndex = new int[1000];
                recentTestedWords.clear();
                if (groupMastered && currentGroupIndex < totalGroups) {
                    currentGroupIndex++;
                    saveGroupProgress();
                    saveCarryOver();
                    allWrongOver3ThisSession = 0;
                    loadCurrentGroup();
                    updateStatsLabel();
                    setLevelProgressVisible(true);
                    print("===== 进入下一组：" + currentEngLevel + " Group" + currentGroupIndex + " =====");
                    print("共 " + listLen(levelList) + " 词，点击「继续测试」开始学习");
                    state = 0;
                    setTestButtonsVisible(false, false);
                    btnContinueTest.setVisible(true);
                    btnLevelSwitch.setVisible(true);
                    setEditButtonsVisible(false);
                    btnDeleteWord.setVisible(false);
                } else {
                    levelTestMode();
                }
            }
        } else if (obj == btnEditWordField) {
            if (editTarget == null) return;
            editField = 1;
            state = 8;
            print("\n当前单词：" + editTarget.word + "\n请在上方输入框中输入新单词：");
            inputField.setVisible(true);
            btnInputConfirm.setVisible(true);
            setEditButtonsVisible(true);
            btnDeleteWord.setVisible(true);
        } else if (obj == btnEditCnField) {
            if (editTarget == null) return;
            editField = 2;
            state = 8;
            print("\n当前释义：" + editTarget.chinese + "\n请在上方输入框中输入新释义：");
            inputField.setVisible(true);
            btnInputConfirm.setVisible(true);
        } else if (obj == btnFinishEdit) {
            if (editTarget == null) return;
            if (levelMode) {
                saveCurrentGroup();
            } else {
                saveToFile();
            }
            clearAll();
            print("\n===== 修改完成 =====");
            print("单词：" + editTarget.word);
            print("释义：" + editTarget.chinese);
            print("已保存");
            if (levelMode) {
                showLevelAsCards();
            } else {
                showAllWordsAsCards();
            }
        } else if (obj == btnDeleteWord) {
            if (editTarget == null) return;
            if (levelMode) {
                JOptionPane.showMessageDialog(this, "等级词书中的单词不能删除，可使用「掌握单词」跳过。");
                return;
            }
            int cfm = JOptionPane.showConfirmDialog(this,
                    "确定删除【" + editTarget.word + "】？", "删除", JOptionPane.YES_NO_OPTION);
            if (cfm == JOptionPane.YES_OPTION) {
                deleteNode(editTarget);
                saveToFile();
                showAllWordsAsCards();
            }
        } else if (obj == btnMarkMastered) {
            if (editTarget == null || !levelMode) return;
            int cfm = JOptionPane.showConfirmDialog(this,
                    "将「" + editTarget.word + "」直接标记为掌握？\n（已掌握单词不会再次出现在测试中）",
                    "掌握单词", JOptionPane.YES_NO_OPTION);
            if (cfm == JOptionPane.YES_OPTION) {
                editTarget.masteryState = 2;
                editTarget.wrongTimes = 0;
                saveCurrentGroup();
                updateLevelProgress();
                updateStatsLabel();
                print("\n已将「" + editTarget.word + "」标记为掌握！");
                // 继续测试下一题
                levelTestMode();
            }
        }
    }

    // ==================== WrapLayout ====================
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetW = target.getWidth();
                if (targetW <= 0) targetW = Integer.MAX_VALUE;
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetW - insets.left - insets.right - hgap * 2;
                int rowW = 0, rowH = 0;
                int totalW = 0, totalH = 0;
                int n = target.getComponentCount();
                for (int i = 0; i < n; i++) {
                    Component c = target.getComponent(i);
                    if (c.isVisible()) {
                        Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                        if (rowW + d.width > maxWidth) {
                            totalW = Math.max(totalW, rowW);
                            totalH += rowH + vgap;
                            rowW = d.width;
                            rowH = d.height;
                        } else {
                            rowW += d.width + hgap;
                            rowH = Math.max(rowH, d.height);
                        }
                    }
                }
                totalW = Math.max(totalW, rowW);
                totalH += rowH;
                return new Dimension(insets.left + totalW + insets.right,
                        insets.top + totalH + insets.bottom);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EngStuJFrame::new);
    }
}