package LanguageStudySystem.JavaJapStuSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JapStuJFrame extends JFrame
        implements ActionListener, JapImageRecognition.RecognitionCallback, JapEditAndDel.EditorContext {

    // 顶部功能按钮
    private JButton btnShowAll;
    private JButton btnAddWord;
    private JButton btnEditWord;
    private JButton btnLocalTest;
    private JButton btnExit;
    private JButton btnImageReco;
    private JButton btnJLPT;

    // JLPT 等级选择弹出菜单
    private JPopupMenu jlptMenu;
    private JButton btnJLPTLevelSwitch;
    private String currentJLPTLevel = "N5";
    private String lastJLPTLevel = "N5";

    // 测试专用按钮
    private JButton btnContinueTest;
    private JButton btnShowAnswer;
    private JButton btnYes;
    private JButton btnNo;
    private JButton btnEditAfterTest;
    private JButton btnDeleteAfterTest;
    private JButton btnAddToLocal;

    // 编辑专用按钮
    private JButton btnEditJpField;
    private JButton btnEditCnField;
    private JButton btnEditExField;
    private JButton btnEditExcField;
    private JButton btnFinishEdit;
    private JButton btnDeleteWord;
    private JButton btnConvertToGrammar;

    // 类型选择按钮
    private JButton btnTypeWord;
    private JButton btnTypeGram;

    // 输入与显示组件
    private JTextField inputField;
    private JButton btnInputConfirm;
    private CardGridView cardView;
    private JPanel topBtnPanel;
    private JPanel inputPanel;
    private JPanel northPanel;
    private JPanel centerWrapPanel;
    private JLabel statsLabel;
    private JPanel statsPanel;
    private JPanel jlptProgressPanel;
    private JProgressBar jlptProgressBar;      // 词书整体进度
    private JProgressBar jlptGroupProgressBar;  // 当前分组进度
    private JLabel jlptProgressLabel;
    private JLabel jlptGroupLabel;

    private JPanel jlptTestStatePanel;
    private JLabel jlptTestStateLabel;
    private JLabel jlptTestWordLabel;

    // 编辑按钮行面板
    private JPanel editBtnRow;

    private JPanel textContentPanel;
    private JScrollPane textScrollPane;

    private StringBuilder htmlContent = new StringBuilder();
    private JTextPane activeTextPane = null;

    private static final Color CARE_COLOR = new Color(255, 251, 240);
    private static final int TEXT_SIZE = 18;
    private static final int TABLE_TEXT_SIZE = 24;

    // 本地词库路径
    private static final String FILE_PATH = "D:/JaStu.txt";
    // JLPT 嵌入源路径（随程序打包）
    private static final String JLPT_SRC_DIR = "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaJapStuSystem/JLPT/SortedJLPT/";
    // JLPT 工作路径（用户数据，程序更新后仍保留进度）
    private static final String JLPT_DIR = "D:/JSS/JLPT/SortedJLPT/";

    // 分组词库路径
    private static final String GROUP_JLPT_SRC_DIR = "D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaJapStuSystem/JLPT/GroupJLPT/";
    private static final String GROUP_JLPT_DIR = "D:/JSS/JLPT/GroupJLPT/";
    private static final int GROUP_SIZE = 15;

    static class JaNode {
        String japanese;
        String chinese;
        int type;
        int examTimes;
        int trueTimes;
        String example;
        String exampleCh;
        int masteryState;  // 0=陌生, 1=了解, 2=掌握
        int jlptLevel;     // 0=本地, 1=N5, 2=N4, 3=N3, 4=N2, 5=N1
        int wrongTimes;    // 本次会话中连续答错次数
        JaNode next;
    }

    private JaNode globalList;
    private JaNode jlptList;
    private boolean jlptMode = false;
    private int state = 0;
    private String tmpJp = "", tmpCn = "", tmpEx = "", tmpExc = "";
    private int tmpStep = 0;
    private JaNode currentTest = null;
    private int[] testedIndex = new int[1000];
    private int testedCount = 0;

    private int totalItems = 0;
    private int masteredItems = 0;
    private boolean showAllTargetLocal = true;
    private int jlptTotalItems = 0;
    private int jlptMasteredCount = 0;
    private int jlptKnownCount = 0;
    private int jlptStrangeCount = 0;

    // 分组学习
    private int currentGroupIndex = 1;
    private int totalGroups = 0;
    private boolean groupMastered = false;
    private int allUndoneThisSession = 0;
    private int allWrongOver3ThisSession = 0;

    // 最近测试的词汇（用于加权随机）
    private java.util.List<JaNode> recentTestedWords = new ArrayList<>();
    private static final int RECENT_WINDOW = 10;

    private static final int STATE_IMAGE_RECOGNITION = 10;
    private static final int STATE_TEST_RESULT = 9;
    private static final int STATE_JLPT_MENU = 11;

    private JapEditAndDel editAndDel;
    private Random random = new Random();

    public JapStuJFrame() {
        initJFrame();
        initNorthPanel();
        initCenterView();
        globalList = initList();
        jlptList = initList();
        readFromFile();
        ensureJLPTWorkDir();
        ensureGroupJLPTWorkDir();
        editAndDel = new JapEditAndDel(this);
        updateStatsLabel();
        setStatsLabelVisible(false);
        loadJLPTLevelPreference();
        showStartupStatus();
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
    }

    private void initJFrame() {
        setTitle("日文学习系统 V3.1.1");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
    }

    private void initNorthPanel() {
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        initTopButtonPanel();
        initInputPanel();

        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 20));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statsLabel.setForeground(new Color(60, 120, 180));
        statsPanel.add(statsLabel);

        jlptProgressPanel = new JPanel();
        jlptProgressPanel.setLayout(new BoxLayout(jlptProgressPanel, BoxLayout.Y_AXIS));
        jlptProgressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jlptProgressPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 20));
        jlptProgressPanel.setVisible(false);

        JPanel groupBarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        jlptGroupProgressBar = new JProgressBar(0, 100);
        jlptGroupProgressBar.setPreferredSize(new Dimension(300, 18));
        jlptGroupProgressBar.setStringPainted(true);
        jlptGroupProgressBar.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        jlptGroupLabel = new JLabel();
        jlptGroupLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        jlptGroupLabel.setForeground(new Color(80, 80, 80));
        groupBarRow.add(new JLabel("分组 "));
        groupBarRow.add(jlptGroupProgressBar);
        groupBarRow.add(Box.createHorizontalStrut(10));
        groupBarRow.add(jlptGroupLabel);

        JPanel bookBarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        jlptProgressBar = new JProgressBar(0, 100);
        jlptProgressBar.setPreferredSize(new Dimension(300, 18));
        jlptProgressBar.setStringPainted(true);
        jlptProgressBar.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        jlptProgressLabel = new JLabel();
        jlptProgressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        jlptProgressLabel.setForeground(new Color(80, 80, 80));
        jlptProgressLabel.setPreferredSize(new Dimension(500, 20));
        bookBarRow.add(new JLabel("词书 "));
        bookBarRow.add(jlptProgressBar);
        bookBarRow.add(Box.createHorizontalStrut(10));
        bookBarRow.add(jlptProgressLabel);

        jlptProgressPanel.add(groupBarRow);
        jlptProgressPanel.add(bookBarRow);

        jlptTestStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        jlptTestStatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jlptTestStatePanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 20));
        jlptTestStatePanel.setVisible(false);

        jlptTestStateLabel = new JLabel();
        jlptTestStateLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        jlptTestStateLabel.setForeground(new Color(200, 100, 0));

        jlptTestWordLabel = new JLabel();
        jlptTestWordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        jlptTestWordLabel.setForeground(new Color(80, 80, 80));

        jlptTestStatePanel.add(jlptTestStateLabel);
        jlptTestStatePanel.add(Box.createHorizontalStrut(15));
        jlptTestStatePanel.add(jlptTestWordLabel);

        northPanel.add(topBtnPanel);
        northPanel.add(statsPanel);
        northPanel.add(jlptProgressPanel);
        northPanel.add(jlptTestStatePanel);
        northPanel.add(inputPanel);
        add(northPanel, BorderLayout.NORTH);
    }

    private void initTopButtonPanel() {
        topBtnPanel = new JPanel();
        topBtnPanel.setLayout(new BoxLayout(topBtnPanel, BoxLayout.Y_AXIS));
        topBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        btnShowAll    = new JButton("查看全部");
        btnAddWord    = new JButton("添加考察点");
        btnEditWord   = new JButton("查找考察点");
        btnLocalTest  = new JButton("测试本地词库");
        btnExit       = new JButton("保存退出");
        btnImageReco  = new JButton("图片识别");
        btnJLPT       = new JButton("JLPT词书");

        btnTypeWord = new JButton("单词");
        btnTypeGram = new JButton("语法");

        Color blue = new Color(235, 245, 255);
        btnTypeWord.setBackground(blue);
        btnTypeGram.setBackground(blue);

        row1.add(btnShowAll);
        row1.add(btnAddWord);
        row1.add(btnEditWord);
        row1.add(btnLocalTest);
        row1.add(btnImageReco);
        row1.add(btnJLPT);
        btnJLPTLevelSwitch = new JButton("JLPT " + lastJLPTLevel);
        btnJLPTLevelSwitch.setBackground(blue);
        btnJLPTLevelSwitch.addActionListener(e -> {
            if (jlptMode) saveJLPTToFile();
            jlptMenu.show(btnJLPTLevelSwitch, 0, btnJLPTLevelSwitch.getHeight());
        });
        btnJLPTLevelSwitch.setVisible(false);

        row1.add(btnExit);
        row1.add(btnJLPTLevelSwitch);
        row1.add(btnTypeWord);
        row1.add(btnTypeGram);

        // JLPT 弹出菜单
        jlptMenu = new JPopupMenu();
        String[] levels = {"N5", "N4", "N3", "N2", "N1"};
        for (String level : levels) {
            JMenuItem item = new JMenuItem(level);
            item.addActionListener(e -> switchJLPTLevel(level));
            jlptMenu.add(item);
        }

        btnJLPT.addActionListener(e -> {
            if (jlptMode) {
                saveCurrentGroup();
                saveGroupProgress();
                saveCarryOver();
            }
            loadJLPTLevelPreference();
            currentJLPTLevel = lastJLPTLevel;
            switchJLPTLevel(currentJLPTLevel);
        });

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        btnContinueTest = new JButton("继续测试");
        btnContinueTest.setBackground(new Color(235, 245, 255));
        row2.add(btnContinueTest);

        btnShowAnswer      = new JButton("显示答案");
        btnYes             = new JButton("记得");
        btnNo              = new JButton("不记得");
        btnEditAfterTest   = new JButton("修改考察点");
        btnDeleteAfterTest = new JButton("删除考察点");
        btnAddToLocal      = new JButton("添加到本地");

        btnShowAnswer.setBackground(blue);
        btnYes.setBackground(blue);
        btnNo.setBackground(blue);
        btnEditAfterTest.setBackground(blue);
        btnDeleteAfterTest.setBackground(new Color(255, 235, 235));
        btnAddToLocal.setBackground(new Color(230, 255, 230));

        row2.add(btnShowAnswer);
        row2.add(btnYes);
        row2.add(btnNo);
        row2.add(btnEditAfterTest);
        row2.add(btnAddToLocal);
        row2.add(btnDeleteAfterTest);

        editBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        editBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtnRow.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        btnEditJpField  = new JButton("修改日文");
        btnEditCnField  = new JButton("修改释义");
        btnEditExField  = new JButton("修改例句");
        btnEditExcField = new JButton("修改例句译");
        btnFinishEdit   = new JButton("完成修改");
        btnDeleteWord   = new JButton("删除");
        btnConvertToGrammar = new JButton("修改为语法");

        Color red = new Color(255, 235, 235);
        Color green = new Color(240, 255, 240);
        btnEditJpField.setBackground(blue);
        btnEditCnField.setBackground(blue);
        btnEditExField.setBackground(blue);
        btnEditExcField.setBackground(blue);
        btnFinishEdit.setBackground(blue);
        btnDeleteWord.setBackground(red);
        btnConvertToGrammar.setBackground(green);

        editBtnRow.add(btnEditJpField);
        editBtnRow.add(btnEditCnField);
        editBtnRow.add(btnEditExField);
        editBtnRow.add(btnEditExcField);
        editBtnRow.add(btnFinishEdit);
        editBtnRow.add(btnDeleteWord);
        editBtnRow.add(btnConvertToGrammar);

        topBtnPanel.add(row1);
        topBtnPanel.add(row2);
        topBtnPanel.add(editBtnRow);

        btnContinueTest.addActionListener(this);
        btnShowAll.addActionListener(this);
        btnAddWord.addActionListener(this);
        btnEditWord.addActionListener(this);
        btnLocalTest.addActionListener(this);
        btnExit.addActionListener(this);
        btnImageReco.addActionListener(this);
        btnShowAnswer.addActionListener(this);
        btnYes.addActionListener(this);
        btnNo.addActionListener(this);
        btnEditAfterTest.addActionListener(this);
        btnDeleteAfterTest.addActionListener(this);
        btnAddToLocal.addActionListener(this);
        btnEditJpField.addActionListener(this);
        btnEditCnField.addActionListener(this);
        btnEditExField.addActionListener(this);
        btnEditExcField.addActionListener(this);
        btnFinishEdit.addActionListener(this);
        btnDeleteWord.addActionListener(this);
        btnConvertToGrammar.addActionListener(this);
        btnTypeWord.addActionListener(this);
        btnTypeGram.addActionListener(this);

        setTestButtonsVisible(false, false, false);
        btnContinueTest.setVisible(false);
        btnAddToLocal.setVisible(false);
        setEditButtonsVisible(false);
        setTypeButtonsVisible(false);
    }

    private void initInputPanel() {
        inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        btnInputConfirm = new JButton("确认输入");
        btnInputConfirm.setBackground(new Color(235, 245, 255));
        btnInputConfirm.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnInputConfirm.addActionListener(this);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(btnInputConfirm, BorderLayout.EAST);

        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
    }

    private void initCenterView() {
        centerWrapPanel = new JPanel(new BorderLayout());
        centerWrapPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        textContentPanel = new JPanel();
        textContentPanel.setLayout(new BoxLayout(textContentPanel, BoxLayout.Y_AXIS));
        textContentPanel.setBackground(CARE_COLOR);
        textContentPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        textScrollPane = new JScrollPane(textContentPanel);
        textScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        textScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textScrollPane.getViewport().setBackground(CARE_COLOR);

        cardView = new CardGridView();

        centerWrapPanel.add(textScrollPane, BorderLayout.CENTER);
        add(centerWrapPanel, BorderLayout.CENTER);
    }

    // ==================== 文本内容区管理 ====================

    private void ensureActiveTextPane() {
        if (activeTextPane == null) {
            activeTextPane = createTextPane();
            htmlContent = new StringBuilder();
            textContentPanel.add(activeTextPane);
        }
    }

    private JTextPane createTextPane() {
        JTextPane tp = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(0, d.height);
            }
        };
        tp.setContentType("text/html");
        tp.setEditable(false);
        tp.setBackground(CARE_COLOR);
        tp.setAlignmentX(Component.LEFT_ALIGNMENT);
        tp.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return tp;
    }

    private void flushActiveTextPane() {
        if (activeTextPane != null) {
            activeTextPane.setText(
                    "<html><body style='font-family:微软雅黑; font-size:" + TEXT_SIZE + "px; margin:0; padding:0;'>"
                            + htmlContent.toString()
                            + "</body></html>");
        }
    }

    @Override
    public void print(String s) {
        append(s, false);
    }

    @Override
    public void printBold(String s) {
        append(s, true);
    }

    private void append(String text, boolean bold) {
        if (text == null) return;
        ensureActiveTextPane();
        String safe = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        if (bold) {
            htmlContent.append("<b>").append(safe).append("</b>");
        } else {
            htmlContent.append(safe);
        }
        htmlContent.append("<br>");
        flushActiveTextPane();
        textContentPanel.revalidate();
        textContentPanel.repaint();
    }

    @Override
    public void embedGrammarInfo(JaNode node) {
        if (node == null) return;

        activeTextPane = null;
        htmlContent = new StringBuilder();

        Font baseFont  = new Font("微软雅黑", Font.PLAIN, TABLE_TEXT_SIZE);
        Font rubyFont  = new Font("微软雅黑", Font.PLAIN, TABLE_TEXT_SIZE / 2 + 2);
        Font labelFont = new Font("微软雅黑", Font.BOLD, TABLE_TEXT_SIZE);

        java.util.List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"语法点", node.japanese});
        rows.add(new Object[]{"释义", node.chinese});

        boolean hasExample = node.example != null && !node.example.isEmpty()
                && !"null".equals(node.example);
        if (hasExample) {
            JPanel rubyPanel = JapJFrameKanaPrint.createRubyPanel(
                    node.example, baseFont, rubyFont,
                    new Color(80, 80, 80), CARE_COLOR);
            rows.add(new Object[]{"例句", rubyPanel});

            boolean hasExc = node.exampleCh != null && !node.exampleCh.isEmpty()
                    && !"null".equals(node.exampleCh);
            if (hasExc) {
                rows.add(new Object[]{"例句译", node.exampleCh});
            }
        }

        JPanel tableMock = new JPanel();
        tableMock.setLayout(new BoxLayout(tableMock, BoxLayout.Y_AXIS));
        tableMock.setBackground(CARE_COLOR);
        tableMock.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableMock.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 180)));

        for (Object[] row : rows) {
            String labelText = (String) row[0];
            Object value     = row[1];

            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.setBackground(CARE_COLOR);
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 200, 180)));

            JLabel lblCol = new JLabel(labelText);
            lblCol.setFont(labelFont);
            lblCol.setBackground(new Color(235, 230, 215));
            lblCol.setOpaque(true);
            lblCol.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            lblCol.setPreferredSize(new Dimension(100, lblCol.getPreferredSize().height));
            lblCol.setMinimumSize(new Dimension(100, 0));
            lblCol.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
            rowPanel.add(lblCol);

            if (value instanceof JPanel) {
                JPanel valPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
                valPanel.setBackground(CARE_COLOR);
                valPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                valPanel.add((JPanel) value);
                valPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JScrollPane valScroll = new JScrollPane(valPanel);
                valScroll.setBorder(null);
                valScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                valScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                valScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                int prefH = valPanel.getPreferredSize().height + 4;
                valScroll.setMinimumSize(new Dimension(0, prefH));
                valScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefH));
                valScroll.setPreferredSize(new Dimension(0, prefH));
                rowPanel.add(valScroll);
            } else {
                JTextArea valArea = new JTextArea((String) value);
                valArea.setFont(baseFont);
                valArea.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                valArea.setForeground(new Color(80, 80, 80));
                valArea.setBackground(CARE_COLOR);
                valArea.setLineWrap(true);
                valArea.setWrapStyleWord(true);
                valArea.setEditable(false);
                valArea.setAlignmentX(Component.LEFT_ALIGNMENT);

                JScrollPane scrollPane = new JScrollPane(valArea);
                scrollPane.setBorder(null);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                int lineH = baseFont.getSize() + 14;
                scrollPane.setMinimumSize(new Dimension(0, lineH));
                scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                scrollPane.setPreferredSize(new Dimension(0, lineH));
                rowPanel.add(scrollPane);
            }

            tableMock.add(rowPanel);
        }

        textContentPanel.add(tableMock);
        textContentPanel.revalidate();
        textContentPanel.repaint();
    }

    // ==================== 接口实现 ====================

    @Override
    public void clearAll() {
        textContentPanel.removeAll();
        activeTextPane = null;
        htmlContent = new StringBuilder();
        textContentPanel.revalidate();
        textContentPanel.repaint();
        if (cardView != null) cardView.clearCards();
    }

    @Override
    public void switchToTextArea() {
        centerWrapPanel.removeAll();
        centerWrapPanel.add(textScrollPane, BorderLayout.CENTER);
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    @Override
    public void onCancel() {
        showAllAsCards();
    }

    @Override
    public void onAddWords(List<JaNode> words) {
        if (words == null || words.isEmpty()) return;
        JaNode tail = globalList;
        while (tail.next != null) tail = tail.next;
        for (JaNode node : words) {
            JaNode newNode = new JaNode();
            newNode.japanese  = node.japanese;
            newNode.chinese   = node.chinese;
            newNode.type      = node.type;
            newNode.examTimes = 0;
            newNode.trueTimes = 0;
            newNode.example   = node.example;
            newNode.exampleCh = node.exampleCh;
            newNode.masteryState = 0;
            newNode.jlptLevel = 0;
            tail.next = newNode;
            tail      = newNode;
        }
        totalItems += words.size();
        saveToFile();
        updateStatsLabel();
        JOptionPane.showMessageDialog(this, "成功添加 " + words.size() + " 个考察点！");
        showAllAsCards();
    }

    private void showAllAsCards() {
        clearAll();
        state = 0;
        jlptMode = false;
        resetAllButtonVisibility();
        totalItems = listLen(globalList);
        updateStatsLabel();
        setStatsLabelVisible(true);
        setJLPTProgressVisible(false);

        centerWrapPanel.removeAll();
        centerWrapPanel.add(cardView.getScrollPane(), BorderLayout.CENTER);
        cardView.clearCards();

        JaNode p = globalList.next;
        if (p == null) {
            print("暂无数据");
            switchToTextArea();
            return;
        }

        while (p != null) {
            try {
                JPanel card = createWordCard(p, false);
                cardView.addCard(card);
            } catch (Exception ex) {
                System.err.println("创建卡片异常: " + p.japanese + " - " + ex.getMessage());
            }
            p = p.next;
        }

        cardView.showCards();
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    private void showJLPTAsCards() {
        clearAll();
        state = 0;
        resetAllButtonVisibility();
        updateStatsLabel();
        setStatsLabelVisible(true);
        setJLPTProgressVisible(true);
        btnJLPTLevelSwitch.setVisible(true);

        centerWrapPanel.removeAll();
        centerWrapPanel.add(cardView.getScrollPane(), BorderLayout.CENTER);
        cardView.clearCards();

        JaNode p = jlptList.next;
        if (p == null) {
            print("暂无 JLPT " + currentJLPTLevel + " 数据");
            switchToTextArea();
            return;
        }

        while (p != null) {
            try {
                JPanel card = createWordCard(p, true);
                cardView.addCard(card);
            } catch (Exception ex) {
                System.err.println("创建卡片异常: " + p.japanese + " - " + ex.getMessage());
            }
            p = p.next;
        }

        cardView.showCards();
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    private JPanel createWordCard(JaNode node, boolean jlptCard) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        String prefix;
        if (jlptCard) {
            switch (node.masteryState) {
                case 2: prefix = "【掌握】"; break;
                case 1: prefix = "【了解】"; break;
                default: prefix = "【陌生】"; break;
            }
        } else {
            prefix = node.type == 1 ? "【单词】" : "【语法】";
        }

        JLabel titleLabel = new JLabel(prefix + node.japanese);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 17));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(6));

        JLabel cnLabel = new JLabel("释义：" + node.chinese);
        cnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        cnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(cnLabel);

        if (node.type == 2 && !jlptCard) {
            infoPanel.add(Box.createVerticalStrut(6));
            JPanel exampleLine = new JPanel(new BorderLayout(4, 0));
            exampleLine.setBackground(Color.WHITE);
            exampleLine.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel exLabel = new JLabel("例句：");
            exLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            exampleLine.add(exLabel, BorderLayout.WEST);
            JPanel rubyExample = JapJFrameKanaPrint.createRubyPanel(
                    node.example,
                    new Font("微软雅黑", Font.PLAIN, 14),
                    new Font("微软雅黑", Font.PLAIN, 10),
                    new Color(100, 100, 100), Color.WHITE);
            exampleLine.add(rubyExample, BorderLayout.CENTER);
            infoPanel.add(exampleLine);
            infoPanel.add(Box.createVerticalStrut(4));
            JTextArea excArea = new JTextArea("例句译文：" + node.exampleCh);
            excArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            excArea.setForeground(new Color(80, 80, 80));
            excArea.setLineWrap(true);
            excArea.setWrapStyleWord(true);
            excArea.setEditable(false);
            excArea.setBackground(Color.WHITE);
            excArea.setBorder(null);
            excArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(excArea);
        }

        infoPanel.add(Box.createVerticalStrut(6));

        if (jlptCard) {
            String stateStr;
            switch (node.masteryState) {
                case 2: stateStr = "掌握"; break;
                case 1: stateStr = "了解（再答对2次升为掌握）"; break;
                default: stateStr = "陌生（答对1次升为了解）"; break;
            }
            JLabel masteryLabel = new JLabel("状态：" + stateStr);
            masteryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            masteryLabel.setForeground(new Color(180, 120, 60));
            masteryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(masteryLabel);
            infoPanel.add(Box.createVerticalStrut(4));
        }

        JLabel statLabel = new JLabel("考核：" + node.examTimes + "  正确：" + node.trueTimes);
        statLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statLabel.setForeground(new Color(60, 120, 180));
        statLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(statLabel);

        if (node.type == 2 && !jlptCard) {
            JScrollPane contentScroll = new JScrollPane(infoPanel);
            contentScroll.setBorder(null);
            contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentScroll.getVerticalScrollBar().setUnitIncrement(16);
            card.add(contentScroll, BorderLayout.CENTER);
        } else {
            card.add(infoPanel, BorderLayout.CENTER);
        }

        if (!jlptCard) {
            JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 8));
            btnPanel.setBackground(Color.WHITE);
            btnPanel.setBorder(new EmptyBorder(10, 5, 10, 10));

            JButton editBtn = new JButton("修改");
            JButton delBtn  = new JButton("删除");
            editBtn.setBackground(new Color(240, 245, 255));
            delBtn.setBackground(new Color(255, 240, 240));

            editBtn.addActionListener(e -> editAndDel.enterEditMode(node));
            delBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "确定删除该内容？", "删除确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    editAndDel.deleteNode(node);
                    showAllAsCards();
                }
            });

            btnPanel.add(editBtn);
            btnPanel.add(delBtn);
            card.add(btnPanel, BorderLayout.EAST);
        }

        return card;
    }

    // ==================== JLPT 进度条 ====================

    private void updateJLPTProgress() {
        // 分组进度
        int groupMastered = 0, groupKnown = 0, groupStrange = 0, groupTotal = 0;
        JaNode p = jlptList.next;
        while (p != null) {
            groupTotal++;
            switch (p.masteryState) {
                case 2: groupMastered++; break;
                case 1: groupKnown++; break;
                default: groupStrange++; break;
            }
            p = p.next;
        }
        jlptTotalItems = groupTotal;
        jlptMasteredCount = groupMastered;
        jlptKnownCount = groupKnown;
        jlptStrangeCount = groupStrange;

        // 词书整体进度（扫描所有分组文件）
        int bookTotal = 0, bookMastered = 0, bookKnown = 0;
        for (int g = 1; g <= totalGroups; g++) {
            File gf = new File(GROUP_JLPT_DIR + currentJLPTLevel + "_Group" + g + ".txt");
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

        // 更新分组进度条
        if (groupTotal == 0) {
            jlptGroupProgressBar.setValue(0);
            jlptGroupProgressBar.setString("无数据");
            jlptGroupLabel.setText("");
        } else {
            double gMasteredPct = (double) groupMastered / groupTotal * 100;
            double gKnownPct = (double) groupKnown / groupTotal * 100;
            double gStrangePct = (double) groupStrange / groupTotal * 100;
            jlptGroupProgressBar.setString(String.format("Group%d  %.1f%%/%.1f%%/%.1f%%",
                    currentGroupIndex, gStrangePct, gKnownPct, gMasteredPct));
            paintDualBar(jlptGroupProgressBar, groupMastered, groupKnown, groupStrange, groupTotal);
            jlptGroupLabel.setText(String.format("陌生%d 了解%d 掌握%d", groupStrange, groupKnown, groupMastered));
        }

        // 更新词书进度条
        if (bookTotal == 0) {
            jlptProgressBar.setValue(0);
            jlptProgressBar.setString("无数据");
            jlptProgressLabel.setText("");
        } else {
            double bMasteredPct = (double) bookMastered / bookTotal * 100;
            double bKnownPct = (double) bookKnown / bookTotal * 100;
            double bStrangePct = (double) (bookTotal - bookMastered - bookKnown) / bookTotal * 100;
            jlptProgressBar.setString(String.format("%s  %.1f%%/%.1f%%/%.1f%%",
                    currentJLPTLevel, bStrangePct, bKnownPct, bMasteredPct));
            paintDualBar(jlptProgressBar, bookMastered, bookKnown, bookTotal - bookMastered - bookKnown, bookTotal);
            jlptProgressLabel.setText(String.format("共%d组 本组%d/%d  词书共%d词 已掌握%d  分组进度%d/%d",
                    totalGroups, currentGroupIndex, totalGroups, bookTotal, bookMastered,
                    groupMastered, groupTotal));
        }
    }

    private void paintDualBar(JProgressBar bar, int mastered, int known, int strange, int total) {
        if (total == 0) return;
        double masteredPctExact = (double) mastered / total * 100;
        double knownPctExact = (double) known / total * 100;

        bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
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

                g2.setColor(new Color(180, 180, 180));     // 陌生灰
                g2.fillRect(b.left, b.top, strangeW, h);
                g2.setColor(new Color(100, 180, 255));     // 了解浅蓝
                g2.fillRect(b.left + strangeW, b.top, knownW, h);
                g2.setColor(new Color(30, 80, 180));       // 掌握深蓝
                g2.fillRect(b.left + strangeW + knownW, b.top, masteredW, h);

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

    private void setJLPTProgressVisible(boolean visible) {
        jlptProgressPanel.setVisible(visible);
        if (visible) updateJLPTProgress();
    }

    // ==================== EditorContext 接口实现 ====================

    @Override
    public void setEditButtonsVisible(boolean visible) {
        btnEditJpField.setVisible(visible);
        btnEditCnField.setVisible(visible);
        btnEditExField.setVisible(visible);
        btnEditExcField.setVisible(visible);
        btnFinishEdit.setVisible(visible);
        btnDeleteWord.setVisible(visible);
        btnConvertToGrammar.setVisible(visible);
    }

    @Override
    public void showEditButtonsForType(int type) {
        btnEditJpField.setVisible(true);
        btnEditCnField.setVisible(true);
        btnFinishEdit.setVisible(true);
        btnDeleteWord.setVisible(true);
        btnEditExField.setVisible(type == 2);
        btnEditExcField.setVisible(type == 2);
        btnConvertToGrammar.setVisible(type == 1);
    }

    @Override
    public void setConvertToGrammarButtonVisible(boolean visible) {
        btnConvertToGrammar.setVisible(visible);
    }

    @Override
    public void setInputVisible(boolean visible) {
        inputField.setVisible(visible);
        btnInputConfirm.setVisible(visible);
    }

    @Override
    public void setState(int newState) {
        this.state = newState;
    }

    @Override
    public int getTotalItems() {
        return totalItems;
    }

    @Override
    public void setTotalItems(int items) {
        this.totalItems = listLen(globalList);
    }

    @Override
    public JapStuJFrame.JaNode getGlobalListHead() {
        return globalList;
    }

    @Override
    public void deleteNodeFromList(JapStuJFrame.JaNode node) {
        if (node == null) return;
        JaNode pre = globalList;
        while (pre.next != null && pre.next != node) {
            pre = pre.next;
        }
        if (pre.next == node) {
            pre.next = node.next;
        }
        totalItems = listLen(globalList);
        saveToFile();
    }

    @Override
    public Component getDialogParent() {
        return this;
    }

    private void switchJLPTLevel(String level) {
        if (jlptMode) {
            saveCurrentGroup();
            saveGroupProgress();
            saveCarryOver();
        }
        currentJLPTLevel = level;
        lastJLPTLevel = level;
        saveJLPTLevelPreference();
        if (btnJLPTLevelSwitch != null) btnJLPTLevelSwitch.setText("JLPT " + level);
        startJLPTMode();
    }

    @Override
    public void updateStatsLabel() {
        if (jlptMode) {
            statsLabel.setText("");
        } else {
            statsLabel.setText("当前在库考察点个数: " + totalItems + "  已掌握: " + masteredItems);
        }
    }

    @Override
    public void setStatsLabelVisible(boolean visible) {
        statsPanel.setVisible(visible);
    }

    @Override
    public void resetAllButtonVisibility() {
        setTestButtonsVisible(false, false, false);
        btnContinueTest.setVisible(false);
        btnAddToLocal.setVisible(false);
        btnJLPTLevelSwitch.setVisible(false);
        setEditButtonsVisible(false);
        btnConvertToGrammar.setVisible(false);
        setTypeButtonsVisible(false);
        inputField.setVisible(false);
        btnInputConfirm.setVisible(false);
        jlptTestStatePanel.setVisible(false);
    }

    private void setTestButtonsVisible(boolean showAnswer, boolean showYesNo, boolean showEditDelete) {
        btnShowAnswer.setVisible(showAnswer);
        btnYes.setVisible(showYesNo);
        btnNo.setVisible(showYesNo);
        btnEditAfterTest.setVisible(showEditDelete);
        btnDeleteAfterTest.setVisible(showEditDelete);
    }

    private void setTypeButtonsVisible(boolean visible) {
        btnTypeWord.setVisible(visible);
        btnTypeGram.setVisible(visible);
    }

    private JaNode initList() {
        JaNode head = new JaNode();
        head.next = null;
        return head;
    }

    private int listLen(JaNode head) {
        int c = 0;
        JaNode p = head.next;
        while (p != null) { c++; p = p.next; }
        return c;
    }

    private boolean isTested(int idx, int[] testedIdx, int count) {
        for (int i = 0; i < count; i++)
            if (testedIdx[i] == idx) return true;
        return false;
    }

    // ==================== 文件读写 ====================

    private void readFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String firstLine = br.readLine();
            JaNode tail = globalList;
            boolean headerParsed = false;
            if (firstLine != null) {
                String[] headParts = firstLine.split("\t");
                if (headParts.length == 2) {
                    try {
                        totalItems = Integer.parseInt(headParts[0].trim());
                        masteredItems = Integer.parseInt(headParts[1].trim());
                        headerParsed = true;
                    } catch (NumberFormatException ignored) {}
                }
                if (!headerParsed) {
                    JaNode node = parseNode(firstLine, false);
                    tail.next = node;
                    tail = node;
                }
            }
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                JaNode node = parseNode(line, false);
                tail.next = node;
                tail = node;
            }
            totalItems = listLen(globalList);
            if (!headerParsed) masteredItems = 0;
        } catch (Exception e) {
            totalItems = 0;
            masteredItems = 0;
        }
    }

    private JaNode parseNode(String line, boolean jlpt) {
        String[] sp = line.split("\t");
        JaNode node = new JaNode();
        node.japanese  = sp.length > 0 ? sp[0] : "";
        node.chinese   = sp.length > 1 ? sp[1] : "";
        node.type      = sp.length > 2 ? Integer.parseInt(sp[2]) : 1;
        node.examTimes = sp.length > 3 ? Integer.parseInt(sp[3]) : 0;
        node.trueTimes = sp.length > 4 ? Integer.parseInt(sp[4]) : 0;
        node.example   = sp.length > 5 ? sp[5] : "";
        node.exampleCh = sp.length > 6 ? sp[6] : "";
        if (jlpt && sp.length > 7) {
            node.masteryState = Integer.parseInt(sp[7].trim());
        } else {
            node.masteryState = 0;
        }
        node.jlptLevel = 0;
        node.wrongTimes = 0;
        return node;
    }

    @Override
    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            pw.println(totalItems + "\t" + masteredItems);
            JaNode p = globalList.next;
            while (p != null) {
                pw.println(p.japanese + "\t" + p.chinese + "\t" + p.type + "\t"
                        + p.examTimes + "\t" + p.trueTimes + "\t" + p.example + "\t" + p.exampleCh);
                p = p.next;
            }
        } catch (Exception ignored) {}
    }

    private int getJLPTLevelValue() {
        switch (currentJLPTLevel) {
            case "N5": return 1;
            case "N4": return 2;
            case "N3": return 3;
            case "N2": return 4;
            case "N1": return 5;
            default: return 1;
        }
    }

    private void loadJLPTList() {
        jlptList = initList();
        String levelFile = JLPT_DIR + currentJLPTLevel + ".txt";
        File f = new File(levelFile);
        if (!f.exists()) {
            print("JLPT " + currentJLPTLevel + " 词库文件未找到: " + levelFile);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(levelFile))) {
            String firstLine = br.readLine();
            JaNode tail = jlptList;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                JaNode node = parseNode(line, true);
                node.jlptLevel = getJLPTLevelValue();
                tail.next = node;
                tail = node;
            }
        } catch (Exception e) {
            print("读取 JLPT 词库失败: " + e.getMessage());
        }
        jlptTotalItems = listLen(jlptList);
        updateJLPTProgress();
    }

    private void saveJLPTToFile() {
        String levelFile = JLPT_DIR + currentJLPTLevel + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(levelFile))) {
            pw.println(jlptTotalItems + "\t" + jlptMasteredCount);
            JaNode p = jlptList.next;
            while (p != null) {
                pw.println(p.japanese + "\t" + p.chinese + "\t" + p.type + "\t"
                        + p.examTimes + "\t" + p.trueTimes + "\t" + p.example + "\t" + p.exampleCh
                        + "\t" + p.masteryState);
                p = p.next;
            }
        } catch (Exception ignored) {}
    }

    private void ensureJLPTWorkDir() {
        File workDir = new File(JLPT_DIR);
        if (workDir.exists()) return;
        workDir.mkdirs();
        File srcDir = new File(JLPT_SRC_DIR);
        if (!srcDir.exists()) {
            System.err.println("JLPT 源目录不存在: " + JLPT_SRC_DIR);
            return;
        }
        File[] files = srcDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return;
        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f));
                 PrintWriter pw = new PrintWriter(new FileWriter(new File(workDir, f.getName())))) {
                String line;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
            } catch (Exception e) {
                System.err.println("复制 JLPT 文件失败: " + f.getName());
            }
        }
        System.out.println("JLPT 词库已初始化到 " + JLPT_DIR);
    }

    private void ensureGroupJLPTWorkDir() {
        File workDir = new File(GROUP_JLPT_DIR);
        if (workDir.exists() && new File(GROUP_JLPT_DIR + "N5_Group1.txt").exists()) return;
        workDir.mkdirs();
        File srcDir = new File(GROUP_JLPT_SRC_DIR);
        if (!srcDir.exists()) {
            System.err.println("分组源目录不存在: " + GROUP_JLPT_SRC_DIR);
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
        System.out.println("分组词库已初始化到 " + GROUP_JLPT_DIR);
    }

    private void loadGroupProgress() {
        File progFile = new File(GROUP_JLPT_DIR + currentJLPTLevel + "_progress.txt");
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
        File outDir = new File(GROUP_JLPT_DIR);
        if (!outDir.exists()) outDir.mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(GROUP_JLPT_DIR + currentJLPTLevel + "_progress.txt"))) {
            pw.println(currentGroupIndex);
        } catch (Exception ignored) {}
    }

    private void countTotalGroups() {
        totalGroups = 0;
        while (new File(GROUP_JLPT_DIR + currentJLPTLevel + "_Group" + (totalGroups + 1) + ".txt").exists()) {
            totalGroups++;
        }
        if (totalGroups == 0) totalGroups = 1;
    }

    private void loadCurrentGroup() {
        jlptList = initList();
        String groupFile = GROUP_JLPT_DIR + currentJLPTLevel + "_Group" + currentGroupIndex + ".txt";
        JaNode tail = jlptList;
        try (BufferedReader br = new BufferedReader(new FileReader(groupFile))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                JaNode node = parseNode(line, true);
                node.jlptLevel = getJLPTLevelValue();
                tail.next = node;
                tail = node;
            }
        } catch (Exception e) {
            print("读取分组文件失败: " + e.getMessage());
        }
        loadCarryOver(tail);
        jlptTotalItems = listLen(jlptList);
        groupMastered = (jlptTotalItems > 0 && countNonMastered() == 0);
        updateJLPTProgress();
    }

    private void loadCarryOver(JaNode tail) {
        File coFile = new File(GROUP_JLPT_DIR + currentJLPTLevel + "_carryover.txt");
        if (!coFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(coFile))) {
            String countLine = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                JaNode node = parseNode(line, true);
                node.jlptLevel = getJLPTLevelValue();
                tail.next = node;
                tail = node;
            }
        } catch (Exception ignored) {}
    }

    private void saveCarryOver() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(GROUP_JLPT_DIR + currentJLPTLevel + "_carryover.txt"))) {
            pw.println(allWrongOver3ThisSession);
        } catch (Exception ignored) {}
    }

    private void saveCurrentGroup() {
        String groupFile = GROUP_JLPT_DIR + currentJLPTLevel + "_Group" + currentGroupIndex + ".txt";
        int len = listLen(jlptList);
        try (PrintWriter pw = new PrintWriter(new FileWriter(groupFile))) {
            pw.println(len + "\t0");
            JaNode p = jlptList.next;
            while (p != null) {
                pw.println(p.japanese + "\t" + p.chinese + "\t" + p.type + "\t"
                        + p.examTimes + "\t" + p.trueTimes + "\t" + p.example + "\t" + p.exampleCh
                        + "\t" + p.masteryState);
                p = p.next;
            }
        } catch (Exception ignored) {}
    }

    private int countCurrentGroupGroups() {
        File dir = new File(GROUP_JLPT_DIR);
        int c = 0;
        while (new File(dir, currentJLPTLevel + "_Group" + (c + 1) + ".txt").exists()) c++;
        return c;
    }

    private void loadJLPTLevelPreference() {
        String prefFile = JLPT_DIR + "last_level.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(prefFile))) {
            String level = br.readLine();
            if (level != null && level.matches("N[1-5]")) {
                currentJLPTLevel = level.trim();
                lastJLPTLevel = level.trim();
            }
        } catch (Exception ignored) {}
    }

    private void saveJLPTLevelPreference() {
        String prefFile = JLPT_DIR + "last_level.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(prefFile))) {
            pw.println(lastJLPTLevel);
        } catch (Exception ignored) {}
    }

    // ==================== 启动状态 ====================

    private void showStartupStatus() {
        switchToTextArea();
        printBold("===== 系统启动状态 =====");
        // 本地词库状态
        File localFile = new File(FILE_PATH);
        if (localFile.exists()) {
            print("本地词库 D:\\JaStu.txt: 存在 | 共 " + totalItems + " 项 | 已掌握 " + masteredItems);
            if (totalItems == 0 && globalList.next == null) {
                print(" 警告：本地词库为空，请添加考察点。");
            }
        } else {
            print("本地词库 D:\\JaStu.txt: 未找到（将在保存时创建）");
        }
        print("");
        // JLPT 词库状态
        printBold("----- JLPT 词书状态（D:\\JSS\\JLPT\\SortedJLPT） -----");
        File jlptWorkDir = new File(JLPT_DIR);
        if (!jlptWorkDir.exists()) {
            print("JLPT 工作目录不存在，尝试初始化...");
            ensureJLPTWorkDir();
        }
        if (jlptWorkDir.exists()) {
            int totalJpWords = 0;
            String[] levels = {"N5", "N4", "N3", "N2", "N1"};
            for (String lv : levels) {
                File lf = new File(JLPT_DIR + lv + ".txt");
                if (lf.exists()) {
                    int wordCount = 0;
                    int masteredCount = 0;
                    int knownCount = 0;
                    int strangeCount = 0;
                    try (BufferedReader br = new BufferedReader(new FileReader(lf))) {
                        String header = br.readLine();
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
                    totalJpWords += wordCount;
                    if (masteredCount + knownCount + strangeCount > 0) {
                        print(lv + "：共 " + wordCount + " 词 | 掌握 " + masteredCount
                                + "  了解 " + knownCount + "  陌生 " + strangeCount);
                    } else {
                        print(lv + "：共 " + wordCount + " 词（尚未开始学习）");
                    }
                } else {
                    print(lv + "：未找到");
                }
            }
            print("JLPT 词书总计：共 " + totalJpWords + " 词 | 上次使用等级：" + lastJLPTLevel);
            // 分组信息
            loadGroupProgress();
            print("分组状态：" + currentJLPTLevel + " 共 " + totalGroups + " 组，当前第 " + currentGroupIndex + " 组");
        } else {
            print("JLPT 词书目录初始化失败！");
        }
        print("");
        printBold("===== 系统就绪 =====");
        print("点击「JLPT词书」按钮开始 JLPT 学习，或使用本地词库功能。");
    }

    // ==================== 添加模式 ====================

    private void addMode() {
        clearAll();
        switchToTextArea();
        updateStatsLabel();
        setStatsLabelVisible(false);
        state = 1;
        tmpStep = 0;
        resetAllButtonVisibility();
        inputField.setVisible(true);
        btnInputConfirm.setVisible(true);
        print("===== 添加考察点 =====");
        print("请在上方输入框中输入日文/语法表达，输入完成后点击 确认输入 按钮");
        print("输入示例：場合");
    }

    private void editMode() {
        clearAll();
        switchToTextArea();
        updateStatsLabel();
        setStatsLabelVisible(false);
        state = 4;
        resetAllButtonVisibility();
        inputField.setVisible(true);
        btnInputConfirm.setVisible(true);
        if (globalList.next == null) {
            print("暂无数据");
            state = 0;
            return;
        }
        print("===== 查找考察点 =====");
        print("请输入日文或中文进行查找，输入完成后点击 确认输入 按钮");
    }

    // ==================== 本地测试模式 ====================

    private void localTestMode() {
        jlptMode = false;
        showAllTargetLocal = true;
        clearAll();
        switchToTextArea();
        updateStatsLabel();
        setStatsLabelVisible(false);
        setJLPTProgressVisible(false);
        state = 2;
        resetAllButtonVisibility();
        setTestButtonsVisible(true, false, false);
        btnAddToLocal.setVisible(false);

        int len = listLen(globalList);
        if (len == 0) {
            print("本地词库无数据，无法进行测试");
            state = 0;
            return;
        }
        if (testedCount >= len) {
            testedCount = 0;
            testedIndex = new int[1000];
        }
        int idx;
        do idx = random.nextInt(len) + 1; while (isTested(idx, testedIndex, testedCount));
        testedIndex[testedCount++] = idx;

        JaNode p = globalList;
        for (int i = 0; i < idx; i++) p = p.next;
        currentTest = p;
        currentTest.wrongTimes = 0;

        print("===== 测试本地词库 =====");
        print(p.type == 1 ? "【单词】" : "【语法】");
        print(p.japanese);
        print("\n请点击上方 显示答案 按钮查看答案");
    }

    // ==================== JLPT 模式 ====================

    private void startJLPTMode() {
        jlptMode = true;
        showAllTargetLocal = false;
        clearAll();
        switchToTextArea();
        loadGroupProgress();
        loadCurrentGroup();
        updateStatsLabel();
        setStatsLabelVisible(true);
        setJLPTProgressVisible(true);
        btnJLPTLevelSwitch.setText("JLPT " + currentJLPTLevel);
        btnJLPTLevelSwitch.setVisible(true);

        print("===== JLPT " + currentJLPTLevel + " 分组学习 =====");
        print("共 " + totalGroups + " 组，当前第 " + currentGroupIndex + " 组，共 " + jlptTotalItems + " 词");
        if (groupMastered) {
            print("本组已全部掌握，点击「继续测试」进入下一组");
        } else {
            print("点击「继续测试」开始测试");
        }
        print("点击「查看全部」浏览当前组词汇");

        state = 0;
        resetAllButtonVisibility();
        btnContinueTest.setVisible(true);
        btnJLPTLevelSwitch.setVisible(true);
        setStatsLabelVisible(true);
        updateStatsLabel();
    }

    private void jlptTestMode() {
        clearAll();
        switchToTextArea();
        state = 2;
        resetAllButtonVisibility();
        setTestButtonsVisible(true, false, false);
        btnAddToLocal.setVisible(false);
        btnJLPTLevelSwitch.setVisible(true);

        int totalNonMastered = countNonMastered();
        if (totalNonMastered == 0) {
            groupMastered = true;
            saveCurrentGroup();
            print("恭喜！当前组 JLPT " + currentJLPTLevel + " Group" + currentGroupIndex + " 全部掌握！");
            if (currentGroupIndex < totalGroups) {
                print("点击「继续测试」进入下一组（Group" + (currentGroupIndex + 1) + "）");
            } else {
                print("恭喜！" + currentJLPTLevel + " 所有分组已全部掌握！");
            }
            updateJLPTProgress();
            state = STATE_TEST_RESULT;
            btnContinueTest.setVisible(true);
            btnJLPTLevelSwitch.setVisible(true);
            return;
        }

        JaNode selected = weightedRandomSelect();
        if (selected == null) {
            print("无可测试的词汇");
            state = 0;
            return;
        }

        currentTest = selected;
        recentTestedWords.add(selected);
        if (recentTestedWords.size() > RECENT_WINDOW * 2) {
            recentTestedWords = recentTestedWords.subList(
                    recentTestedWords.size() - RECENT_WINDOW, recentTestedWords.size());
        }

        String stateStr;
        switch (currentTest.masteryState) {
            case 0: stateStr = "陌生"; break;
            case 1: stateStr = "了解"; break;
            default: stateStr = "掌握"; break;
        }
        jlptTestStateLabel.setText("状态：" + stateStr);
        jlptTestWordLabel.setText("当前单词：" + currentTest.japanese);
        jlptTestStatePanel.setVisible(true);

        print("===== JLPT " + currentJLPTLevel + " Group" + currentGroupIndex + " 测试 =====");
        print("【单词】" + currentTest.japanese);
        print("\n请点击上方 显示答案 按钮查看答案");
    }

    private int countNonMastered() {
        int c = 0;
        JaNode p = jlptList.next;
        while (p != null) {
            if (p.masteryState < 2) c++;
            p = p.next;
        }
        return c;
    }

    private JaNode weightedRandomSelect() {
        java.util.List<JaNode> candidates = new ArrayList<>();
        java.util.List<Double> weights = new ArrayList<>();

        JaNode p = jlptList.next;
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

        double r = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (r <= cumulative) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    private double getWeight(JaNode node, int index) {
        // 基础权重：陌生=5, 了解=2, 掌握=0
        double baseWeight;
        switch (node.masteryState) {
            case 0: baseWeight = 5.0; break;
            case 1: baseWeight = 2.0; break;
            default: baseWeight = 0.0; break;
        }

        // 答错次数惩罚权重
        baseWeight += node.wrongTimes * 3.0;

        // 近期被抽到词汇的权重提升（距离越近权重越高）
        int recencyIndex = recentTestedWords.indexOf(node);
        if (recencyIndex >= 0) {
            int distFromEnd = recentTestedWords.size() - recencyIndex;
            if (distFromEnd <= RECENT_WINDOW) {
                baseWeight += (RECENT_WINDOW - distFromEnd + 1) * 1.5;
            }
        }

        return baseWeight;
    }

    // ==================== 图片识别模式 ====================

    private void enterImageRecognitionMode() {
        clearAll();
        state = STATE_IMAGE_RECOGNITION;
        resetAllButtonVisibility();
        setStatsLabelVisible(false);
        setJLPTProgressVisible(false);

        centerWrapPanel.removeAll();
        centerWrapPanel.add(new JapImageRecognition(this), BorderLayout.CENTER);
        centerWrapPanel.revalidate();
        centerWrapPanel.repaint();
    }

    // ==================== 测试结果与掌握逻辑 ====================

    private void handleTestResult(JaNode node, boolean correct) {
        if (correct) {
            node.trueTimes++;
            node.wrongTimes = 0;
        } else {
            node.wrongTimes++;
        }
        node.examTimes++;

        double correctRate = node.examTimes == 0 ? 0
                : (double) node.trueTimes / node.examTimes * 100;

        if (jlptMode) {
            handleJLPTTestResult(node, correct);
        } else {
            handleLocalTestResult(node, correct, correctRate);
        }
    }

    private void handleLocalTestResult(JaNode node, boolean correct, double correctRate) {
        int wrong = node.examTimes - node.trueTimes;
        int requiredCorrect = 5 + wrong / 2;
        boolean mastered = node.trueTimes >= requiredCorrect;

        print("\n=====================================");
        print("测试结果及统计数据");
        print("=====================================");
        print("累计考核：" + node.examTimes + " 次");
        print("正确次数：" + node.trueTimes + " 次");
        print("正确率：" + String.format("%.1f", correctRate) + " %");

        print("\n===== 考察点信息 =====");
        if (node.type == 2) {
            embedGrammarInfo(node);
        } else {
            print("单词：" + node.japanese);
            print("释义：" + node.chinese);
        }
        print("=====================");

        if (mastered) {
            print("\n已达到掌握要求，自动从词库移除！");
            masteredItems++;
            editAndDel.deleteNode(currentTest);
            currentTest = null;
            totalItems = listLen(globalList);
            updateStatsLabel();
        } else {
            int remaining = requiredCorrect - node.trueTimes;
            print("\n还需答对 " + remaining + " 次即可掌握，继续加油！");
        }
        print("【掌握规则】起始需答对5次，每答错2次则需多答对1次");

        saveToFile();
        state = STATE_TEST_RESULT;
        setTestButtonsVisible(false, false, true);
        btnContinueTest.setVisible(true);
        btnAddToLocal.setVisible(false);
        setStatsLabelVisible(true);
        print("\n可点击上方的【继续测试】【修改考察点】【删除考察点】");
    }

    private void handleJLPTTestResult(JaNode node, boolean correct) {
        print("\n=====================================");
        print("JLPT " + currentJLPTLevel + " Group" + currentGroupIndex + " 测试结果");
        print("=====================================");
        print("累计考核：" + node.examTimes + " 次");
        print("正确次数：" + node.trueTimes + " 次");

        if (correct) {
            switch (node.masteryState) {
                case 0:
                    node.masteryState = 1;
                    print("状态提升：陌生 -> 了解");
                    break;
                case 1:
                    if (node.trueTimes >= getJLPTRequiredCorrect(node)) {
                        node.masteryState = 2;
                        print("状态提升：了解 -> 掌握！");
                    } else {
                        int need = getJLPTRequiredCorrect(node) - node.trueTimes;
                        print("还需答对 " + need + " 次即可达到掌握");
                    }
                    break;
                default:
                    print("当前状态：掌握");
                    break;
            }
            node.wrongTimes = 0;
        } else {
            print("回答错误！");
            int wrongTimes = node.examTimes - node.trueTimes;
            if (wrongTimes >= 2 && wrongTimes % 2 == 0) {
                print("已累计答错 " + wrongTimes + " 次，需要额外多答对 " + (wrongTimes / 2) + " 次");
            }
            // 答错超过3次，追加至下一分组
            if (node.examTimes - node.trueTimes > 3) {
                if (currentGroupIndex < totalGroups) {
                    appendToNextGroup(node);
                    print("\n累计答错超过3次，已自动追加至下一分组复习！");
                } else {
                    print("\n累计答错超过3次，但已是最后一组，无法继续追加。");
                }
            }
        }

        print("\n当前状态：" + getMasteryLabel(node.masteryState));
        print("\n===== 词汇信息 =====");
        print("单词：" + node.japanese);
        print("释义：" + node.chinese);

        jlptTestStateLabel.setText("状态：" + getMasteryLabel(node.masteryState));
        jlptTestWordLabel.setText("当前单词：" + node.japanese);
        jlptTestStatePanel.setVisible(true);

        saveCurrentGroup();
        updateJLPTProgress();

        state = STATE_TEST_RESULT;
        setTestButtonsVisible(false, false, true);
        btnContinueTest.setVisible(true);
        btnAddToLocal.setVisible(true);
        btnJLPTLevelSwitch.setVisible(true);
        setStatsLabelVisible(true);
        print("\n可点击上方【继续测试】【添加到本地】【修改考察点】【删除考察点】");
    }

    private void appendToNextGroup(JaNode node) {
        int nextGroup = currentGroupIndex + 1;
        String coFile = GROUP_JLPT_DIR + currentJLPTLevel + "_carryover.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(coFile, true))) {
            pw.println(node.japanese + "\t" + node.chinese + "\t" + node.type + "\t"
                    + "0\t0\t" + node.example + "\t" + node.exampleCh + "\t0");
        } catch (Exception ignored) {}
        allWrongOver3ThisSession++;
        saveCarryOver();
    }

    private int getJLPTRequiredCorrect(JaNode node) {
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

    // ==================== 添加到本地词库 ====================

    private void addCurrentToLocal() {
        if (currentTest == null) return;
        int cfm = JOptionPane.showConfirmDialog(this,
                "将「" + currentTest.japanese + "」添加到本地词库 D:\\JaStu.txt？",
                "添加到本地", JOptionPane.YES_NO_OPTION);
        if (cfm != JOptionPane.YES_OPTION) return;

        JaNode newNode = new JaNode();
        newNode.japanese = currentTest.japanese;
        newNode.chinese = currentTest.chinese;
        newNode.type = 1;
        newNode.examTimes = 0;
        newNode.trueTimes = 0;
        newNode.example = "";
        newNode.exampleCh = "";
        newNode.masteryState = 0;
        newNode.jlptLevel = 0;

        JaNode tail = globalList;
        while (tail.next != null) tail = tail.next;
        tail.next = newNode;
        totalItems++;
        saveToFile();
        updateStatsLabel();
        print("\n已添加到本地词库！");
    }

    // ==================== ActionListener ====================

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == btnImageReco) {
            enterImageRecognitionMode();
        }
        else if (obj == btnShowAll) {
            if (showAllTargetLocal) {
                showAllTargetLocal = false;
                showAllAsCards();
            } else {
                showAllTargetLocal = true;
                if (jlptMode && (jlptList == null || listLen(jlptList) == 0)) {
                    loadCurrentGroup();
                }
                showJLPTAsCards();
            }
        }
        else if (obj == btnAddWord) addMode();
        else if (obj == btnEditWord) editMode();
        else if (obj == btnLocalTest) {
            jlptMode = false;
            setJLPTProgressVisible(false);
            localTestMode();
        }
        else if (obj == btnContinueTest) {
            if (jlptMode) {
                testedCount = 0;
                testedIndex = new int[1000];
                recentTestedWords.clear();
                if (groupMastered && currentGroupIndex < totalGroups) {
                    currentGroupIndex++;
                    saveGroupProgress();
                    loadCurrentGroup();
                    print("===== 进入下一组：JLPT " + currentJLPTLevel + " Group" + currentGroupIndex + " =====");
                    print("共 " + jlptTotalItems + " 词，点击「继续测试」开始学习");
                    state = 0;
                    resetAllButtonVisibility();
                    btnContinueTest.setVisible(true);
                    btnJLPTLevelSwitch.setVisible(true);
                    setJLPTProgressVisible(true);
                    updateStatsLabel();
                } else {
                    jlptTestMode();
                }
            } else {
                testedCount = 0;
                testedIndex = new int[1000];
                localTestMode();
            }
        }
        else if (obj == btnExit) {
            if (jlptMode) {
                saveCurrentGroup();
                saveGroupProgress();
                saveCarryOver();
            }
            saveToFile();
            System.exit(0);
        }
        else if (obj == btnShowAnswer) {
            if (state != 2 || currentTest == null) return;
            print("\n----- 参考答案 -----");
            if (currentTest.type == 2 && !jlptMode) {
                embedGrammarInfo(currentTest);
            } else {
                print("释义：" + currentTest.chinese);
            }
            print("\n请点击上方 记得 或 不记得 按钮进行选择");
            state = 3;
            setTestButtonsVisible(false, true, false);
        }
        else if (obj == btnYes || obj == btnNo) {
            if (state != 3 || currentTest == null) return;
            boolean ok = (obj == btnYes);
            handleTestResult(currentTest, ok);

            SwingUtilities.invokeLater(() -> {
                JScrollBar vsb = textScrollPane.getVerticalScrollBar();
                vsb.setValue(vsb.getMaximum());
            });
        }
        else if (obj == btnEditAfterTest) {
            if ((state == STATE_TEST_RESULT) && currentTest != null) {
                editAndDel.enterEditMode(currentTest);
            }
        }
        else if (obj == btnDeleteAfterTest) {
            if (state == STATE_TEST_RESULT && currentTest != null) {
                int cfm = JOptionPane.showConfirmDialog(this,
                        "确定删除该考察点？", "删除确认", JOptionPane.YES_NO_OPTION);
                if (cfm == JOptionPane.YES_OPTION) {
                    if (jlptMode) {
                        JaNode pre = jlptList;
                        while (pre.next != null && pre.next != currentTest) pre = pre.next;
                        if (pre.next == currentTest) pre.next = currentTest.next;
                        saveCurrentGroup();
                        updateJLPTProgress();
                    } else {
                        editAndDel.deleteNode(currentTest);
                    }
                    currentTest = null;
                    clearAll();
                    print("删除成功！");
                    state = 0;
                    totalItems = listLen(globalList);
                    updateStatsLabel();
                    resetAllButtonVisibility();
                    setStatsLabelVisible(true);
                }
            }
        }
        else if (obj == btnAddToLocal) {
            addCurrentToLocal();
        }
        else if (obj == btnEditJpField) {
            editAndDel.editField(1);
        }
        else if (obj == btnEditCnField) {
            editAndDel.editField(2);
        }
        else if (obj == btnEditExField) {
            editAndDel.editField(4);
        }
        else if (obj == btnEditExcField) {
            editAndDel.editField(5);
        }
        else if (obj == btnFinishEdit) {
            editAndDel.finishEdit();
        }
        else if (obj == btnDeleteWord) {
            editAndDel.deleteEditTarget();
        }
        else if (obj == btnConvertToGrammar) {
            editAndDel.convertToGrammar();
        }
        else if (obj == btnTypeWord) {
            if (state != 1 || tmpStep != 2) return;
            JaNode node = new JaNode();
            node.japanese  = tmpJp;
            node.chinese   = tmpCn;
            node.type      = 1;
            node.examTimes = 0;
            node.trueTimes = 0;
            node.masteryState = 0;
            node.jlptLevel = 0;
            node.next      = globalList.next;
            globalList.next = node;
            totalItems++;
            updateStatsLabel();
            saveToFile();
            printBold("\n-----单词添加成功-----");
            printBold("类型：单词");
            printBold("日文：" + node.japanese);
            printBold("释义：" + node.chinese);
            state = 0;
            resetAllButtonVisibility();
            setStatsLabelVisible(true);
        }
        else if (obj == btnTypeGram) {
            if (state != 1 || tmpStep != 2) return;
            print("\n请输入日文例句，完成后点击 确认输入");
            print("例句格式示例：毎日（まいにち）の　運動（うんどう）のおかげで、　体（からだ）が　健康（けんこう）になりました。");
            print("【格式规范】在需要注音的汉字前加一个全角空格（　），例：日本　文化（ぶんか）");
            tmpStep = 3;
            setTypeButtonsVisible(false);
        }
        else if (e.getActionCommand().equals("确认输入")) {
            String input = inputField.getText().trim();
            inputField.setText("");
            if (input.isEmpty()) {
                print("输入不能为空，请重新输入！");
                return;
            }

            if (state == 1) {
                if (tmpStep == 0) {
                    tmpJp   = input;
                    tmpStep = 1;
                    printBold("成功输入日文：" + input);
                    print("\n请输入中文释义：");
                    print("例如：場合（ばあい）：场合");
                } else if (tmpStep == 1) {
                    tmpCn   = input;
                    tmpStep = 2;
                    printBold("成功输入中文释义：" + input);
                    print("\n请点击上方按钮选择类型：单词 或 语法");
                    setTypeButtonsVisible(true);
                } else if (tmpStep == 3) {
                    tmpEx   = input;
                    tmpStep = 4;
                    printBold("成功输入例句：" + input);
                    print("\n请输入例句翻译：");
                } else if (tmpStep == 4) {
                    tmpExc = input;
                    JaNode node = new JaNode();
                    node.japanese  = tmpJp;
                    node.chinese   = tmpCn;
                    node.type      = 2;
                    node.example   = tmpEx;
                    node.exampleCh = tmpExc;
                    node.examTimes = 0;
                    node.trueTimes = 0;
                    node.masteryState = 0;
                    node.jlptLevel = 0;
                    node.next      = globalList.next;
                    globalList.next = node;
                    totalItems++;
                    updateStatsLabel();
                    saveToFile();
                    printBold("\n语法添加成功！");
                    printBold("类型：语法");
                    printBold("日文：" + node.japanese);
                    printBold("释义：" + node.chinese);
                    printBold("例句：" + node.example);
                    printBold("例句译：" + node.exampleCh);
                    state = 0;
                    resetAllButtonVisibility();
                    setStatsLabelVisible(true);
                }
            }
            else if (state == 4) {
                clearAll();
                JaNode p    = globalList.next;
                int    found = 0;
                JaNode firstMatch = null;
                print("===== 查找结果 =====");
                while (p != null) {
                    if (p.japanese.contains(input) || p.chinese.contains(input)) {
                        found++;
                        if (firstMatch == null) firstMatch = p;
                        print("\n--- 结果 " + found + " ---");
                        print("类型：" + (p.type == 1 ? "单词" : "语法"));
                        print("日文：" + p.japanese);
                        print("释义：" + p.chinese);
                        if (p.type == 2) {
                            embedGrammarInfo(p);
                        }
                        print("考核：" + p.examTimes + " 次  正确：" + p.trueTimes + " 次");
                    }
                    p = p.next;
                }
                if (found == 0) print("未找到相关内容");
                else if (found == 1) {
                    print("\n已选中该内容，请点击上方对应按钮修改");
                    state = 7;
                    resetAllButtonVisibility();
                    showEditButtonsForType(firstMatch.type);
                    updateStatsLabel();
                    setStatsLabelVisible(false);
                    editAndDel.enterEditMode(firstMatch);
                } else {
                    print("\n找到多个结果，请输入更精确的内容重新查找");
                    inputField.setVisible(false);
                    btnInputConfirm.setVisible(false);
                    updateStatsLabel();
                    setStatsLabelVisible(false);
                }
            }
            else if (editAndDel.isEditing()) {
                editAndDel.handleConfirmInput(input);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JapStuJFrame::new);
    }
}
