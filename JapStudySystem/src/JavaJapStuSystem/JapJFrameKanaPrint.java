package LanguageStudySystem.JavaJapStuSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日文假名注音打印工具类
 * 将 "漢字（かな）" 格式的文本渲染为假名在上、汉字在下的振假名效果面板
 *
 * 支持的格式：
 *   漢字（かな）   — 全角圆括号
 *   漢字(かな)     — 半角圆括号（自动兼容）
 *
 * 【录入规范】为让假名精准显示在目标汉字正上方，
 *   请在需要注音的汉字前加一个全角空格（　）：
 *   例：日本　文化（ぶんか）　発展（はってん）した。
 *
 * 优化说明（v3）：
 *   1. 正则基字改为仅匹配汉字（不含平假名/片假名），
 *      避免前置假名被吸入基字导致假名横向偏移
 *   2. 配合全角空格录入规范，假名精准对齐目标汉字
 *   3. 注音单元高度统一：所有单元采用相同的 rubyHeight+baseHeight 固定高度，
 *      普通文本标签也强制对齐到相同行高，避免基线错位
 *   4. 背景色参数化：createRubyPanel 接受 bgColor 参数，默认 null 时透明
 *   5. null / empty 输入安全处理
 *   6. 修复 BottomAlignWrapLayout.layoutContainer x 坐标累计逻辑
 *   7. RubyUnit 透明背景，背景由外部 panel 统一控制
 */
public class JapJFrameKanaPrint {

    /**
     * 匹配 "基字（注音）" 或 "基字(注音)" 格式
     * 基字：仅限汉字 / 长音符（不含平假名/片假名，避免把前置假名吸入基字导致假名偏移）
     * 注音：平假名 / 片假名 / 长音符（1个以上）
     *
     * 录入规范：在需要注音的汉字前添加一个全角空格（　），
     *   例：日本　文化（ぶんか）　発展（はってん）した。
     * 这样假名能精准显示在目标汉字正上方。
     */
    private static final Pattern RUBY_PATTERN = Pattern.compile(
            "([\\p{IsHan}ー々ヵヶ]+)" +
            "[（(]([ぁ-ゖァ-ヴーa-zA-Z0-9\\p{IsHiragana}\\p{IsKatakana}]+)[）)]"
    );

    // ------------------------------------------------------------------ //
    //  公开 API
    // ------------------------------------------------------------------ //

    /**
     * 创建振假名显示面板（假名在上，汉字在下，基线对齐，自动换行）
     *
     * @param text      包含注音的文本，如 "毎日（まいにち）の運動（うんどう）"
     * @param baseFont  汉字/正文字体
     * @param rubyFont  注音假名字体（上方小字）
     * @param rubyColor 注音假名颜色
     * @return 可添加到界面的 JPanel（背景透明，调用方负责设置背景色）
     */
    public static JPanel createRubyPanel(String text, Font baseFont, Font rubyFont, Color rubyColor) {
        return createRubyPanel(text, baseFont, rubyFont, rubyColor, null);
    }

