# JSS v3.8.10 构建、打包与安装记录

- 日期：2026-09-28 23:16（Asia/Shanghai）
- 项目：`E:/SHIO/LanguageStudySystem`（隔离工作树：`E:/SHIO/worktrees/jss-local-mastery-history-20260926`）
- 基线 HEAD：`ff559af174e47a72a4c81eb06ec8164d243f80cd`（JSS v3.8.9）
- 工作分支：`fix/jss-progress-alignment-3.8.10`
- 版本：`3.8.10`
- 本次修复：分组行与词书/本地词库行使用相同的 72 像素标题列；两条进度条宽度均为 300 像素，且两行采用相同的左对齐 FlowLayout 和间距。

## 编译与布局核对

- Java 编译器：JDK 24.0.2；16 个生产源码文件按 UTF-8 编译成功，临时 classes 位于 `D:/SHIO_LANGUAGE/.planning/jss-3.8.10-20260928/classes/`。
- JAR：`JapStudySystem/bin/JapanStudySystem.jar`，143,115 bytes，SHA-256 `420AC1365EF45F843DCE4C89CC0B0BB057039081ADC9B22617D64D3EF3F605FF`。
- Manifest 主类保持 `LanguageStudySystem.JavaJapStuSystem.Test`；`JapStuJFrame` 中的程序版本常量为 `3.8.10`。
- 布局核对：两行的标题组件首列宽度均为 72；同一父面板和 FlowLayout 参数下，进度条具有相同水平起点。
- 本次仅修正静态布局参数；未新增或运行自动测试，也未启动 GUI 做像素截图检查。

## 发布载荷

- Inno Setup：6.7.1；编译退出码：0。
- 安装器：`JSS_history/JapaneseStudySystem_3.8.10_Setup.exe`，30,600,556 bytes，SHA-256 `DE1218F4CB0FF8694E19BAABFD93E3C3A7639A98B802A06605D7FCE846400286`。
- 运行时 ZIP：`JSS_history/JapStudySystemV3.8.10.zip`，32,013,659 bytes，SHA-256 `A96DB82A9F2303098CB495801EDF1575910FAED2373DE331892F640578EB1C6E`。
- ZIP 检查：142 个条目，含更新后的 JAR、启动脚本、图标、README 和 Java 24.0.2 内置 JRE；不含源码、测试、临时 class、备份 JAR 或用户数据。
- `release/jss-setup.iss` 的 `AppVersion` 为 `3.8.10`，默认安装目录为 `D:\SHIO_LANGUAGE\jss`，稳定 `AppId` 未变。

## 安装后检查

- 安装目标：`D:/SHIO_LANGUAGE/jss`。
- 静默安装退出码：0。
- 安装后 JAR SHA-256 与本次构建产物一致；Manifest 主类、程序版本常量、启动脚本相对路径和内置 JRE 可执行检查均通过；内置 Java 报告版本 `24.0.2`。
- `D:/JaStu.txt` 安装前后 SHA-256 均为 `5FFC0CDDBB9A163A99AE523C6C4191B99D3342D4105C88DF66AC201522EA8075`。
- `D:/JSS/LocalTest/state.txt` 安装前后 SHA-256 均为 `2DC4B144184E7FA5A162029C9654F9877AAE3C01FEF610AEE025303CF9D4F265`。
- 未启动安装后的 GUI，安装期间未发现运行中的 Java/JSS 进程。
