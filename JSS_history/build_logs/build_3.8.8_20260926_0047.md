# JSS v3.8.8 构建、打包与安装记录

- 日期：2026-09-26 00:47（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`（隔离工作树：`E:/SHIO/worktrees/jss-local-mastery-history-20260926`）
- 基线 HEAD：`475749a551454bec5f85cccf18a8ff388bb358ca`
- 工作分支：`fix/local-mastery-history-3.8.8`
- 版本：`3.8.8`
- 本次修复：保留词库文件头中的历史掌握数；本地条目成功达到掌握条件并从临时组移除时累计一次，不再从会清零的临时组状态重算历史值。

## 编译与回归

- Java 编译器：JDK 24.0.2，源码按 UTF-8 编译；11 个生产源码文件、5 个测试源码文件均编译成功。
- `LocalMasteryHistoryTest`：PASS（文件头计数往返、缺失/无效计数、掌握结算与困难词边界）。
- `LocalDataCodecTest`：PASS。
- `LocalTestEngineTest`：PASS。
- 临时 classes 与构建输入位于 `D:/SHIO_LANGUAGE/.planning/2026-09-26-jss-local-mastery-history-3.8.8`，未放入发布载荷。
- 新 JAR：`JapStudySystem/bin/JapanStudySystem.jar`，135,962 bytes，SHA-256 `23665C446CF3786FA4628DEED4884E1BEE0CC1BEDA843458D06E179D3825FE60`。
- Manifest 主类：`LanguageStudySystem.JavaJapStuSystem.Test`；主类及新增计数类均存在于 JAR。
- 与 3.8.7 JAR 逐项比较 class 条目：所有生产类均保留；新 JAR 新增 `LocalMasteryHistory.class`，并去掉旧包中误带的 3 个 `*Test.class`。

## 发布载荷

- 安装脚本：`release/jss-setup.iss`；AppId 保持 `{43063371-76E3-448C-81CE-415C4C209AEC}`，默认目录为 `D:/SHIO_LANGUAGE/jss`。
- Inno Setup：6.7.1；编译退出码：0。
- 安装器：`JSS_history/JapaneseStudySystem_3.8.8_Setup.exe`，30,594,504 bytes，SHA-256 `171FB1A648F5C2F465BB465D253C0CF7574E76EE6BD418E34B3B520E965A22DF`。
- 运行时 ZIP：`JSS_history/JapStudySystemV3.8.8.zip`，32,007,005 bytes，SHA-256 `0031B6D35A7664BDB1A9A7009AC0C0942EB33755D6FF5E052CA112B1FE3E2B01`。
- ZIP 检查：142 个条目，包含 JAR、启动脚本、图标、说明和内置 JRE；不含用户词库、测试状态、源码、class 或备份文件。内置 JRE 为 Java 24.0.2。

## 安装后检查

- 安装目标：`D:/SHIO_LANGUAGE/jss`。
- 安装退出码：0。
- 安装后 JAR SHA-256 与构建产物一致；Manifest 主类、启动脚本相对路径和内置 JRE 可执行检查均通过。
- `D:/JaStu.txt` 安装前后 SHA-256 均为 `CFA50ACB7CBCFD6A6618F2B75C7329D1D650A1705240304B140C4DF2EDC6B8F2`。
- `D:/JSS/LocalTest/state.txt` 安装前后 SHA-256 均为 `62975540A11E03DF04100B25DF71279A21E30F8D9A6946A0D44053DA426440E9`。
- 未启动安装后的 GUI，避免程序退出流程写回生产词库或本地测试状态。
