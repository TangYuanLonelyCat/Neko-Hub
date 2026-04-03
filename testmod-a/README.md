# 测试模组说明

## 模组介绍

### TestModA
- **模组 ID**: TestModA
- **版本**: 1.0.0
- **依赖**: 无
- **说明**: 基础测试模组，无任何依赖
- **命令**: `/test` - 测试模组间调用

### TestModB
- **模组 ID**: TestModB
- **版本**: 1.0.0
- **依赖**: TestModA 1.0.0+
- **说明**: 依赖 TestModA 的测试模组
- **公开方法**: `doComplete()` - 被 TestModA 调用

## 模组间调用示例

TestModA 注册了 `/test` 命令，执行流程：
1. TestModA 收到 `/test` 命令
2. TestModA 输出 "OK"
3. TestModA 调用 TestModB 的 `doComplete()` 方法
4. TestModB 返回 "complete"
5. TestModA 输出 "[TestModB] complete"

## 测试方法

### 测试场景 1：完整功能测试（依赖满足）

1. 构建两个模组：
   ```bash
   gradle :testmod-a:jar
   gradle :testmod-b:jar
   ```

2. 将 JAR 文件复制到 mods 目录：
   ```bash
   mkdir modloader/build/libs/mods
   cp testmod-a/build/libs/testmod-a-1.0.0.jar modloader/build/libs/mods/
   cp testmod-b/build/libs/testmod-b-1.0.0.jar modloader/build/libs/mods/
   ```

3. 运行 ModLoader：
   ```bash
   cd modloader/build/libs
   java -jar Neko-Hub-ModLoader-2.0.0.jar
   ```

4. 在控制台中应该看到：
   - TestModA 成功加载
   - TestModB 成功加载（显示依赖 TestModA）

5. 输入 `/test` 命令，应该看到：
   ```
   [TestModA] OK
   [TestModA] [TestModB] complete
   ```

### 测试场景 2：依赖不满足

1. 只复制 TestModB：
   ```bash
   cp testmod-b/build/libs/testmod-b-1.0.0.jar modloader/build/libs/mods/
   ```

2. 运行 ModLoader

3. 在控制台中应该看到错误：
   ```
   模组 [TestModB] 所需的依赖 [TestModA-1.0.0] 不存在或未加载
   ```

4. TestModB 应该被拒绝加载

### 测试场景 3：AutoBoot 依赖排序

1. 将两个模组都复制到 mods 目录

2. 运行 ModLoader 后执行 `/autoboot` 命令

3. 查看生成的 `auto.boot` 文件，应该看到：
   ```
   /load testmod-a-1.0.0.jar
   /load testmod-b-1.0.0.jar
   ```
   
   TestModA 在 TestModB 之前

## 项目结构

```
Neko-Hub/
├── testmod-a/
│   ├── build.gradle.kts
│   └── src/main/java/
│       └── net/lemoncookie/neko/modloader/testmod/a/
│           └── TestModA.java
├── testmod-b/
│   ├── build.gradle.kts
│   └── src/main/java/
│       └── net/lemoncookie/neko/modloader/testmod/b/
│           └── TestModB.java
└── settings.gradle.kts (已包含 testmod-a 和 testmod-b)
```

## 注意事项

1. **必须使用 Java 21**：测试模组和 ModLoader 都使用 Java 21
2. **依赖 ModLoader**：测试模组依赖 `:modloader` 项目
3. **Manifest 配置**：TestModB 的 manifest 中配置了依赖关系
4. **版本匹配**：TestModB 要求 TestModA 版本 >= 1.0.0

## 清理构建

```bash
gradle :testmod-a:clean
gradle :testmod-b:clean
```

或清理所有项目：

```bash
gradle clean
```
