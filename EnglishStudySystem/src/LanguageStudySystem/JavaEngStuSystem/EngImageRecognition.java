package LanguageStudySystem.JavaEngStuSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 图片识别面板 — AI Agent API 自动识别 + 拖入图片支持
 */
public class EngImageRecognition extends JPanel {

    public interface ImageRecognitionCallback {
        void onCancel();
        void onAddWords(List<EngStuJFrame.EngNode> words);
    }

    private static final String DEEPSEEK_URL = "https://chat.deepseek.com/";
    private static final String DEFAULT_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    // 如果 deepseek-chat 不支持图片，在 API 设置中改为支持视觉的模型如 gpt-4o / qwen-vl-max 等
    private static final String INSTRUCTION =
            "请识别图片中的英语单词、词性与对应的中文释义，" +
            "以纯文本格式输出，每行一个单词，格式为：英文单词\t词性. 中文释义（制表符分隔）。" +
            "例如：run\tv. 跑步。不要多余的解释，只输出列表。";

    private static final Color BG_COLOR   = new Color(250, 251, 252);
    private static final Color ACCENT_BLUE = new Color(70, 130, 220);
    private static final Color DROP_BORDER = new Color(180, 200, 230);

    private final ImageRecognitionCallback callback;
    private final List<EngStuJFrame.EngNode> recognizedWords = new ArrayList<>();
    private File selectedImageFile;

    // UI
    private JLabel statusLabel;
    private JButton btnApiCall, btnCancel, btnAddAll, btnWebMode;
    private JPanel recognitionResultPanel, dropZone;
    private JLabel dropLabel, imagePreview;
    private JTextField apiUrlField, apiKeyField, modelField;
    private JPanel apiConfigPanel;

    // 网页模式
    private JPanel webPanel;
    private JTextArea resultInputArea;

    public EngImageRecognition(ImageRecognitionCallback callback) {
        this.callback = callback;
        loadApiConfig();
        buildUI();
    }

