# JSS 3.8.11 构建记录

- 基线提交：`bc9f1bd74a05e4909b9dcd914c0ff440de3627fc`（V3.8.10）
- 版本：3.8.11
- 修改：缩短进度行标题列并减小横向间距；本地标题改为“词库”；累计总量使用“累计入库”文案。
- Java：JDK 24.0.2；`javac -encoding UTF-8` 编译 16 个 Java 源文件成功。
- JAR：`Main-Class` 为 `LanguageStudySystem.JavaJapStuSystem.Test`，入口类存在；SHA-256 `50860088D98C216BFC3C317DF284B0ED893D57DEE95B17CF7612B648AB19CEC8`。
- 自动化测试：未运行；本次仅调整 Swing 文案和布局参数。
- GUI 冒烟：未运行，避免启动程序读取生产词库；实际窗口效果未作截图复核。
- Inno Setup：6.7.1；编译成功，安装器 SHA-256 `1D950CED01FDFE515235008606E3AA5C25950507E117BF4D4065D8DFEC7F4744`。
- 运行时 ZIP：SHA-256 `6EDC7E5669B25ADF054258234796599424FDA626F998A5194B5EE5C364D02470`；包含主 JAR、启动脚本、图标、说明及内置 JRE，不含源码和测试目录。
- 安装：安装器退出码 0；目标 `D:\SHIO_LANGUAGE\jss`。安装后 JAR 哈希与构建产物一致，版本常量为 3.8.11，进度标题宽度为 36，主类、入口类、JRE 24.0.2、相对路径启动脚本、图标及说明文件均通过检查。
- 生产数据：未启动 GUI；安装后记录 `D:\JaStu.txt` SHA-256 `5FFC0CDDBB9A163A99AE523C6C4191B99D3342D4105C88DF66AC201522EA8075`，`D:\JSS\LocalTest\state.txt` SHA-256 `17A0740AA1AB1315CE6A129C952D347F7B7F63AA53528617A029010134AF052D`。安装器载荷只包含程序文件。
- 发布：待本记录提交后推送 `origin/main` 并核验远端。
