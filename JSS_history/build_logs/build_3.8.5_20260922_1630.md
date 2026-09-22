# JSS v3.8.5 构建、打包与安装记录

- 日期：2026-09-22 16:30（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`
- 基线 HEAD：`566ecb1cf75285c68b3c4f389389c3e3550dd14b`
- 版本：`3.8.5`
- 本次改动：本地词库答案页布局与长内容滚动修订；查看答案增加 `←` 快捷键；快捷键提示同步。

## 构建与测试

- Java 编译：PASS，JDK 24.0.2，UTF-8，10 个生产源文件 + 3 个测试入口
- 临时 classes：`D:/JSS/work/jss_release_3.8.5_classes_20260922_1625`
- JAR：`JapStudySystem/bin/JapanStudySystem.jar`
- JAR 大小：140,758 bytes
- JAR SHA-256：`476D17362A71C16821AA211BCD19438B394EC45DB09D8AAF206304C42DAC2CBB`
- Manifest：`Main-Class: LanguageStudySystem.JavaJapStuSystem.Test`
- `LocalDataCodecTest`：PASS
- `LocalTestEngineTest`：PASS
- Swing 布局/快捷键探针：PASS，1200x850，答案页 contentHeight=371，外层滚动值=0，`stateAfterButton=3`，`stateAfterLeft=3`
- Swing 探针证据：`D:/JSS/work/jss_release_3.8.5_visual_evidence`

## Inno 打包

- 脚本：`release/jss-setup.iss`
- AppId：`{43063371-76E3-448C-81CE-415C4C209AEC}`
- 默认安装目录：`D:/SHIO_LANGUAGE/jss`
- 编译器：`E:/SHIO/inno/Inno Setup 6/ISCC.exe`
- ISCC 退出码：0
- 安装器：`JSS_history/JapaneseStudySystem_3.8.5_Setup.exe`
- 安装器大小：30,600,367 bytes
- 安装器 SHA-256：`B1E621A8144056824718B97D0B40817C6C3BDD949306A71D3010608B9EC809CC`

## 运行时 ZIP

- ZIP：`JSS_history/JapStudySystemV3.8.5.zip`
- ZIP 大小：32,011,432 bytes
- ZIP SHA-256：`5BDA8A482359892F91044C389D530856EFF5367A22136B5062B6141524F6E5BB`
- 载荷包含：`bin/JapanStudySystem.jar`、`run.bat`、`run.vbs`、`app.ico`、`README.md`、`jre-minimal`
- 载荷排除：源码、测试、临时 classes、备份 JAR、用户词库和测试状态

## 安装后检查

- 安装目录：`D:/SHIO_LANGUAGE/jss`
- 安装退出码：0
- 安装后 JAR SHA-256 与构建产物一致：PASS
- 安装后 Manifest 主类：PASS
- 内置 JRE：`java version 24.0.2`，可执行：PASS
- `run.bat` / `run.vbs`：均绑定安装目录相对路径：PASS
- 未启动安装后的 GUI，避免写入生产词库/测试状态。

## 发布

- 本次只提交 JSS 版本源文件、构建 JAR、3.8.5 历史归档、构建日志、README、历史索引和 Inno 脚本。
- 工作区中其他未跟踪的上一轮验证证据、项目协作文件和备份 JAR 保持原样，不纳入本次提交。
