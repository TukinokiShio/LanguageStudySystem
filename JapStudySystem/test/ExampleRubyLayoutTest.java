package LanguageStudySystem.JavaJapStuSystem;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

/** Regression checks for complete plain-text rendering in answer-page ruby examples. */
public final class ExampleRubyLayoutTest {
    private static final String EXAMPLE_1 =
            "彼（かれ）は何度（なんど）断（ことわ）っても、しつこく誘（さそ）ってくる。";
    private static final String EXAMPLE_2 =
            "この汚（よご）れはしつこくて、なかなか落（お）ちない。";
    private static final Font BASE_FONT = new Font("微软雅黑", Font.PLAIN, 22);
    private static final Font RUBY_FONT = new Font("微软雅黑", Font.PLAIN, 13);

    private ExampleRubyLayoutTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--live".equals(args[0])) {
            runLiveWindowCheck(args.length > 1 ? args[1] : null);
            return;
        }
        runGeometryChecks();
        System.out.println("ExampleRubyLayoutTest: PASS");
    }

    private static void runGeometryChecks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int viewportWidth : new int[]{300, 800, 1146}) {
                verifyGeometry(EXAMPLE_1, "っても、しつこく", viewportWidth);
                verifyGeometry(EXAMPLE_2, "れはしつこくて、なかなか", viewportWidth);
            }
        });
    }

    private static void verifyGeometry(String sentence, String expectedPlainSegment,
                                      int viewportWidth) {
        JScrollPane answerScroll = createAnswerPane(sentence);
        answerScroll.setSize(viewportWidth, 700);
        layoutRecursively(answerScroll);
        assertLabelHasMargin(answerScroll, expectedPlainSegment, viewportWidth);
    }

    /** Optional on-screen probe catches font metrics that change after peer realization. */
    private static void runLiveWindowCheck(String screenshotPath) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("--live requires a Windows desktop session");
        }
        final JFrame[] frameRef = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("JSS answer example rendering probe");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(createAnswerPane(EXAMPLE_1, EXAMPLE_2));
            frame.setSize(1050, 420);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.validate();
            frameRef[0] = frame;
        });

        JFrame frame = frameRef[0];
        try {
            Robot robot = new Robot();
            robot.waitForIdle();
            robot.delay(500);
            SwingUtilities.invokeAndWait(() -> {
                assertLabelHasMargin(frame.getContentPane(), "っても、しつこく", 1050);
                assertLabelHasMargin(frame.getContentPane(), "れはしつこくて、なかなか", 1050);
            });
            if (screenshotPath != null) {
                BufferedImage screenshot = robot.createScreenCapture(frame.getBounds());
                ImageIO.write(screenshot, "png", new File(screenshotPath));
            }
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
        System.out.println("ExampleRubyLayoutTest --live: PASS");
    }

    private static JScrollPane createAnswerPane(String... sentences) {
        JPanel table = new JPanel();
        table.setLayout(new BoxLayout(table, BoxLayout.Y_AXIS));
        for (int index = 0; index < sentences.length; index++) {
            table.add(createExampleRow(index == 0 ? "例句" : "例句2", sentences[index]));
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(255, 251, 240));
        content.setBorder(new EmptyBorder(12, 14, 12, 14));
        int naturalTableHeight = table.getPreferredSize().height;
        table.setMaximumSize(new Dimension(Integer.MAX_VALUE, naturalTableHeight));
        content.add(table);
        JTextArea instruction = new JTextArea("请点击上方 记得 或 不记得 按钮进行选择");
        instruction.setRows(1);
        instruction.setLineWrap(false);
        instruction.setEditable(false);
        content.add(instruction);

        JScrollPane answerScroll = new JScrollPane(content);
        answerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return answerScroll;
    }

    private static JPanel createExampleRow(String caption, String sentence) {
        Color bgColor = new Color(255, 251, 240);
        JPanel rubyPanel = JapJFrameKanaPrint.createRubyPanel(
                sentence, BASE_FONT, RUBY_FONT, new Color(80, 80, 80), bgColor);
        int typicalRubyWidth = 800;
        rubyPanel.setSize(typicalRubyWidth, 0);
        Dimension wrappedSize = rubyPanel.getPreferredSize();
        rubyPanel.setPreferredSize(new Dimension(
                Math.max(typicalRubyWidth, wrappedSize.width), wrappedSize.height));

        JPanel rubyWrapper = new JPanel(new BorderLayout());
        rubyWrapper.setBackground(bgColor);
        rubyWrapper.add(rubyPanel, BorderLayout.CENTER);
        JScrollPane valueScroll = new JScrollPane(rubyWrapper);
        valueScroll.setBorder(null);
        valueScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        valueScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        int prefH = wrappedSize.height + 4;
        valueScroll.setMinimumSize(new Dimension(0, prefH));
        valueScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefH));
        valueScroll.setPreferredSize(new Dimension(0, prefH));

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(bgColor);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 200, 180)));
        JLabel labelColumn = new JLabel(caption);
        labelColumn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        labelColumn.setBackground(new Color(235, 230, 215));
        labelColumn.setOpaque(true);
        labelColumn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        labelColumn.setPreferredSize(new Dimension(100, labelColumn.getPreferredSize().height));
        labelColumn.setMinimumSize(new Dimension(100, 0));
        labelColumn.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
        row.add(labelColumn);
        row.add(valueScroll);
        int rowHeight = row.getPreferredSize().height;
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        return row;
    }

    private static void assertLabelHasMargin(Container parent, String text, int viewportWidth) {
        JLabel label = findLabel(parent, text);
        check(label != null, "例句普通文本片段未完整保留：" + text);
        Dimension preferred = label.getPreferredSize();
        check(label.getWidth() >= preferred.width + 4,
                "例句标签在当前布局/绘制上下文余量不足：viewport=" + viewportWidth
                        + ", text=" + label.getText() + ", actual=" + label.getWidth()
                        + ", preferred=" + preferred.width);
    }

    private static void layoutRecursively(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) {
            if (child instanceof Container) layoutRecursively((Container) child);
        }
    }

    private static JLabel findLabel(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JLabel && text.equals(((JLabel) child).getText())) {
                return (JLabel) child;
            }
            if (child instanceof Container) {
                JLabel nested = findLabel((Container) child, text);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
