# JSS v3.8.9 构建、打包与安装记录

- 日期：2026-09-28 22:56（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`（隔离工作树：`E:/SHIO/worktrees/jss-local-mastery-history-20260926`）
- 基线 HEAD：`49cd5833581d8ab6342aa1f5d154ffefbcd8f5d5`（`origin/main`）
- 工作分支：`fix/jss-progress-random-3.8.9`
- 版本：`3.8.9`
- 本次变更：本地进度按“现存词条＋历史累计掌握”显示，掌握段纳入当前批次待结算掌握词；JLPT 分组与本地临时组移除近期抽中加权，保留错题/状态权重，并跨重启避免连续重复。

## 编译与回归

- Java 编译器：JDK 24.0.2；生产源码和全部测试源码均按 UTF-8 编译到临时目录 `D:/SHIO_LANGUAGE/.planning/jss-3.8.9-20260928/`，未将临时 class 放入发布目录。
- `LocalTestEngineTest`、`LocalMasteryHistoryTest`、`LocalDataCodecTest`、`LocalTestStateCodecTest`、`JLPTGroupProgressStateTest`、`JLPTTestEngineTest`：PASS。
- `LocalStagingReadbackTest`：PASS，使用临时生成的 238 条 staging 夹具。
- `ExampleRubyLayoutTest`：PASS（无窗口几何检查）。
- 覆盖进度含待富化词、现存/历史掌握连续性、空词库、历史计数边界；覆盖本地/JLPT 不连续重复、唯一候选重复、上次词不可测、去除近期权重；覆盖本地 v2 状态读取、v3 上次抽题恢复、旧 JLPT 进度与旧分组行读取。
- JAR：`JapStudySystem/bin/JapanStudySystem.jar`，143,053 bytes，SHA-256 `6E6EA8436FB48758B2656E1E71A0223F0CF7B3B8B39C54EE1D07813D09632ABB`。
- Manifest 主类为 `LanguageStudySystem.JavaJapStuSystem.Test`；包含新增生产类；未包含 `*Test.class` 测试类；程序常量版本为 `3.8.9`。

## 发布载荷

- Inno Setup：6.7.1；编译退出码：0。
- 安装器：`JSS_history/JapaneseStudySystem_3.8.9_Setup.exe`，30,602,845 bytes，SHA-256 `57D837E51272241F5EC636642DFCB884CEDB6F4E4762391733AFDDC27B688FE1`。
- 运行时 ZIP：`JSS_history/JapStudySystemV3.8.9.zip`，32,013,553 bytes，SHA-256 `28D596FDF32EC5DE4536E257D6E30E3BC91F033B2D7A3DB252ACCC16D3521C87`。
- ZIP 检查：142 个条目，含 JAR、启动脚本、图标、README 和 Java 24.0.2 内置 JRE；不含源码、测试、临时 class、备份 JAR 或用户数据。
- `release/jss-setup.iss` 的 `AppVersion` 为 `3.8.9`，默认安装目录为 `D:\SHIO_LANGUAGE\jss`，`AppId` 保持不变。

## 安装后检查

- 安装目标：`D:/SHIO_LANGUAGE/jss`。
- 静默安装退出码：0。
- 安装后 JAR SHA-256 与本次构建产物一致；Manifest 主类、`3.8.9` 常量、启动脚本相对路径和内置 JRE 可执行检查均通过；内置 Java 报告版本 `24.0.2`。
- `D:/JaStu.txt` 安装前后 SHA-256 均为 `5FFC0CDDBB9A163A99AE523C6C4191B99D3342D4105C88DF66AC201522EA8075`。
- `D:/JSS/LocalTest/state.txt` 安装前后 SHA-256 均为 `7B30DF7F59C9B3DB2BD94997008A9294260173818154C369BFBA9224AF776D07`。
- 未启动安装后的 GUI，避免程序退出流程写回生产词库或本地测试状态。
