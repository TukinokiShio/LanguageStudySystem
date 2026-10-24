# JSS 3.9.0 构建记录

- 基线提交：`de493e051317c7c5a277f32c66871eb66de481e8`（V3.8.11）
- 版本：3.9.0
- 日期：2026-10-24
- 修改：本地临时组结算时，将达到掌握且本组错误不超过 3 次的完整词条追加到独立 UTF-8 TSV 档案 `D:/JaStu_mastered.txt`，然后从活动词库移除；归档失败时保留词条并允许重试。旧累计掌握数及 12 列活动词库格式保持兼容。
- Java：JDK 24.0.2；`javac -encoding UTF-8` 编译 17 个 Java 源文件与 9 个测试类成功。
- 自动化测试：9 项通过：`ExampleRubyLayoutTest`、`JLPTGroupProgressStateTest`、`JLPTTestEngineTest`、`LocalDataCodecTest`、`LocalMasteryArchiveTest`、`LocalMasteryHistoryTest`、`LocalStagingReadbackTest`（238 行）、`LocalTestEngineTest`、`LocalTestStateCodecTest`。
- JAR：`Main-Class` 为 `LanguageStudySystem.JavaJapStuSystem.Test`，入口类与 `LocalMasteryArchive.class` 存在；SHA-256 `5304D34C7271CEE4D798CA4327EB70716A0254114CEA9CD00133FEFEE11CA046`。
- Inno Setup：6.7.1；编译成功，退出码 0；安装器 `JapaneseStudySystem_3.9.0_Setup.exe`，大小 30,603,555 bytes，SHA-256 `70F61DB62890554902D106E0FC099E828D51C853DC3B6CAEBFFA3AD8DD4D3E3E`。`AppVersion=3.9.0`、稳定 AppId `{43063371-76E3-448C-81CE-415C4C209AEC}` 和默认目录 `D:\SHIO_LANGUAGE\jss` 均已核对。
- 运行时 ZIP：`JapStudySystemV3.9.0.zip`，大小 32,016,032 bytes，SHA-256 `FAFFE8C59ADCEF4378D671A5E8C2CECCB492A3B7BFDAE4DBFE25AF4A19374B50`；包含 JAR、启动脚本、图标、说明和内置 JRE；不含源码、测试或用户数据。
- 安装：安装器静默安装退出码 0；目标 `D:\SHIO_LANGUAGE\jss`。安装后 JAR 与构建产物 SHA-256 一致；Manifest 主类、归档类、`run.bat`、`run.vbs`、图标、说明和内置 JRE 均通过静态检查；内置 Java 为 24.0.2，启动脚本使用安装目录内的 JRE 与 JAR。
- GUI 冒烟：未运行，避免启动程序读取或写回生产词库。测试中的归档文件使用隔离临时路径；生产档案文件尚未创建，应用首次成功结算掌握词时会生成。
- 发布：提交与推送待完成。
