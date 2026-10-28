# JSS 3.10.1 构建记录

- 基线提交：`2dfd4ede012d547d0d5e5e9b8ac1109bc8c5ac71`（V3.10.0，`origin/main`）
- 版本：3.10.1
- 日期：2026-10-28
- 修改：已富化的有效本地词条按 `localId` 顺序分为前60%、中间30%、后10%，每段无放回随机抽取最多5条；15条及以下仍全部纳入，分段不足时不跨段补位。组内抽题算法和本地状态格式未变。
- Java：JDK 24.0.2；17个生产 Java 源文件和9个测试 Java 源文件共26个文件，以 UTF-8 编译成功。生产 JAR 只包含生产类。
- 自动测试：`LocalTestEngineTest`、`LocalTestStateCodecTest`、`LocalDataCodecTest`、`JLPTTestEngineTest`、`JLPTGroupProgressStateTest`、`LocalMasteryArchiveTest`、`LocalMasteryHistoryTest` 共7项通过。覆盖15条全部纳入、16条按9/5/2分段且不跨段补位、100条按60/30/10各抽5条、富化筛选、有效/重复 `localId` 与组内抽题规则。
- JAR：`Main-Class` 为 `LanguageStudySystem.JavaJapStuSystem.Test`；入口类和 `LocalTestEngine` 存在，版本常量为 `3.10.1`。共38个归档项、146,116 bytes；SHA-256 `821BFD6B8CF7B04A1C58B6A630A24768EAA44A79C9CDD28D3B193F1501185871`。
- Inno Setup：6.7.1；编译退出码 0。安装器 `JapaneseStudySystem_3.10.1_Setup.exe`，30,604,585 bytes；SHA-256 `FB437B90BBDDCC908B228D768A8A4D0DFF7CD722CFD6547E4B195EB298572568`。`AppVersion=3.10.1`、稳定 AppId `{43063371-76E3-448C-81CE-415C4C209AEC}` 和默认目录 `D:\SHIO_LANGUAGE\jss` 已核对。
- 运行时 ZIP：`JapStudySystemV3.10.1.zip`，32,017,578 bytes；SHA-256 `36B97EB21AC494D9E420C76B1FAF91774AB538ED715CFE8703049C0B87898E25`。共141项，与3.10.0载荷路径一致，包含更新后的 JAR、启动脚本、说明、图标和内置 JRE；不含源码、测试类、备份 JAR 或用户词库。
- 安装：静默安装退出码 0；目标 `D:\SHIO_LANGUAGE\jss`。卸载注册版本显示3.10.1；安装后 JAR 与构建产物 SHA-256 一致，`Main-Class`、应用版本常量、启动文件和内置 Java 24.0.2 均通过检查。安装前后 `D:\JaStu.txt`、`D:\JSS\LocalTest\state.txt` 和 `D:\JaStu_mastered.txt` 的 SHA-256 未变化。
- GUI 冒烟：未运行，避免启动程序读取或写回生产词库。
