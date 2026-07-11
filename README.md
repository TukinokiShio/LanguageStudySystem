# LanguageStudySystem

多语言学习系统集合，基于 Java Swing 开发。当前包含**日语学习系统（JSS）**与**英语学习系统（ESS）**两套独立应用，各版本归档统一通过 Git LFS 管理。

---

## 项目结构

| 目录 | 说明 |
|---|---|
| `JapStudySystem/` | 日语学习系统（JSS）源码与编译产物 |
| `JSS_history/` | JSS 全部历史版本归档（zip / Setup 安装包 / 构建日志），经 Git LFS 管理 |
| `EnglishStudySystem/` | 英语学习系统（ESS）源码与编译产物 |
| `ESS_history/` | ESS 全部历史版本归档（zip / Setup 安装包 / 构建日志），经 Git LFS 管理 |
| `.gitattributes` | Git LFS 跟踪规则（`*.zip` / `*.exe`）|
| `.gitignore` | 忽略 jre-minimal、jdk-24、.class 等 |

---

## 日语学习系统（JSS）

- **最新版本**：V3.4.0
- **技术栈**：Java Swing GUI · JDK 24
- **启动方式**：运行 `JapStudySystem/run.bat`（或 `run.vbs`）启动；也可直接运行 `bin/JapanStudySystem.jar`
- **核心功能**
  - 卡片式浏览：以卡片样式展示全部单词与语法条目，支持滚动查看
  - 图片识别：通过 AI（DeepSeek）识别图片中的外语内容，批量解析并导入词库
  - JLPT 辞典系统：内置 N1–N5 级别日语辞典数据，支持分级词汇学习
  - 分组测试：基于掌握规则的智能抽查（JLPT 分组生成器 `GroupGenerator`）
  - 键盘快捷键：本地测试、JLPT、添加考察点全流程键盘操作（回车=显示答案/记得/继续，←/→/空格导航；添加考察点 Ctrl+回车确认、←/→ 选类型）
  - 掌握度统计：掌握 / 了解 / 陌生三色可视化
- **历史版本**：V1.3.5 · V1.5.4 · V1.5.5 · V2.0.3 · V3.0.5 · V3.1.1 · V3.2.0 · V3.3.2 · V3.4.0（详见 `JSS_history/`）

---

## 英语学习系统（ESS）

- **最新版本**：V2.2.0
- **技术栈**：Java Swing GUI · JDK 24
- **启动方式**：运行 `EnglishStudySystem/` 下启动脚本；或执行编译产物 JAR
- **核心功能**
  - 等级生成器（`EngLevelGenerator`）：按掌握率生成分级练习
  - 分组生成器（`EngGroupGenerator`）：按考试体系分组词汇
  - 分级词库：内置 CET4 / CET6 / IELTS / TOEFL 分组词汇数据
  - 词库切换：一键在「等级词库 / 本地词库」间切换查看
  - 本地词库测试、使用说明内置面板
  - 掌握度统计与智能复习
- **历史版本**：V2.1.0 · V2.1.1 · V2.1.5 · V2.2.0（详见 `ESS_history/`）

---

## 版本归档与 Git LFS

所有历史版本的压缩包与安装包（`.zip` / `.exe`）体积较大，已通过 **Git LFS** 管理。克隆本仓库后需先执行：

```bash
git lfs install
git lfs pull
```

以正确拉取 `JSS_history/` 与 `ESS_history/` 中的归档文件。

---

## 仓库地址

https://github.com/TukinokiShio/LanguageStudySystem
