package LanguageStudySystem.JavaJapStuSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 日语图片识别面板（DeepSeek 网页版）
 * 展示识别操作界面，处理 DeepSeek 回复的解析与预览，
 * 并提供修改、删除及批量添加单词/语法的功能。
 * 所有需要主窗口参与的操作通过 RecognitionCallback 回调通知。
 */
public class JapImageRecognition extends JPanel {

    // ==================== 回调接口 ====================
    public interface RecognitionCallback {
        /** 用户点击“返回主界面”时调用 */
        void onCancel();
        /** 用户点击“全部添加到词库”时调用，传入待添加的考察点列表 */
        void onAddWords(List<JapStuJFrame.JaNode> words);
    }

    // ==================== 常量 ====================
    private static final String DEEPSEEK_URL = "https://chat.deepseek.com/";
    // 与原始版本保持完全一致的指令
    private static final String INSTRUCTION =
            "请识别图片中的日语内容，区分单词或语法。\n" +
                    "对每个项目输出一行，每行包含5个字段，用制表符分隔，不要输出任何额外说明。\n\n" +
                    "字段顺序：日文表达\t中文释义\t类型（单词 或 语法）\t例句\t例句翻译\n\n" +
                    "格式规则：\n" +
                    "【单词】\n" +
                    "  - 如果是汉字词（例如「適切」）：日文表达只写汉字（不带假名）；中文释义写「单词本身（假名）+ 空格 + 中文意思」。\n" +
                    "    示例：適切\t適切（てきせつ）：合适，恰当\t单词\t\t\n" +
                    "  - 如果是非汉字词（平假名、片假名词）：日文表达写原词（不带额外假名）；中文释义只写中文意思。\n" +
                    "    示例：たまらない\t难以忍受\t单词\t\t\n" +
                    "【语法】\n" +
                    "  - 日文表达写带假名注音的语法条目；中文释义写中文意思；必须提供例句和例句翻译。\n" +
                    "    示例：場合（ばあい）\t场合\t语法\tこの場合（ばあい）は特別（とくべつ）です。\t在这个场合是特别的。\n\n" +
                    "请严格按照上述格式输出。";

    // ==================== 数据与回调 ====================
    private final RecognitionCallback callback;
    private final List<JapStuJFrame.JaNode> recognizedWords = new ArrayList<>();

    // ==================== UI 组件 ====================
    private JTextArea resultInputArea;
    private JButton btnParseResult;
    private JButton btnCancelRecognition;
    private JButton btnAddAllRecognized;
    private JPanel recognitionResultPanel;

    // ==================== 构造方法 ====================
    public JapImageRecognition(RecognitionCallback callback) {
        this.callback = callback;
        buildUI();
        // 自动打开浏览器并复制指令
        openDeepSeekInBrowser();
        copyInstructionToClipboard();
        // 显示操作提示
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "浏览器已打开 DeepSeek 聊天页面。\n\n" +
                            "操作步骤：\n" +
                            "1. 将图片上传到 DeepSeek 对话框；\n" +
                            "2. 在输入框中粘贴（Ctrl+V）已复制的识别指令；\n" +
                            "3. 发送后复制 DeepSeek 回复的全部内容（Ctrl+C）；\n" +
                            "4. 回到本窗口，在左侧文本框中粘贴回复，点击 [解析并预览]；\n" +
                            "5. 检查右侧卡片中的内容，可修改或删除；\n" +
                            "6. 最后点击 [全部添加到词库] 完成导入。",
                    "操作说明", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // ==================== 界面构建 ====================
    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部：使用说明
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new TitledBorder("使用说明"));
        JTextArea infoArea = new JTextArea(
                "1. 浏览器已自动打开 DeepSeek，识别指令已复制到剪贴板。\n" +
                        "2. 上传图片后，在 DeepSeek 输入框粘贴指令并发送。\n" +
                        "3. 复制 DeepSeek 的完整回复，粘贴到左侧文本框。\n" +
                        "4. 点击“解析并预览”，在右侧检查识别结果。\n" +
                        "5. 确认无误后点击“全部添加到词库”。");
        infoArea.setEditable(false);
        infoArea.setBackground(null);
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        infoPanel.add(infoArea, BorderLayout.CENTER);

