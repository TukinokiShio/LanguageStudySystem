package LanguageStudySystem.JavaJapStuSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡片网格视图组件 —— 负责卡片的响应式布局与显示。
 *
 * <p>使用多组预设卡片宽度，根据视口宽度自动选择最合适的宽度，
 * 使一行能放下尽可能多的卡片，减少空白区域。
 * 窗口过小时显示错误提示。</p>
 */
public class CardGridView extends JPanel {

    // ── 预设卡片宽度（内容区域宽度，不含 padding） ──────────────────
    private static final int[] WIDTH_OPTIONS = {200, 260, 320, 380, 440, 500, 560};

    // ── 布局参数 ────────────────────────────────────────────────────
    private static final int HGAP = 15;          // 卡片水平间距
    private static final int VGAP = 15;          // 卡片垂直间距
    private static final int CARD_PAD_X = 30;    // 卡片左右 padding 之和

    // ── 内部组件 ────────────────────────────────────────────────────
    private final JPanel cardPanel;              // 实际放卡片的容器（WrapLayout）
    private final JScrollPane cardScrollPane;    // 包裹 cardPanel 的滚动面板

    // ── 数据 ────────────────────────────────────────────────────────
    private final List<JPanel> cardList = new ArrayList<>();
    private int currentCardWidth = 560;          // 当前使用的卡片总宽度

    // ── 防抖 Timer ──────────────────────────────────────────────────
    private final Timer resizeTimer;

    public CardGridView() {
        super(new BorderLayout());

        // 卡片容器
        cardPanel = new JPanel(new WrapLayout(FlowLayout.LEADING, HGAP, VGAP));
        cardPanel.setBackground(Color.WHITE);

        // 滚动面板
        cardScrollPane = new JScrollPane(cardPanel);
        cardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cardScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        cardScrollPane.setBorder(null);

        add(cardScrollPane, BorderLayout.CENTER);

        // 防抖 Timer：200ms 后执行 refreshLayout
        resizeTimer = new Timer(200, e -> refreshLayout());
        resizeTimer.setRepeats(false);

        // 监听组件大小变化
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    // ── 对外接口 ────────────────────────────────────────────────────

    /** 获取滚动面板（供外部放入 centerWrapPanel） */
    public JScrollPane getScrollPane() {
        return cardScrollPane;
    }

    /** 清空所有卡片 */
    public void clearCards() {
        cardList.clear();
        cardPanel.removeAll();
    }

    /** 添加一个卡片（由外部创建好传入） */
    public void addCard(JPanel card) {
        cardList.add(card);
    }

    /** 显示所有卡片，触发布局计算 */
    public void showCards() {
        cardPanel.removeAll();

        if (cardList.isEmpty()) {
            cardPanel.revalidate();
            cardPanel.repaint();
            return;
        }

        // 计算最佳卡片宽度
        int viewportWidth = cardScrollPane.getViewport().getWidth();
        if (viewportWidth <= 0) {
            // 视口尚未布局，用父容器宽度估算
            viewportWidth = getWidth() > 0 ? getWidth() - 20 : 1100;
        }

        int bestContentWidth = calculateBestWidth(viewportWidth);

        if (bestContentWidth <= 0) {
            // 窗口过小，显示提示
            showTooSmallHint();
            return;
        }

        currentCardWidth = bestContentWidth + CARD_PAD_X;

        // 用卡片自然尺寸获取最大高度（不设 preferredSize，让布局管理器自行计算）
        int maxHeight = 0;
        for (JPanel card : cardList) {
            Dimension pref = card.getPreferredSize();
            if (pref.height > maxHeight) maxHeight = pref.height;
        }

        // 统一设置卡片尺寸
        for (JPanel card : cardList) {
            card.setPreferredSize(new Dimension(currentCardWidth, maxHeight));
            cardPanel.add(card);
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /** 刷新布局（窗口大小变化时调用） */
    public void refreshLayout() {
        if (cardList.isEmpty()) return;

        int viewportWidth = cardScrollPane.getViewport().getWidth();
        if (viewportWidth <= 0) {
            viewportWidth = getWidth() > 0 ? getWidth() - 20 : 1100;
        }

        int bestContentWidth = calculateBestWidth(viewportWidth);

        if (bestContentWidth <= 0) {
            showTooSmallHint();
            return;
        }

        int newCardWidth = bestContentWidth + CARD_PAD_X;

        // 宽度没变，无需重新布局
        if (newCardWidth == currentCardWidth) return;
        currentCardWidth = newCardWidth;

        cardPanel.removeAll();

        // 用自然尺寸获取最大高度
        int maxHeight = 0;
        for (JPanel card : cardList) {
            card.setPreferredSize(null);
            Dimension pref = card.getPreferredSize();
            if (pref.height > maxHeight) maxHeight = pref.height;
        }

        for (JPanel card : cardList) {
            card.setPreferredSize(new Dimension(currentCardWidth, maxHeight));
            cardPanel.add(card);
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // ── 内部方法 ────────────────────────────────────────────────────

    /**
     * 根据视口宽度，选择最合适的卡片内容宽度。
     * 目标：使一行放尽可能多的卡片，且剩余空白最少。
     *
     * @param viewportWidth 视口可用宽度
     * @return 最佳卡片内容宽度；若窗口过小返回 -1
     */
    private int calculateBestWidth(int viewportWidth) {
        int bestWidth = -1;
        int minWaste = Integer.MAX_VALUE;

        for (int w : WIDTH_OPTIONS) {
            int totalCardWidth = w + CARD_PAD_X;    // 单卡片总宽（含 padding）
            int cols = viewportWidth / (totalCardWidth + HGAP);  // 一行列数
            if (cols < 1) continue;                  // 放不下，跳过

            // 计算剩余空白
            int usedWidth = cols * totalCardWidth + (cols - 1) * HGAP;
            int waste = viewportWidth - usedWidth;

            // 优先选空白最少的；空白相同时选更宽的卡片
            if (waste < minWaste || (waste == minWaste && w > bestWidth)) {
                minWaste = waste;
                bestWidth = w;
            }
        }

        return bestWidth;
    }

    /** 窗口过小时显示提示 */
    private void showTooSmallHint() {
        cardPanel.removeAll();
        JLabel hint = new JLabel("窗口宽度过小，请放大窗口以显示卡片", SwingConstants.CENTER);
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        hint.setForeground(Color.RED);
        hint.setBorder(new EmptyBorder(60, 20, 60, 20));
        cardPanel.add(hint);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // ── WrapLayout 内部类 ──────────────────────────────────────────
    // 修复 FlowLayout 在 JScrollPane 中不能正确换行的问题

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
