package LanguageStudySystem.JavaJapStuSystem;

import javax.swing.*;
import java.awt.*;

/**
 * 考察点修改 / 删除统一处理类。
 *
 * <p>提取原有的编辑与删除逻辑，通过 {@link EditorContext} 接口与主窗口交互，
 * 主窗口只需在相应按钮事件中调用本类方法即可复用全部修改/删除功能。</p>
 *
 * <p>使用方式（在主窗口 {@code JapStuJFrame} 中）：</p>
 * <pre>
 *   EditorContext ctx = new EditorContextImpl(this);   // 实现接口
 *   JapEditAndDel editAndDel = new JapEditAndDel(ctx);
 *
 *   // 卡片修改按钮
 *   editAndDel.enterEditMode(node);
 *
 *   // 编辑按钮行
 *   editAndDel.editField(1);    // 修改日文
 *   editAndDel.editField(2);    // 修改释义
 *   editAndDel.editField(4);    // 修改例句
 *   editAndDel.editField(5);    // 修改例句译
 *   editAndDel.convertToGrammar(); // 单词转语法
 *   editAndDel.finishEdit();    // 完成修改
 *   editAndDel.deleteEditTarget(); // 删除当前编辑考察点
 *
 *   // 输入框确认（当处于字段编辑状态或转换语法状态时）
 *   editAndDel.handleConfirmInput(inputText);
 *
 *   // 任意位置删除节点（卡片删除、测试后删除等）
 *   editAndDel.deleteNode(node);
 * </pre>
 */
public class JapEditAndDel {

    // ==================== 回调接口 ====================

    /**
     * 主窗口必须实现该接口，以便本类能够操纵 UI 与数据。
     */
    public interface EditorContext {
        /* ── 文本输出 ── */
        void print(String s);
        void printBold(String s);

        /* ── 内容区域切换 ── */
        void clearAll();
        void switchToTextArea();

        /* ── 统计标签 ── */
        void updateStatsLabel();
        void setStatsLabelVisible(boolean visible);

        /* ── 按钮可见性 ── */
        void resetAllButtonVisibility();
        void setEditButtonsVisible(boolean visible);
        void showEditButtonsForType(int type);
        void setInputVisible(boolean visible);
        void setConvertToGrammarButtonVisible(boolean visible);

        /* ── 主窗口状态 ── */
        void setState(int state);

        /* ── 数据持久化 ── */
        void saveToFile();

        /* ── 考察点数量管理 ── */
        int getTotalItems();
        void setTotalItems(int items);

        /* ── 链表操作（获取头节点、删除节点） ── */
        JapStuJFrame.JaNode getGlobalListHead();
        void deleteNodeFromList(JapStuJFrame.JaNode node);

        /* ── 语法信息块输出（振假名表格） ── */
        void embedGrammarInfo(JapStuJFrame.JaNode node);

        /* ── 对话框父组件 ── */
        Component getDialogParent();
    }

    // ==================== 状态管理 ====================

    private static final int STATE_IDLE        = 0;   // 空闲
    private static final int STATE_EDIT_MODE   = 7;   // 编辑模式（可点击字段按钮）
    private static final int STATE_FIELD_INPUT = 8;   // 正在输入字段新值

    // 单词→语法 转换状态
    private static final int STATE_CONVERT_GRAMMAR_EXAMPLE     = 9;   // 正在输入语法例句
    private static final int STATE_CONVERT_GRAMMAR_EXAMPLE_CH  = 10;  // 正在输入语法例句译

    private final EditorContext ctx;
    private JapStuJFrame.JaNode editTarget;
    private int editField;    // 1-日文, 2-释义, 4-例句, 5-例句译
    private int internalState = STATE_IDLE;

    // ==================== 构造方法 ====================

    public JapEditAndDel(EditorContext context) {
        this.ctx = context;
    }

    // ==================== 公开 API ====================

    /**
     * 进入针对某个考察点的编辑模式。
     * 清空内容区，显示当前考察点信息并激活编辑按钮。
     * 仅当考察点为单词类型时，显示"修改为语法"按钮
     */
    public void enterEditMode(JapStuJFrame.JaNode node) {
        if (node == null) return;

        this.editTarget = node;
        this.editField = 0;
        this.internalState = STATE_EDIT_MODE;

        ctx.clearAll();
        ctx.switchToTextArea();
        ctx.setState(STATE_EDIT_MODE);
        ctx.setStatsLabelVisible(false);
        ctx.print("===== 修改考察点 =====");
        ctx.print("日文：" + node.japanese);
        ctx.print("释义：" + node.chinese);
        ctx.print("类型：" + (node.type == 1 ? "单词" : "语法"));
        ctx.print("请点击上方对应按钮进行修改");

        ctx.resetAllButtonVisibility();
        ctx.showEditButtonsForType(node.type);
        // 只有单词类型才显示"修改为语法"按钮（语法点不能转为单词）
        ctx.setConvertToGrammarButtonVisible(node.type == 1);
    }

