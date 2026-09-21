# JSS v3.8.3 构建与发布记录

- 日期：2026-09-21
- 基线：`c976d22 chore(JSS): 归档 v3.8.2 安装器`
- 目标：本地测试结算页保留单词与释义，移除例句/例句译文重复展示，增加正确率与达到下一状态所需答对次数。

## 修改

- `JapStuJFrame.java`
  - 答题前显示累计考察、正确次数、正确率、本组错误、当前状态和掌握规则。
  - 结算时清空显示答案阶段的完整例句内容。
  - 结算页仅渲染单词/语法点与释义，并显示统计、正确率、状态推进和保留规则。
  - JLPT 结果页继续使用完整词汇信息渲染。
- `LocalTestEngine.java`
  - 增加当前状态到下一状态的剩余正确次数计算。
- `LocalTestEngineTest.java`
  - 增加陌生/了解/掌握及错误加权后的剩余次数测试。
- `README.md`、`archive_index.txt`
  - 更新到 v3.8.3。

## 验证

- Java 编译：通过，临时 classes：`D:/JSS/work/jss_runtime_classes_20260921_0100`
- `LocalDataCodecTest`：PASS
- `LocalTestEngineTest`：PASS
- `LocalStagingReadbackTest`：PASS，238 行
- 本地结果显示静态契约检查：PASS
- `D:/JaStu.txt`：恢复后 238 行、12 列，SHA-256 与冒烟前备份一致
- 状态文件：恢复后 SHA-256 与冒烟前备份一致
- JAR Manifest：`Main-Class: LanguageStudySystem.JavaJapStuSystem.Test`
- `jar --validate --file`：PASS

## 构建产物

- JAR：`D:/JSS/work/JapanStudySystem_v3.8.3_20260921_0100.jar`
  - SHA-256：`6E9211CA75B59949BE3C6B4E47DD6001FFB1DDCD42B4CF2DEEFC4B4131951E86`
- 仓库 JAR：`JapStudySystem/bin/JapanStudySystem.jar`
  - SHA-256 与构建 JAR 一致
- 安装包：`C:/JSS/日语学习系统_3.8.3_Setup.exe`
  - Inno Setup 编译退出码：0
  - SHA-256：`3C082B7F96EDD4D7C5B777621353848748CF1BA5477F1D3C551EACB31FF661E4`
- 运行时 ZIP：`JSS_history/JapStudySystemV3.8.3.zip`
  - SHA-256：`BBC1A70209CDC13DA41AFBA54618AAED7843680D4C71A39D2C5C3AEB419FD800`

## 安装后检查

- 安装目录：`D:/SHIO_LANGUAGE/jss`
- 安装退出码：0
- 安装后 JAR 与构建 JAR SHA-256 一致
- 内置 JRE：Java 24.0.2，可执行
- 使用安装后的 JAR 执行本地数据、临时组和 staging 回读测试：全部通过

## GUI 说明

本轮按用户要求改用后台验证。曾启动 v3.8.3 窗口确认进程标题，但自动化环境中的窗口位于不可见桌面坐标，未完成可视化截图验收；源码显示契约、编译、规则测试和安装后测试均已通过。冒烟启动造成的本地词库/状态文件变化已用启动前备份恢复，并完成哈希复核。
