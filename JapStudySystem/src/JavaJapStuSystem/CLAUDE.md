# 日语学习系统 JavaJapStuSystem — 项目记忆

## 项目概述
日文学习系统 V2.0.3，Swing 桌面 GUI 应用，支持本地词库与 JLPT 词书学习。

## 源码文件说明

| 文件 | 行数 | 说明 |
|------|------|------|
| `JapStuJFrame.java` | ~1850 | 主窗口，UI+逻辑 |
| `CardGridView.java` | 261 | 响应式卡片网格布局 |
| `JapEditAndDel.java` | 353 | 编辑/删除统一处理 |
| `JapImageRecognition.java` | 347 | DeepSeek OCR 图片识别 |
| `JapJFrameKanaPrint.java` | 443 | 振假名注音渲染 |
| `Test.java` | 7 | 启动入口 |
| `start_jap.bat` | - | 启动脚本（javaw） |

## 数据架构

### 本地词库 (D:\JaStu.txt)
- 格式：header(`total\tmastered`) + 每行 `japanese\tchinese\ttype\texamTimes\ttrueTimes\texample\texampleCh`
- type: 1=单词, 2=语法
- JaNode 链表：globalList（哨兵头节点）

### JLPT 词书 (D:\JSS\JLPT\SortedJLPT)
- 嵌入源：`JLPT/SortedJLPT/`（随程序打包）
- 工作目录：`D:/JSS/JLPT/SortedJLPT/`（用户进度持久化）
- 5级词书：N5(274词)、N4(299词)、N3(378词)、N2(444词)、N1(459词) 共1854词
- 格式增加 masteryState 第8字段

### JaNode 字段
```
japanese, chinese, type, examTimes, trueTimes, example, exampleCh,
masteryState(0=陌生/1=了解/2=掌握), jlptLevel(0=本地/1-5=N5-N1), wrongTimes, next
```

## 关键状态机制

### JLPT 掌握状态
- 陌生(0)→答对1次→了解(1)→再答对2次→掌握(2)，不抽取
- 答错惩罚：每答错2次，需额外多答对1次
- 加权随机：陌生权重5、了解权重2、近期抽中+1.5×(10-位置)、答错+3

### state 常量
- 0=idle, 1=添加模式, 2=测试提问, 3=测试作答, 4=查找模式
- 7=编辑模式, 8=字段输入, 9=测试结果, 10=图片识别

## 最近的重要修改（2026-06-19）

1. **JLPT 词书导入**：JLPTGenerator.java 从知识库生成 N5-N1 共1854词
2. **UI 改造**："随机测试"→"测试本地词库"；新增"JLPT词书"按钮+下拉菜单(N1-N5)；新增"添加到本地"按钮
3. **JLPT 状态机制**：三态掌握系统 + 加权随机算法 + 进度条（灰/浅蓝/深蓝三色分区，百分数精确到小数点后一位）
4. **首页状态检测**：启动时显示本地词库+JLPT词书各5级的文件状态、词数、学习进度
5. 工作目录自动初始化：若 D:\JSS\JLPT\SortedJLPT 不存在，自动从嵌入源复制
6. 上次等级持久化：`last_level.txt` 记忆上次使用的 JLPT 等级

## 编译命令
```bash
cd D:/长江大学计算机实验室/计算机实验室文件/java/LanguageStudySystem/JavaJapStuSystem
javac -encoding UTF-8 -d . JapStuJFrame.java CardGridView.java JapEditAndDel.java JapImageRecognition.java JapJFrameKanaPrint.java Test.java
```

## 关键路径
- 本地词库：D:/JaStu.txt
- JLPT 源目录：JLPT/SortedJLPT/
- JLPT 工作目录：D:/JSS/JLPT/SortedJLPT/
- 编译产物：CodeProduct/