    /**
     * 创建振假名显示面板（带背景色控制）
     *
     * @param text      包含注音的文本
     * @param baseFont  汉字/正文字体
     * @param rubyFont  注音假名字体
     * @param rubyColor 注音颜色
     * @param bgColor   面板背景色，null 表示透明（opaque=false）
     * @return 可添加到界面的 JPanel
     */
    public static JPanel createRubyPanel(String text, Font baseFont, Font rubyFont,
                                          Color rubyColor, Color bgColor) {
        if (text == null || text.isEmpty()) {
            return makeBgPanel(bgColor);
        }

        // 预先计算统一行高（ruby高+base高），使所有单元基线对齐
        FontMetrics baseFM  = getFontMetrics(baseFont);
        FontMetrics rubyFM  = getFontMetrics(rubyFont);
        int rubyH  = rubyFM.getHeight() + 2;   // 注音行高 + 留白
        int baseH  = baseFM.getHeight() + 2;   // 正文行高 + 留白
        int unitH  = rubyH + baseH;             // 单元总高度（统一）

        RubyDisplayPanel panel = new RubyDisplayPanel(bgColor);

        Matcher matcher = RUBY_PATTERN.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String plain = text.substring(lastEnd, matcher.start());
                // 去掉全角空格标记（全角空格仅用于录入时标识注音位置，显示时不需要）
                plain = plain.replace("\u3000", "");
                if (!plain.isEmpty()) {
                    panel.add(createPlainLabel(plain, baseFont, unitH, rubyH, bgColor));
                }
            }
            panel.add(createRubyUnit(
                    matcher.group(1), matcher.group(2),
                    baseFont, rubyFont, rubyColor,
                    unitH, rubyH, bgColor));
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) {
            String plain = text.substring(lastEnd);
            // 去掉全角空格标记
            plain = plain.replace("\u3000", "");
            if (!plain.isEmpty()) {
                panel.add(createPlainLabel(plain, baseFont, unitH, rubyH, bgColor));
            }
        }
        return panel;
    }

    // ------------------------------------------------------------------ //
    //  私有构建方法
    // ------------------------------------------------------------------ //

    /**
     * 普通文本标签：顶部用 EmptyBorder 填充 rubyH 高度，使汉字基线与振假名单元完全对齐。
     * 用 BorderLayout 包一个 JPanel，NORTH 放空白撑顶，CENTER/SOUTH 放文字，
     * 整体高度固定为 unitH，与振假名单元一致。
     */
    private static JPanel createPlainLabel(String text, Font font,
                                            int unitH, int rubyH, Color bgColor) {
        // 底部文字 label
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(font);
        textLabel.setHorizontalAlignment(SwingConstants.LEFT);
        textLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        applyBackground(textLabel, bgColor);

        // 用固定高度的顶部占位撑开 rubyH 空间
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(1, rubyH));
        spacer.setMinimumSize(new Dimension(1, rubyH));
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, rubyH));
        applyBackground(spacer, bgColor);

        // 外层容器：NORTH = spacer（撑高），SOUTH = 文字（基线对齐）
        FontMetrics fm = getFontMetrics(font);
        int textW = fm.stringWidth(text) + 4;
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(textW, unitH);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        applyBackground(wrapper, bgColor);
        wrapper.add(spacer,     BorderLayout.NORTH);
        wrapper.add(textLabel,  BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * 注音单元：假名在上，汉字在下，整体高度固定为 unitH
     */
    private static JPanel createRubyUnit(String base, String ruby,
                                          Font baseFont, Font rubyFont, Color rubyColor,
                                          int unitH, int rubyH, Color bgColor) {
        JLabel rubyLabel = new JLabel(ruby);
        rubyLabel.setFont(rubyFont);
        rubyLabel.setForeground(rubyColor);
        rubyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rubyLabel.setPreferredSize(new Dimension(
                Math.max(getFontMetrics(rubyFont).stringWidth(ruby) + 2,
                         getFontMetrics(baseFont).stringWidth(base) + 2),
                rubyH));
        applyBackground(rubyLabel, bgColor);

        JLabel baseLabel = new JLabel(base);
        baseLabel.setFont(baseFont);
        baseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        baseLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        applyBackground(baseLabel, bgColor);

        // 计算单元宽度：取 ruby 和 base 中较宽的那个
        FontMetrics rubyFM = getFontMetrics(rubyFont);
        FontMetrics baseFM = getFontMetrics(baseFont);
        int w = Math.max(rubyFM.stringWidth(ruby), baseFM.stringWidth(base)) + 4;

        JPanel unit = new JPanel(new BorderLayout(0, 0)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(w, unitH);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        applyBackground(unit, bgColor);
        unit.setBorder(BorderFactory.createEmptyBorder());
        unit.add(rubyLabel, BorderLayout.NORTH);
        unit.add(baseLabel, BorderLayout.SOUTH);

        return unit;
    }

    // ------------------------------------------------------------------ //
    //  工具方法
    // ------------------------------------------------------------------ //

    private static JPanel makeBgPanel(Color bgColor) {
        JPanel p = new JPanel();
        applyBackground(p, bgColor);
        return p;
    }

    private static void applyBackground(JComponent comp, Color bgColor) {
        if (bgColor == null) {
            comp.setOpaque(false);
        } else {
            comp.setOpaque(true);
            comp.setBackground(bgColor);
        }
    }

    /** 获取字体度量（使用临时 JLabel 作为载体） */
    private static FontMetrics getFontMetrics(Font font) {
        return new JLabel().getFontMetrics(font);
    }

    // ------------------------------------------------------------------ //
    //  自定义面板：支持 JScrollPane 中宽度自适应，触发换行
    // ------------------------------------------------------------------ //

    static class RubyDisplayPanel extends JPanel implements Scrollable {

        public RubyDisplayPanel(Color bgColor) {
            super(new BottomAlignWrapLayout(FlowLayout.LEFT, 0, 2));
            if (bgColor == null) {
                setOpaque(false);
            } else {
                setOpaque(true);
                setBackground(bgColor);
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 40;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;   // 宽度跟随视口，触发换行
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // ------------------------------------------------------------------ //
    //  自动换行 + 底部对齐布局（修复版）
    // ------------------------------------------------------------------ //

    static class BottomAlignWrapLayout extends FlowLayout {

        public BottomAlignWrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                int maxWidth  = target.getWidth() - insets.left - insets.right;
                int hgap      = getHgap();
                int vgap      = getVgap();
                int x         = insets.left;
                int y         = insets.top;
                int rowHeight = 0;
                List<Component> rowComps = new ArrayList<>();

                for (Component comp : target.getComponents()) {
                    if (!comp.isVisible()) continue;
                    Dimension dim = comp.getPreferredSize();

                    boolean mustWrap = !rowComps.isEmpty()
                            && (x + dim.width + hgap > insets.left + maxWidth);

                    if (mustWrap) {
                        placeRow(rowComps, insets.left, y, rowHeight);
                        y += rowHeight + vgap;
                        x  = insets.left;
                        rowComps.clear();
                        rowHeight = 0;
                    }

                    rowComps.add(comp);
                    rowHeight = Math.max(rowHeight, dim.height);
                    x += dim.width + hgap;
                }

                if (!rowComps.isEmpty()) {
                    placeRow(rowComps, insets.left, y, rowHeight);
                }
            }
        }

        private void placeRow(List<Component> row, int leftMargin, int y, int rowHeight) {
            int xPos = leftMargin + getHgap();
            for (Component comp : row) {
                Dimension dim = comp.getPreferredSize();
                int compY = y + rowHeight - dim.height;   // 底部对齐
                comp.setBounds(xPos, compY, dim.width, dim.height);
                xPos += dim.width + getHgap();
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return calcLayoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return calcLayoutSize(target, false);
        }

        private Dimension calcLayoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth <= 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets   = target.getInsets();
                int maxWidth    = targetWidth - insets.left - insets.right;
                int rowWidth    = 0, rowHeight = 0;
                int totalWidth  = 0, totalHeight = 0;
                boolean first   = true;

                for (Component comp : target.getComponents()) {
                    if (!comp.isVisible()) continue;
                    Dimension dim = preferred ? comp.getPreferredSize() : comp.getMinimumSize();

                    if (!first && rowWidth + dim.width + hgap > maxWidth) {
                        totalWidth   = Math.max(totalWidth, rowWidth);
                        totalHeight += rowHeight + vgap;
                        rowWidth     = dim.width;
                        rowHeight    = dim.height;
                    } else {
                        rowWidth  += (first ? 0 : hgap) + dim.width;
                        rowHeight  = Math.max(rowHeight, dim.height);
                    }
                    first = false;
                }
                totalWidth   = Math.max(totalWidth, rowWidth);
                totalHeight += rowHeight;

                return new Dimension(
                        insets.left + totalWidth + insets.right,
                        insets.top  + totalHeight + insets.bottom);
            }
        }
    }

    // ===================== 独立测试 =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("JapJFrameKanaPrint v2 - 振假名测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);

            Color bgColor   = new Color(255, 251, 240);  // 护眼色
            Font  baseFont  = new Font("Dialog", Font.PLAIN, 18);
            Font  rubyFont  = new Font("Dialog", Font.PLAIN, 10);
            Color rubyColor = new Color(80, 80, 80);

            JPanel mainContainer = new JPanel();
            mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
            mainContainer.setBackground(bgColor);
            mainContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[][] tests = {
                {"[1] 新格式：全角空格锚点",
                 "今すぐ　家（いえ）を　出（で）てタクシーに　乗（の）って　行（い）けば、　九時（くじ）　出発（しゅっぱつ）の　新幹線（しんかんせん）に　間（ま）に　合（あ）ないこともない。"},
                {"[2] 旅行例句（修正版）",
                 "久々（ひさびさ）の　家族旅行（りょこう）といっても、　温泉旅館（おんせんりょかん）に　一泊（いっぱく）するぐらいだ。"},
                {"[3] 外に出の拆分（修正版）",
                 "雨が　激（はげ）しすぎて、　外（そと）に　出（で）ようがない。"},
                {"[4] ご存知（修正版）",
                 "田中（たなか）さんがいらっしゃるかどうかご　存知（ぞんじ）ですか。"},
                {"[5] 時（とき）に限（かぎ）って",
                 "急（いそ）いでいる　時（とき）に　限（かぎ）って、タクシーがなかなか　捕（つか）まらない。"},
                {"[6] 长句换行（修正版）",
                 "毎日（まいにち）の　運動（うんどう）のおかげで、　体（からだ）が　健康（けんこう）になりました。"},
                {"[7] 纯平假名（无注音）",
                 "ぐっすり眠れました。"},
                {"[8] 混合数字",
                 "昨日（きのう）12個（こ）の　単語（たんご）を　覚（おぼ）えました。"},
                {"[9] 半角括号兼容",
                 "場合(ばあい)によって、　対応(たいおう)が　変(か)わる。"},
                {"[10] null/empty",
                 ""},
            };

            for (String[] t : tests) {
                JLabel title = new JLabel(t[0]);
                title.setFont(new Font("Dialog", Font.BOLD, 13));
                title.setForeground(new Color(80, 80, 160));
                title.setAlignmentX(Component.LEFT_ALIGNMENT);
                mainContainer.add(title);
                mainContainer.add(Box.createVerticalStrut(2));

                JPanel rp = createRubyPanel(t[1], baseFont, rubyFont, rubyColor, bgColor);
                rp.setAlignmentX(Component.LEFT_ALIGNMENT);
                rp.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 210)));
                mainContainer.add(rp);
                mainContainer.add(Box.createVerticalStrut(8));
            }

            JScrollPane sp = new JScrollPane(mainContainer);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            frame.add(sp);
            frame.setVisible(true);
        });
    }
}
