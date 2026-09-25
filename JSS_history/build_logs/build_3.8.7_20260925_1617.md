# JSS v3.8.7 构建、打包与安装记录

- 日期：2026-09-25 16:17（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`
- 基线 HEAD：`525336c2b462a1a217cecef476060b9e25379c0b`
- 工作分支：`fix/jss-example-ellipsis-3.8.7`
- 版本：`3.8.7`
- 本次改动：修复答案页振假名例句在显示缩放下普通文字片段尾部显示省略号；同步版本号、回归测试和发布归档。

## 根因与回归

- Windows 显示缩放：125%（GraphicsConfiguration transform = 1.25）。旧实现创建窗口前按 FontMetrics 固定标签宽度；标签进入真实窗口后首选宽度增大，Swing 将末尾文本绘制为省略号。
- 红测：旧实现实屏标签宽 180 px、实现后首选宽 181 px，布局回归断言失败。
- 修复：普通片段 wrapper 在首选宽度上留 4 px，并在布局时重读已实现窗口中的标签首选尺寸。
- 绿测：`ExampleRubyLayoutTest` 几何与实屏两项通过；实屏截图：`D:/SHIO_LANGUAGE/.planning/2026-09-25-jss-example-ellipsis/answer-visible-final.png`。两条例句完整显示，振假名对齐保留。

## 构建与测试

- Java 编译：PASS，JDK 24.0.2，UTF-8，10 个生产源文件 + 3 个既有测试入口。
- 临时 classes：`D:/SHIO_LANGUAGE/.planning/2026-09-25-jss-example-ellipsis/release-classes-final`
- JAR：`JapStudySystem/bin/JapanStudySystem.jar`
- JAR 大小：141,033 bytes
- JAR SHA-256：`900F508B67303920F51AD7C191B048B7DCEE9EBAA29E62A504F0D60C08AFD78F`
- Manifest：`Main-Class: LanguageStudySystem.JavaJapStuSystem.Test`
- `ExampleRubyLayoutTest`：PASS（300 / 800 / 1146 px 几何布局；125% 实屏两句截图及标签余量）
- `LocalDataCodecTest`：PASS
- `LocalTestEngineTest`：PASS
- `LocalStagingReadbackTest`：PASS，独立合成 fixture 238 行；未读取或改写用户词库。

## Inno 打包

- 脚本：`release/jss-setup.iss`
- AppId：`{43063371-76E3-448C-81CE-415C4C209AEC}`
- 默认安装目录：`D:/SHIO_LANGUAGE/jss`
- 编译器：`E:/SHIO/inno/Inno Setup 6/ISCC.exe` 6.7.1
- ISCC 退出码：0
- 安装器：`JSS_history/JapaneseStudySystem_3.8.7_Setup.exe`
- 安装器大小：30,602,926 bytes
- 安装器 SHA-256：`8F3AA5FCDC077167A046C228B1BC3AE31CF3E0C37EFE6973F1D5BAD7BF47F7BE`

## 运行时 ZIP

- ZIP：`JSS_history/JapStudySystemV3.8.7.zip`
- ZIP 大小：32,011,733 bytes
- ZIP SHA-256：`A33CA00F5258C65DAD9D283BDAA78207C28B07B5820321539535AF342D6E6E2F`
- 载荷包含：`bin/JapanStudySystem.jar`、`run.bat`、`run.vbs`、`app.ico`、`README.md`、`jre-minimal`
- 载荷排除：源码、测试、临时 classes、备份 JAR、用户词库和测试状态。

## 安装后检查

- 安装目录：`D:/SHIO_LANGUAGE/jss`
- 安装退出码：0
- 安装后 JAR SHA-256 与构建产物一致：PASS
- 安装后 Manifest 主类：PASS
- 内置 JRE：`java version 24.0.2`，可执行：PASS
- `run.bat` / `run.vbs`：均绑定安装目录相对路径：PASS
- `D:/JaStu.txt` 安装前后 SHA-256 一致：`A13A057AF045A09FB9BC0C8C568F41C1291727E1E29E0EBA5EDAB5153EEFA479`
- 未启动安装后的完整 GUI，避免应用退出流程写回生产词库/测试状态；渲染修复使用独立 Swing fixture 实屏验证。
