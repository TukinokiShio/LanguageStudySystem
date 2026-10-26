# JSS 3.10.0 构建记录

- 基线提交：`237331b6026b73b38f175cdcd6f19bc43da75923`（V3.9.1，origin/main）
- 版本：3.10.0
- 日期：2026-10-26
- 修改：临时组必含最近追加的5条可测词，其余按 localId 顺序分为前70%和后30%并各自无放回抽取最多5条；组内改为均匀随机，先覆盖未出题陌生词，再练习未完成了解词。困难词错超过3次后状态封顶为了解，继续答到 `2 + floor(本组错误数 / 2)` 次正确后仅完成本组并保留，不归档；正常掌握词仍归档并移除。
- 数据兼容：未增加词条列、状态字段或新状态文件版本；本地词条仍为12列，临时组状态仍用既有 `slots`、`hardIds`、`lastAskedId` 格式，继续读取 v2 状态文件。读取时跳过旧版本写入的非正槽位 ID，并保留其他有效槽位与 `hardIds`。
- Java：JDK 24.0.2；使用 UTF-8 编译17个生产 Java 源文件。源码和测试共26个 Java 文件编译成功。
- 自动测试：7项纯测试入口全部通过：`LocalTestEngineTest`、`LocalTestStateCodecTest`、`LocalDataCodecTest`、`JLPTTestEngineTest`、`JLPTGroupProgressStateTest`、`LocalMasteryArchiveTest`、`LocalMasteryHistoryTest`。未运行会显示 GUI 的 `ExampleRubyLayoutTest`；`LocalStagingReadbackTest` 缺少 staging 文件参数。测试未访问生产词库或状态文件。
- JAR：`Main-Class` 为 `LanguageStudySystem.JavaJapStuSystem.Test`；入口类、`LocalTestEngine` 和 Manifest 均存在；`javap` 核实窗口版本常量为 `3.10.0`。大小 146,175 bytes，SHA-256 `740B39096196690892233159BA2A860EF1921FB508794E4C4DC018A1BBE215D7`。
- Inno Setup：6.7.1；编译退出码 0。安装器 `JapaneseStudySystem_3.10.0_Setup.exe`，大小 30,605,169 bytes，SHA-256 `4B8FFA81886B0AAD0674F0AA69FD97E72CBCB1B798177651DD0B6577BF9AC471`。`AppVersion=3.10.0`、稳定 AppId `{43063371-76E3-448C-81CE-415C4C209AEC}` 和默认目录 `D:\SHIO_LANGUAGE\jss` 已核对。
- 运行时 ZIP：`JapStudySystemV3.10.0.zip`，大小 32,017,483 bytes，SHA-256 `46DCE3CF19679550899A4A7FA1E9486A590A684E21BC08F07DC09DEE2A939B7B`；含 JAR、启动脚本、图标、说明和136个内置 JRE 文件，共141个 ZIP 项；不含源码、测试或用户词库。
- 安装：静默安装退出码 0；目标 `D:\SHIO_LANGUAGE\jss`。卸载注册信息显示版本3.10.0；安装后 JAR 与构建产物 SHA-256 一致，`Main-Class`、启动脚本相对路径及内置 Java 24.0.2 均通过检查。安装前后 `D:\JaStu.txt`、`D:\JSS\LocalTest\state.txt` 和 `D:\JaStu_mastered.txt` 的 SHA-256 未变化。
- GUI 冒烟：未运行，避免启动程序读取或写回生产词库。