        // 中部：左右分栏
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 左侧：粘贴区
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("粘贴 DeepSeek 回复"));
        resultInputArea = new JTextArea();
        resultInputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultInputArea.setLineWrap(true);
        JScrollPane leftScroll = new JScrollPane(resultInputArea);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        btnParseResult = new JButton("解析并预览");
        btnParseResult.addActionListener(this::parseAndShowResult);
        JPanel btnPanelLeft = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanelLeft.add(btnParseResult);
        leftPanel.add(leftScroll, BorderLayout.CENTER);
        leftPanel.add(btnPanelLeft, BorderLayout.SOUTH);

        // 右侧：预览卡片区
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(new TitledBorder("识别结果预览（可修改/删除）"));
        recognitionResultPanel = new JPanel();
        recognitionResultPanel.setLayout(new WrapLayout(FlowLayout.LEADING, 10, 10));
        JScrollPane recognitionScrollPane = new JScrollPane(recognitionResultPanel);
        recognitionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        recognitionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightPanel.add(recognitionScrollPane, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        // 底部：操作按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnCancelRecognition = new JButton("返回主界面");
        btnCancelRecognition.addActionListener(e -> {
            recognizedWords.clear();
            callback.onCancel();
        });
        btnAddAllRecognized = new JButton("全部添加到词库");
        btnAddAllRecognized.setVisible(false);
        btnAddAllRecognized.addActionListener(e -> {
            if (!recognizedWords.isEmpty()) {
                callback.onAddWords(new ArrayList<>(recognizedWords));
                recognizedWords.clear();
            } else {
                JOptionPane.showMessageDialog(this, "没有可添加的考察点");
            }
        });
        bottomPanel.add(btnAddAllRecognized);
        bottomPanel.add(btnCancelRecognition);

        add(infoPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ==================== 解析与预览 ====================
    private void parseAndShowResult(ActionEvent e) {
        String text = resultInputArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先粘贴 DeepSeek 的回复内容。");
            return;
        }

        recognizedWords.clear();
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 制表符格式（新指令的标准输出）
            if (line.contains("\t")) {
                String[] parts = line.split("\t", -1);
                if (parts.length >= 3) {
                    JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
                    node.japanese = parts[0].trim();
                    node.chinese = parts[1].trim();
                    String typeStr = parts[2].trim();
                    node.type = typeStr.equals("语法") ? 2 : 1;
                    node.example = parts.length > 3 ? parts[3].trim() : "";
                    node.exampleCh = parts.length > 4 ? parts[4].trim() : "";
                    node.examTimes = 0;
                    node.trueTimes = 0;
                    recognizedWords.add(node);
                    continue;
                }
            }

            // 兼容旧版冒号格式（仅限单词）
            if (line.contains("：") || line.contains(":")) {
                String[] pair = line.split("：", 2);
                if (pair.length < 2) pair = line.split(":", 2);
                if (pair.length >= 2) {
                    JapStuJFrame.JaNode node = new JapStuJFrame.JaNode();
                    node.japanese = pair[0].trim();
                    node.chinese = pair[1].trim();
                    node.type = 1;
                    node.example = "";
                    node.exampleCh = "";
                    node.examTimes = 0;
                    node.trueTimes = 0;
                    recognizedWords.add(node);
                }
            }
        }

        showRecognitionResults();
    }

    private void showRecognitionResults() {
        recognitionResultPanel.removeAll();
        if (recognizedWords.isEmpty()) {
            recognitionResultPanel.add(new JLabel("未能解析出有效内容，请检查粘贴的格式。"));
            btnAddAllRecognized.setVisible(false);
        } else {
            for (JapStuJFrame.JaNode node : recognizedWords) {
                recognitionResultPanel.add(createRecognitionCard(node));
            }
            btnAddAllRecognized.setVisible(true);
        }
        recognitionResultPanel.revalidate();
        recognitionResultPanel.repaint();
    }

    private JPanel createRecognitionCard(JapStuJFrame.JaNode node) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 120));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        String typeStr = node.type == 1 ? "【单词】" : "【语法】";
        JLabel titleLabel = new JLabel(typeStr + node.japanese);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        JLabel cnLabel = new JLabel("释义：" + node.chinese);
        cnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(cnLabel);

        if (node.type == 2 && !node.example.isEmpty()) {
            JTextArea exLabel = new JTextArea("例句：" + node.example);
            exLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            exLabel.setLineWrap(true);
            exLabel.setEditable(false);
            exLabel.setBackground(Color.WHITE);
            infoPanel.add(Box.createVerticalStrut(4));
            infoPanel.add(exLabel);
        }

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(5, 5, 5, 10));

        JButton editBtn = new JButton("修改");
        JButton delBtn = new JButton("删除");

        editBtn.addActionListener(e -> {
            String newJp = JOptionPane.showInputDialog(this, "修改日文：", node.japanese);
            if (newJp != null && !newJp.trim().isEmpty()) node.japanese = newJp.trim();
            String newCn = JOptionPane.showInputDialog(this, "修改释义：", node.chinese);
            if (newCn != null) node.chinese = newCn.trim();
            if (node.type == 2) {
                String newEx = JOptionPane.showInputDialog(this, "修改例句：", node.example);
                if (newEx != null) node.example = newEx.trim();
                String newExc = JOptionPane.showInputDialog(this, "修改例句翻译：", node.exampleCh);
                if (newExc != null) node.exampleCh = newExc.trim();
            }
            showRecognitionResults();
        });

        delBtn.addActionListener(e -> {
            recognizedWords.remove(node);
            showRecognitionResults();
        });

        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    // ==================== 工具方法 ====================
    private void openDeepSeekInBrowser() {
        try {
            Desktop.getDesktop().browse(URI.create(DEEPSEEK_URL));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "无法自动打开浏览器，请手动访问 " + DEEPSEEK_URL);
        }
    }

    private void copyInstructionToClipboard() {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(INSTRUCTION), null);
    }

    // ==================== 自定义流式布局 ====================
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
                int targetW = target.getWidth() <= 0 ? Integer.MAX_VALUE : target.getWidth();
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetW - insets.left - insets.right - hgap * 2;
                int rowW = 0, rowH = 0, totalW = 0, totalH = 0;
                for (Component c : target.getComponents()) {
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
}