    // ════════════════════ UI ════════════════════

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        // ── 顶部: API 配置（可折叠） ──
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(8, 12, 4, 12));
        topPanel.setBackground(BG_COLOR);

        JButton toggleConfig = new JButton("▼ API 设置");
        toggleConfig.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        toggleConfig.setFocusPainted(false);
        toggleConfig.setContentAreaFilled(false);

        apiConfigPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 2));
        apiConfigPanel.setBackground(BG_COLOR);
        apiConfigPanel.add(new JLabel("URL:"));
        apiUrlField = new JTextField(DEFAULT_API_URL, 32);
        apiUrlField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        apiConfigPanel.add(apiUrlField);
        apiConfigPanel.add(new JLabel("Key:"));
        apiKeyField = new JTextField(22);
        apiKeyField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        apiConfigPanel.add(apiKeyField);
        apiConfigPanel.add(new JLabel("Model:"));
        modelField = new JTextField(DEFAULT_MODEL, 16);
        modelField.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        apiConfigPanel.add(modelField);
        JButton btnSave = new JButton("保存配置");
        btnSave.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveApiConfig());
        apiConfigPanel.add(btnSave);

        toggleConfig.addActionListener(e -> {
            apiConfigPanel.setVisible(!apiConfigPanel.isVisible());
            toggleConfig.setText(apiConfigPanel.isVisible() ? "▼ API 设置" : "▶ API 设置");
            revalidate(); repaint();
        });
        topPanel.add(toggleConfig, BorderLayout.NORTH);
        topPanel.add(apiConfigPanel, BorderLayout.CENTER);

        // ── 中部: 拖放区 + 预览区 ──
        JPanel mainPanel = new JPanel(new BorderLayout(12, 0));
        mainPanel.setBorder(new EmptyBorder(6, 12, 8, 12));
        mainPanel.setBackground(BG_COLOR);

        // 左侧: 拖放区
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(BG_COLOR);
        leftPanel.setPreferredSize(new Dimension(340, 1));

        dropZone = new JPanel(new BorderLayout());
        dropZone.setPreferredSize(new Dimension(320, 240));
        dropZone.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DROP_BORDER, 2, true),
                new EmptyBorder(15, 15, 15, 15)));
        dropZone.setBackground(Color.WHITE);

        JPanel dropInner = new JPanel(new GridBagLayout());
        dropInner.setBackground(Color.WHITE);
        dropLabel = new JLabel("<html><center>拖入图片到此处<br><font color=#888>或点击选择文件</font></center></html>");
        dropLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        dropLabel.setForeground(new Color(120, 120, 120));
        dropLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dropInner.add(dropLabel);
        dropZone.add(dropInner, BorderLayout.CENTER);

        // 图片预览标签
        imagePreview = new JLabel();
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreview.setVisible(false);

        // 点击选择图片
        dropZone.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectImageFile(); }
        });
        dropLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectImageFile(); }
        });

        // 拖放支持
        setupDragAndDrop();

        JPanel dropWrapper = new JPanel(new CardLayout());
        dropWrapper.setBackground(BG_COLOR);
        dropWrapper.add(dropZone, "drop");
        leftPanel.add(dropWrapper, BorderLayout.CENTER);

        // 调用按钮
        btnApiCall = new JButton("调用 AI 识别");
        btnApiCall.setFont(new Font("微软雅黑", Font.BOLD, 16));
        btnApiCall.setBackground(ACCENT_BLUE);
        btnApiCall.setForeground(Color.WHITE);
        btnApiCall.setFocusPainted(false);
        btnApiCall.setPreferredSize(new Dimension(0, 42));
        btnApiCall.addActionListener(this::callApiRecognition);
        leftPanel.add(btnApiCall, BorderLayout.SOUTH);

        // 网页模式（隐藏）
        webPanel = new JPanel(new BorderLayout(5, 5));
        webPanel.setBorder(new TitledBorder("网页模式 — 粘贴 DeepSeek 回复"));
        webPanel.setBackground(BG_COLOR);
        resultInputArea = new JTextArea(4, 0);
        resultInputArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        resultInputArea.setLineWrap(true);
        JButton btnParse = new JButton("解析并预览");
        btnParse.addActionListener(this::parseWebResponse);
        JPanel webBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        webBtnRow.setBackground(BG_COLOR);
        webBtnRow.add(btnParse);
        webPanel.add(new JScrollPane(resultInputArea), BorderLayout.CENTER);
        webPanel.add(webBtnRow, BorderLayout.SOUTH);
        webPanel.setVisible(false);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setBackground(BG_COLOR);
        leftWrap.add(leftPanel, BorderLayout.CENTER);
        leftWrap.add(webPanel, BorderLayout.SOUTH);
        mainPanel.add(leftWrap, BorderLayout.WEST);

        // 右侧: 识别结果预览
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_COLOR);
        rightPanel.setBorder(new TitledBorder("识别结果"));
        recognitionResultPanel = new JPanel();
        recognitionResultPanel.setBackground(BG_COLOR);
        recognitionResultPanel.setLayout(new WrapLayout(FlowLayout.LEADING, 8, 8));
        JScrollPane rightScroll = new JScrollPane(recognitionResultPanel);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightPanel.add(rightScroll, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // ── 底部: 状态 + 按钮 ──
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(6, 12, 8, 12));
        bottomPanel.setBackground(BG_COLOR);

        statusLabel = new JLabel("拖入图片或点击选择，然后调用 AI 识别");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(100, 100, 100));

        btnWebMode = new JButton("网页版");
        btnWebMode.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        btnWebMode.setFocusPainted(false);
        btnWebMode.addActionListener(e -> toggleWebMode());

        btnAddAll = new JButton("添加到词库");
        btnAddAll.setFont(new Font("微软雅黑", Font.BOLD, 13));
        btnAddAll.setFocusPainted(false);
        btnAddAll.setVisible(false);
        btnAddAll.addActionListener(e -> {
            if (!recognizedWords.isEmpty()) {
                callback.onAddWords(new ArrayList<>(recognizedWords));
                recognizedWords.clear();
                recognitionResultPanel.removeAll();
                recognitionResultPanel.revalidate();
                recognitionResultPanel.repaint();
                btnAddAll.setVisible(false);
            }
        });

        btnCancel = new JButton("返回");
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> { recognizedWords.clear(); callback.onCancel(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(BG_COLOR);
        btnRow.add(btnWebMode);
        btnRow.add(btnAddAll);
        btnRow.add(btnCancel);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(btnRow, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ════════════════════ 拖放支持 ════════════════════

    @SuppressWarnings("unchecked")
    private void setupDragAndDrop() {
        // Windows/Java 9+ 使用 TransferHandler
        dropZone.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File f = files.get(0);
                        if (isImageFile(f)) {
                            setImageFile(f);
                            return true;
                        }
                    }
                } catch (Exception ignored) {}
                return false;
            }
        });

        // Linux / fallback: AWT DropTarget
        try {
            DropTarget dt = new DropTarget();
            dt.setComponent(dropZone);
            dt.addDropTargetListener(new DropTargetAdapter() {
                @Override
                public void drop(DropTargetDropEvent e) {
                    try {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        Object data = e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (data instanceof List) {
                            List<?> files = (List<?>) data;
                            if (!files.isEmpty() && files.get(0) instanceof File) {
                                File f = (File) files.get(0);
                                if (isImageFile(f)) {
                                    setImageFile(f);
                                    e.dropComplete(true);
                                    return;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    e.dropComplete(false);
                }
                @Override
                public void dragEnter(DropTargetDragEvent e) {
                    dropZone.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(ACCENT_BLUE, 2, true),
                            new EmptyBorder(15, 15, 15, 15)));
                }
                @Override
                public void dragExit(DropTargetEvent e) {
                    dropZone.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DROP_BORDER, 2, true),
                            new EmptyBorder(15, 15, 15, 15)));
                }
            });
            dropZone.setDropTarget(dt);
        } catch (Exception ignored) {}
    }

    // ════════════════════ 图片处理 ════════════════════

    private void selectImageFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override public boolean accept(File f) { return f.isDirectory() || isImageFile(f); }
            @Override public String getDescription() { return "图片 (*.png, *.jpg, *.gif, *.bmp, *.webp)"; }
        });
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setImageFile(chooser.getSelectedFile());
        }
    }

    private void setImageFile(File f) {
        selectedImageFile = f;
        statusLabel.setText("已选择: " + f.getName() + "  (" + formatFileSize(f.length()) + ")");
        showImagePreview(f);
        btnApiCall.setEnabled(true);
    }

    private void showImagePreview(File f) {
        try {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage();
            int zoneW = dropZone.getWidth() > 0 ? dropZone.getWidth() - 40 : 280;
            int zoneH = dropZone.getHeight() > 0 ? dropZone.getHeight() - 40 : 200;
            double scale = Math.min((double) zoneW / img.getWidth(null),
                                    (double) zoneH / img.getHeight(null));
            if (scale < 1) {
                img = img.getScaledInstance((int)(img.getWidth(null) * scale),
                        (int)(img.getHeight(null) * scale), Image.SCALE_SMOOTH);
            }
            imagePreview.setIcon(new ImageIcon(img));
            imagePreview.setVisible(true);
            dropLabel.setVisible(false);
            dropZone.removeAll();
            dropZone.add(imagePreview, BorderLayout.CENTER);
            dropZone.revalidate();
            dropZone.repaint();
        } catch (Exception ex) {
            statusLabel.setText("图片预览失败，但不影响识别");
        }
    }

    private boolean isImageFile(File f) {
        String name = f.getName().toLowerCase();
        return name.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp)$");
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / 1048576.0);
    }

    // 压缩并缩放图片，减少 API 请求大小（目标 < 1 MB）
    private static byte[] prepareImageForApi(File f) throws IOException {
        BufferedImage src = ImageIO.read(f);
        if (src == null) throw new IOException("无法读取图片文件");

        int maxSide = 1600;  // 限制最大边 1600px
        int w = src.getWidth(), h = src.getHeight();
        if (w > maxSide || h > maxSide) {
            double scale = Math.min((double) maxSide / w, (double) maxSide / h);
            w = (int) (w * scale);
            h = (int) (h * scale);
        }

        // 统一转为 RGB + 白底，避免 PNG 透明区在 JPEG 中变黑
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(src.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        // 逐步降低质量直到 < 1 MB
        for (float quality : new float[]{0.9f, 0.7f, 0.5f, 0.35f}) {
            byte[] data = writeJpeg(out, quality);
            if (data.length < 1024 * 1024) return data;
        }
        // 兜底：最低质量
        return writeJpeg(out, 0.25f);
    }

    private static byte[] writeJpeg(BufferedImage img, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.stream.MemoryCacheImageOutputStream ios = new javax.imageio.stream.MemoryCacheImageOutputStream(baos);
        writer.setOutput(ios);
        javax.imageio.plugins.jpeg.JPEGImageWriteParam param = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(null);
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
        writer.dispose();
        baos.flush();
        return baos.toByteArray();
    }

    // ════════════════════ API 调用 ════════════════════

    private void callApiRecognition(ActionEvent e) {
        if (selectedImageFile == null) {
            statusLabel.setText("请先选择或拖入一张图片！");
            return;
        }
        String apiUrl = apiUrlField.getText().trim();
        String apiKey = apiKeyField.getText().trim();
        if (apiUrl.isEmpty()) { statusLabel.setText("请填写 API URL"); return; }

        btnApiCall.setEnabled(false);
        btnApiCall.setText("识别中...");
        statusLabel.setText("正在调用 AI，请稍候...");

        new Thread(() -> {
            try {
                // 压缩/缩放图片，避免 API 因图片过大返回 400
                byte[] imageBytes = prepareImageForApi(selectedImageFile);
                statusLabel.setText("图片已压缩为 " + formatFileSize(imageBytes.length) + "，正在调用 AI...");
                String b64 = Base64.getEncoder().encodeToString(imageBytes);
                String mime = "image/jpeg"; // 统一压缩为 JPEG
                String body = buildJson(b64, mime);
                String result = httpPost(apiUrl, apiKey, body);

                SwingUtilities.invokeLater(() -> {
                    parseResponse(result);
                    statusLabel.setText("识别完成！共 " + recognizedWords.size() + " 个单词");
                    btnApiCall.setText("调用 AI 识别");
                    btnApiCall.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("识别失败: " + ex.getMessage());
                    btnApiCall.setText("调用 AI 识别");
                    btnApiCall.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                            "API 调用失败:\n" + ex.getMessage() + "\n\n请检查 URL/Key 和网络，或使用「网页版」手动识别。",
                            "失败", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private String buildJson(String b64, String mime) {
        String instr = INSTRUCTION.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        String model = modelField.getText().trim();
        if (model.isEmpty()) model = DEFAULT_MODEL;
        return "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"user\","
            + "\"content\":[{\"type\":\"text\",\"text\":\"" + instr + "\"},"
            + "{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:" + mime + ";base64," + b64 + "\"}}]}],"
            + "\"max_tokens\":4096,\"temperature\":0.1}";
    }

    private String httpPost(String apiUrl, String key, String body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(apiUrl).openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/json");
        if (!key.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + key);
        c.setDoOutput(true);
        c.setConnectTimeout(30000);
        c.setReadTimeout(120000);
        try (OutputStream os = c.getOutputStream()) { os.write(body.getBytes("UTF-8")); }

        int code = c.getResponseCode();
        InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line; while ((line = r.readLine()) != null) sb.append(line);
        }
        if (code >= 400) throw new IOException("HTTP " + code + "\n" + sb.toString());
        return extractContent(sb.toString());
    }

    private String extractContent(String json) {
        for (String m : new String[]{"\"content\":\"", "\"content\": \""}) {
            int i = json.indexOf(m);
            if (i >= 0) {
                i += m.length();
                StringBuilder sb = new StringBuilder();
                boolean esc = false;
                for (; i < json.length(); i++) {
                    char c = json.charAt(i);
                    if (esc) {
                        esc = false;
                        switch (c) { case 'n': sb.append('\n'); break; case 't': sb.append('\t'); break;
                            case 'r': sb.append('\r'); break; case '"': sb.append('"'); break;
                            case '\\': sb.append('\\'); break; default: sb.append(c); }
                    } else if (c == '\\') { esc = true; }
                    else if (c == '"') { return sb.toString(); }
                    else { sb.append(c); }
                }
                return sb.toString();
            }
        }
        return json;
    }

    // ════════════════════ 解析与预览 ════════════════════

    private void parseWebResponse(ActionEvent e) {
        parseResponse(resultInputArea.getText().trim());
    }

    private void parseResponse(String text) {
        if (text.isEmpty()) return;
        recognizedWords.clear();
        for (String line : text.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\t", 2);
            if (parts.length < 2) parts = line.split("\\s{2,}", 2);
            if (parts.length >= 2) {
                EngStuJFrame.EngNode node = new EngStuJFrame.EngNode();
                node.word = parts[0].trim();
                node.chinese = parts[1].trim();
                recognizedWords.add(node);
            }
        }
        refreshCards();
    }

    private void refreshCards() {
        recognitionResultPanel.removeAll();
        if (recognizedWords.isEmpty()) {
            recognitionResultPanel.add(new JLabel("未解析到单词"));
            btnAddAll.setVisible(false);
        } else {
            for (EngStuJFrame.EngNode node : recognizedWords) {
                recognitionResultPanel.add(createCard(node));
            }
            btnAddAll.setVisible(true);
        }
        recognitionResultPanel.revalidate();
        recognitionResultPanel.repaint();
    }

    private JPanel createCard(EngStuJFrame.EngNode node) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(290, 85));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel wl = new JLabel(node.word);
        wl.setFont(new Font("微软雅黑", Font.BOLD, 15));
        JLabel cl = new JLabel(node.chinese.isEmpty() ? "(无释义)" : node.chinese);
        cl.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        info.add(wl); info.add(Box.createVerticalStrut(4)); info.add(cl);

        JPanel btns = new JPanel(new GridLayout(2, 1, 4, 4));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(0, 4, 0, 0));
        JButton edit = new JButton("改"); edit.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JButton del  = new JButton("删"); del.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        edit.setFocusPainted(false); del.setFocusPainted(false);
        edit.addActionListener(ev -> {
            String nw = JOptionPane.showInputDialog(this, "单词:", node.word);
            if (nw != null && !nw.trim().isEmpty()) node.word = nw.trim();
            String nc = JOptionPane.showInputDialog(this, "释义:", node.chinese);
            if (nc != null) node.chinese = nc.trim();
            refreshCards();
        });
        del.addActionListener(ev -> { recognizedWords.remove(node); refreshCards(); });
        btns.add(edit); btns.add(del);

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ════════════════════ 网页模式 ════════════════════

    private void toggleWebMode() {
        webPanel.setVisible(!webPanel.isVisible());
        btnWebMode.setText(webPanel.isVisible() ? "收起网页版" : "网页版");
        if (webPanel.isVisible()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(INSTRUCTION), null);
            try { Desktop.getDesktop().browse(URI.create(DEEPSEEK_URL)); } catch (Exception ignored) {}
            statusLabel.setText("网页模式 — 在 DeepSeek 中粘贴指令（已复制），复制回复后粘贴到文本框");
        } else {
            statusLabel.setText("拖入图片或点击选择，然后调用 AI 识别");
        }
        revalidate(); repaint();
    }

    // ════════════════════ 配置持久化 ════════════════════

    private static final String CONFIG_FILE = "D:/EngStudy/api_config.txt";

    private void saveApiConfig() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            pw.println(apiUrlField.getText().trim());
            pw.println(apiKeyField.getText().trim());
            pw.println(modelField.getText().trim());
            statusLabel.setText("配置已保存");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
        }
    }

    private void loadApiConfig() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String url = br.readLine(), key = br.readLine(), model = br.readLine();
            if (url != null && !url.isEmpty()) apiUrlField.setText(url);
            if (key != null && !key.isEmpty()) apiKeyField.setText(key);
            if (model != null && !model.isEmpty()) modelField.setText(model);
        } catch (Exception ignored) {}
    }

    private String getMimeType(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".bmp")) return "image/bmp";
        if (name.endsWith(".webp")) return "image/webp";
        return "image/png";
    }

    // ════════════════════ WrapLayout ════════════════════

    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layoutSize(t, true); }
        @Override public Dimension minimumLayoutSize(Container t) { return layoutSize(t, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int tw = target.getWidth() <= 0 ? Integer.MAX_VALUE : target.getWidth();
                int hg = getHgap(), vg = getVgap();
                Insets ins = target.getInsets();
                int mw = tw - ins.left - ins.right - hg * 2;
                int rw = 0, rh = 0, totalW = 0, totalH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rw + d.width > mw) { totalW = Math.max(totalW, rw); totalH += rh + vg; rw = d.width; rh = d.height; }
                    else { rw += d.width + hg; rh = Math.max(rh, d.height); }
                }
                totalW = Math.max(totalW, rw); totalH += rh;
                return new Dimension(ins.left + totalW + ins.right, ins.top + totalH + ins.bottom);
            }
        }
    }
}
