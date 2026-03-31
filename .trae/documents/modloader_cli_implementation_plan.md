# Neko-Hub ModLoader CLI 实现计划

## 项目概述

实现 Neko-Hub 模组加载器的 CLI 部分，包括控制台界面、命令系统和模组注册API。

## 任务分解

### \[ ] Task 1: 重构 ModLoader 主类（Java 实现）

* **Priority**: P0

* **Depends On**: None

* **Description**:

  * 在 Java 代码中重新实现 ModLoader 主类

  * 负责初始化核心功能、加载模组和启动控制台

* **Success Criteria**:

  * ModLoader 能正确初始化并启动

  * 能检测和加载模组

  * 能启动控制台界面

* **Test Requirements**:

  * `programmatic` TR-1.1: ModLoader 初始化无异常

  * `programmatic` TR-1.2: 能正确检测到模组

* **Notes**: 核心功能使用 Java 21 实现以保证稳定性

### \[ ] Task 2: 实现控制台系统

* **Priority**: P0

* **Depends On**: Task 1

* **Description**:

  * 创建独立的控制台终端界面

  * 支持中文显示

  * 实现初始化显示内容

  * 实现交互提示符

* **Success Criteria**:

  * 控制台能正确显示初始化信息

  * 支持中文输入输出

  * 显示正确的用户提示符

* **Test Requirements**:

  * `programmatic` TR-2.1: 控制台启动无异常

  * `human-judgement` TR-2.2: 显示内容符合要求

* **Notes**: 需要处理不同操作系统的终端差异

### \[ ] Task 3: 实现命令系统

* **Priority**: P0

* **Depends On**: Task 2

* **Description**:

  * 创建 core.command 包

  * 实现命令解析器

  * 实现 /help 命令

  * 实现 /clear 命令

* **Success Criteria**:

  * 命令系统能正确解析斜杠命令

  * /help 命令显示命令列表

  * /clear 命令清空控制台

* **Test Requirements**:

  * `programmatic` TR-3.1: 命令解析无异常

  * `programmatic` TR-3.2: 命令执行正确

* **Notes**: 命令系统应支持扩展，便于添加更多命令

### \[ ] Task 4: 实现模组注册 API

* **Priority**: P0

* **Depends On**: Task 1

* **Description**:

  * 定义模组注册接口

  * 实现 API 版本检查

  * 实现模组信息收集

  * 实现模组加载流程

* **Success Criteria**:

  * 模组能正确注册

  * 低版本 API 模组被拒绝

  * 显示正确的错误信息

* **Test Requirements**:

  * `programmatic` TR-4.1: 模组注册无异常

  * `programmatic` TR-4.2: 版本检查正确

* **Notes**: API 版本检查逻辑要严格

### \[ ] Task 5: 实现模组检测和加载

* **Priority**: P0

* **Depends On**: Task 4

* **Description**:

  * 实现模组扫描机制

  * 实现模组加载计时

  * 显示加载结果

* **Success Criteria**:

  * 能正确检测到所有模组

  * 显示正确的加载时间

  * 显示模组数量

* **Test Requirements**:

  * `programmatic` TR-5.1: 模组检测无异常

  * `human-judgement` TR-5.2: 加载信息显示正确

* **Notes**: 加载过程要考虑性能和错误处理

### \[ ] Task 6: 添加 GUI 模块（JavaFX）

* **Priority**: P1

* **Depends On**: Task 1-5

* **Description**:

  * 创建 GUI 模块

  * 使用 JavaFX 实现窗口

  * 集成 CLI 功能

* **Success Criteria**:

  * GUI 窗口能正确启动

  * 显示与 CLI 相同的信息

  * 支持基本交互

* **Test Requirements**:

  * `programmatic` TR-6.1: GUI 启动无异常

  * `human-judgement` TR-6.2: 界面显示正确

* **Notes**: JavaFX 版本要与 Java 21 兼容

### \[ ] Task 7: 编写测试用例

* **Priority**: P2

* **Depends On**: Task 1-5

* **Description**:

  * 为核心功能编写单元测试

  * 为命令系统编写测试

  * 为模组加载编写测试

* **Success Criteria**:

  * 测试覆盖率达到 80% 以上

  * 所有测试通过

* **Test Requirements**:

  * `programmatic` TR-7.1: 测试执行无失败

  * `programmatic` TR-7.2: 覆盖率达到要求

* **Notes**: 测试要覆盖正常和异常场景

### \[ ] Task 8: 更新文档

* **Priority**: P2

* **Depends On**: Task 1-6

* **Description**:

  * 更新 API\_GUIDE.md

  * 更新 API\_GUIDE\_EN.md

  * 添加 CLI 使用说明

* **Success Criteria**:

  * 文档内容与实现一致

  * 包含所有新功能说明

  * 格式清晰易读

* **Test Requirements**:

  * `human-judgement` TR-8.1: 文档内容完整

  * `human-judgement` TR-8.2: 文档格式正确

* **Notes**: 文档要保持中英文同步

## 技术栈

* **Java**: 21 (LTS)

* **Kotlin**: 2.0.0 (用于非核心功能)

* **JavaFX**: 21 (用于 GUI 模块)

* **Gradle**: 8.7+ (构建工具)

## 实现顺序

1. Task 1: 重构 ModLoader 主类
2. Task 4: 实现模组注册 API
3. Task 5: 实现模组检测和加载
4. Task 2: 实现控制台系统
5. Task 3: 实现命令系统
6. Task 6: 添加 GUI 模块
7. Task 7: 编写测试用例
8. Task 8: 更新文档

## 预期交付物

* 完整的 ModLoader CLI 实现

* 命令系统（/help 和 /clear）

* 模组注册 API

* GUI 模块（JavaFX）

* 测试用例

* 更新后的文档

