# JSS 3.9.1 构建记录

- 基线提交：`8d8f89f87200155e2c6a4595c8748f262b4d65b2`（V3.9.0）
- 版本：3.9.1
- 日期：2026-10-24
- 修改：将窗口标题使用的 `APP_VERSION` 从遗留的 `3.8.11` 更新为 `3.9.1`，与安装器和发布版本一致。
- Java：JDK 24.0.2；`javac -encoding UTF-8` 编译 17 个 Java 源文件与 9 个测试类成功。
- 自动化测试：9 项通过：`ExampleRubyLayoutTest`、`JLPTGroupProgressStateTest`、`JLPTTestEngineTest`、`LocalDataCodecTest`、`LocalMasteryArchiveTest`、`LocalMasteryHistoryTest`、`LocalStagingReadbackTest`（238 行）、`LocalTestEngineTest`、`LocalTestStateCodecTest`。
- JAR：`Main-Class` 为 `LanguageStudySystem.JavaJapStuSystem.Test`；`javap -verbose` 确认窗口标题常量为 `V3.9.1`，不再包含 `V3.8.11`。SHA-256 `16D18CFB02371EFF4DC3DED2D7D7DDC5030296C7F87B7A971C521ACA700C08D2`。
- Inno Setup：编译成功，退出码 0；安装器 `JapaneseStudySystem_3.9.1_Setup.exe`，大小 30,604,480 bytes，SHA-256 `1AD8C736BF8FE8AAF0165022C5FECD3A555D6C99604D04FA4DDEB16B7739E1A9`。`AppVersion=3.9.1`、稳定 AppId `{43063371-76E3-448C-81CE-415C4C209AEC}` 和默认目录 `D:\SHIO_LANGUAGE\jss` 已核对。
- 运行时 ZIP：`JapStudySystemV3.9.1.zip`，大小 32,016,054 bytes，SHA-256 `6EAFC799B28EEFC86D30EFF19E4A601E8E152495417A4A17E064A39CBC53CFCB`；包含 JAR、启动脚本、图标、说明和内置 JRE，不含源码、测试或用户词库。
- 安装：静默安装退出码 0；目标 `D:\SHIO_LANGUAGE\jss`。安装器卸载注册信息显示版本 3.9.1；安装后 JAR 与构建产物 SHA-256 一致，窗口标题常量为 `V3.9.1`。
- GUI 冒烟：未运行，避免启动程序读取或写回生产词库。实际窗口标题由已安装 JAR 中经 `javap` 核验的常量生成。