    /**
     * 开始修改指定字段（1=日文, 2=释义, 4=例句, 5=例句译）。
     */
    public void editField(int field) {
        if (internalState != STATE_EDIT_MODE || editTarget == null) return;

        this.editField = field;
        this.internalState = STATE_FIELD_INPUT;
        ctx.setState(STATE_FIELD_INPUT);
        ctx.setInputVisible(true);

        switch (field) {
            case 1 -> ctx.print("\n当前日文：" + editTarget.japanese + "\n请输入新日文：");
            case 2 -> ctx.print("\n当前释义：" + editTarget.chinese + "\n请输入新释义：");
            case 4 -> ctx.print("\n当前例句：" + editTarget.example + "\n请输入新例句：");
            case 5 -> ctx.print("\n当前例句译：" + editTarget.exampleCh + "\n请输入新翻译：");
            default -> {
                // 不应出现
                internalState = STATE_EDIT_MODE;
                ctx.setState(STATE_EDIT_MODE);
            }
        }
    }

    /**
     * 单词转语法点功能
     * 语法点不能转为单词，点击后依次输入例句和例句释义
     * 输入不完全（例句或例句释义为空白）则修改失败，考察点仍为单词
     */
    public void convertToGrammar() {
        // 仅在编辑模式且目标为单词时允许转换
        if (internalState != STATE_EDIT_MODE || editTarget == null || editTarget.type != 1) {
            return;
        }
        // 隐藏"修改为语法"按钮，避免重复点击
        ctx.setConvertToGrammarButtonVisible(false);
        // 进入例句输入状态
        this.internalState = STATE_CONVERT_GRAMMAR_EXAMPLE;
        ctx.setState(STATE_CONVERT_GRAMMAR_EXAMPLE);
        ctx.setInputVisible(true);
        ctx.print("\n===== 单词转语法点 =====");
        ctx.print("请输入语法例句：");
        ctx.print("格式示例：毎日（まいにち）の　運動（うんどう）のおかげで、　体（からだ）が　健康（けんこう）になりました。");
        ctx.print("【格式规范】在需要注音的汉字前加一个全角空格（　）");
    }

    /**
     * 处理用户确认输入（当处于字段编辑状态或转换语法状态时）。
     */
    public void handleConfirmInput(String input) {
        if (input == null) return;

        // 处理普通字段编辑状态
        if (internalState == STATE_FIELD_INPUT && editTarget != null) {
            switch (editField) {
                case 1 -> editTarget.japanese = input.trim();
                case 2 -> editTarget.chinese  = input.trim();
                case 4 -> editTarget.example  = input.trim();
                case 5 -> editTarget.exampleCh = input.trim();
            }
            ctx.print("修改成功！可继续修改或点击 完成修改");
            // 回到编辑模式
            this.internalState = STATE_EDIT_MODE;
            this.editField = 0;
            ctx.setState(STATE_EDIT_MODE);
            ctx.setInputVisible(false);
            return;
        }

        // 处理单词转语法-例句输入阶段
        if (internalState == STATE_CONVERT_GRAMMAR_EXAMPLE && editTarget != null) {
            String example = input.trim();
            if (example.isEmpty()) {
                ctx.print("错误：例句不能为空，转换失败，考察点仍为单词");
                // 恢复编辑模式和按钮状态
                this.internalState = STATE_EDIT_MODE;
                ctx.setState(STATE_EDIT_MODE);
                ctx.setInputVisible(false);
                ctx.setConvertToGrammarButtonVisible(true);
                return;
            }
            // 保存临时例句
            editTarget.example = example;
            // 进入例句翻译输入状态
            this.internalState = STATE_CONVERT_GRAMMAR_EXAMPLE_CH;
            ctx.setState(STATE_CONVERT_GRAMMAR_EXAMPLE_CH);
            ctx.print("例句输入成功！请输入例句翻译：");
            return;
        }

        // 处理单词转语法-例句翻译输入阶段
        if (internalState == STATE_CONVERT_GRAMMAR_EXAMPLE_CH && editTarget != null) {
            String exampleCh = input.trim();
            if (exampleCh.isEmpty()) {
                ctx.print("错误：例句翻译不能为空，转换失败，考察点仍为单词");
                // 清空临时保存的例句
                editTarget.example = "";
                // 恢复编辑模式和按钮状态
                this.internalState = STATE_EDIT_MODE;
                ctx.setState(STATE_EDIT_MODE);
                ctx.setInputVisible(false);
                ctx.setConvertToGrammarButtonVisible(true);
                return;
            }
            // 输入完整，执行类型转换
            editTarget.exampleCh = exampleCh;
            editTarget.type = 2; // 正式改为语法类型
            ctx.print("转换成功！该考察点已变为语法点");
            ctx.print("类型：语法");
            ctx.print("日文：" + editTarget.japanese);
            ctx.print("释义：" + editTarget.chinese);
            ctx.print("例句：" + editTarget.example);
            ctx.print("例句译：" + editTarget.exampleCh);
            // 回到编辑模式，更新为语法专用编辑按钮
            this.internalState = STATE_EDIT_MODE;
            ctx.setState(STATE_EDIT_MODE);
            ctx.setInputVisible(false);
            ctx.showEditButtonsForType(2); // 显示语法点的例句/例句译编辑按钮
        }
    }

