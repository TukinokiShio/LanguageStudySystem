# JSS v3.8.6 构建、打包与安装记录

- 日期：2026-09-22 17:45（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`
- 基线 HEAD：`6b99e21031733bd57e875ad736b7bdf2e603fb10`
- 版本：`3.8.6`
- 本次改动：本地词库答案页标题与表格保持紧邻，表格行锁定内容自然高度；递增补丁版本并发布安装包。

## 构建与测试

- Java 编译：PASS，JDK 24.0.2，UTF-8，10 个生产源文件 + 3 个测试入口
- 临时 classes：`D:/SHIO_LANGUAGE/jss-release-3.8.6/classes`
- JAR：`JapStudySystem/bin/JapanStudySystem.jar`
- JAR 大小：140,860 bytes
- JAR SHA-256：`E130F383CA27D58E685844FD1CAB206C4FA41C91ADFE6CC25A02B579802DC5B3`
- Manifest：`Main-Class: LanguageStudySystem.JavaJapStuSystem.Test`
- `LocalDataCodecTest`：PASS
- `LocalTestEngineTest`：PASS
- Swing 答案页布局/快捷键探针：沿用提交 `6b99e21` 的已验证结果，标题与表格间距为 0，左方向键可返回上一题。
- `LocalStagingReadbackTest`：本轮未执行，缺少独立 staging fixture；不影响本次 UI 发布载荷。

## Inno 打包

- 脚本：`release/jss-setup.iss`
- AppId：`{43063371-76E3-448C-81CE-415C4C209AEC}`
- 默认安装目录：`D:/SHIO_LANGUAGE/jss`
- 编译器：`E:/SHIO/inno/Inno Setup 6/ISCC.exe` 6.7.1
- ISCC 退出码：0
- 安装器：`artifacts/release/output/JapaneseStudySystem_3.8.6_Setup.exe`
- 安装器大小：30,603,047 bytes
- 安装器 SHA-256：`BA95351F2A6FD0A6D2F567620CD7376DE7FC1196A88CD8AFBAC57F0FED581A71`

## 运行时 ZIP

- ZIP：`JSS_history/JapStudySystemV3.8.6.zip`
- ZIP 大小：32,011,487 bytes
- ZIP SHA-256：`49609DD1800C76A85D8F08BB74F91C0A00084F627B5CCB5EEE2638F05BF09C73`
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

- 本次仅提交 JSS v3.8.6 源文件、构建 JAR、历史归档、构建日志、README、历史索引和 Inno 脚本。
- 工作区中其他未跟踪的项目协作文件、验证证据和备份 JAR 保持原样，不纳入本次提交。