    /**
     * 完成修改，保存文件并退出编辑模式。
     */
    public void finishEdit() {
        if (internalState != STATE_EDIT_MODE || editTarget == null) return;

        ctx.saveToFile();
        ctx.clearAll();
        ctx.print("===== 修改完成！最新信息 =====");
        ctx.print("类型：" + (editTarget.type == 1 ? "单词" : "语法"));
        ctx.print("日文：" + editTarget.japanese);
        ctx.print("释义：" + editTarget.chinese);

        if (editTarget.type == 2) {
            ctx.embedGrammarInfo(editTarget);
        }

        ctx.print("提示：修改已自动保存");

        // 重置状态
        this.internalState = STATE_IDLE;
        this.editTarget = null;
        this.editField = 0;
        ctx.setState(0);
        ctx.resetAllButtonVisibility();
        ctx.updateStatsLabel();
        ctx.setStatsLabelVisible(false);
    }

    /**
     * 删除当前正在编辑的考察点。
     */
    public void deleteEditTarget() {
        if (editTarget == null) return;

        int cfm = JOptionPane.showConfirmDialog(
                ctx.getDialogParent(),
                "确认删除？",
                "提示",
                JOptionPane.YES_NO_OPTION);

        if (cfm == JOptionPane.YES_OPTION) {
            deleteNodeFromList(editTarget);
            ctx.clearAll();
            ctx.print("删除成功！");

            this.internalState = STATE_IDLE;
            this.editTarget = null;
            this.editField = 0;
            ctx.setState(0);
            ctx.setTotalItems(ctx.getTotalItems() - 1);   // 实际由 deleteNodeFromList 内部更新
            ctx.updateStatsLabel();
            ctx.resetAllButtonVisibility();
            ctx.setStatsLabelVisible(true);
        }
    }

    /**
     * 从任意位置删除一个考察点（例如卡片上的删除、测试后的删除）。
     * 不负责 UI 的后续刷新，调用方需自行处理 UI（如重新加载卡片列表）。
     */
    public void deleteNode(JapStuJFrame.JaNode node) {
        if (node == null) return;
        deleteNodeFromList(node);
    }

    /**
     * 供外部判断当前是否处于编辑相关状态（用于主窗口输入框确认时的路由）。
     */
    public boolean isEditing() {
        return internalState == STATE_EDIT_MODE
                || internalState == STATE_FIELD_INPUT
                || internalState == STATE_CONVERT_GRAMMAR_EXAMPLE
                || internalState == STATE_CONVERT_GRAMMAR_EXAMPLE_CH;
    }

    /**
     * 获取当前编辑目标（可能为 null）。
     */
    public JapStuJFrame.JaNode getEditTarget() {
        return editTarget;
    }

    /**
     * 放弃编辑，重置内部状态（不影响文件保存）。
     */
    public void cancelEdit() {
        this.editTarget = null;
        this.editField = 0;
        this.internalState = STATE_IDLE;
    }

    // ==================== 内部工具 ====================

    private void deleteNodeFromList(JapStuJFrame.JaNode target) {
        ctx.deleteNodeFromList(target);
    }
}
