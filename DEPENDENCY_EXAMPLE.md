# 模组依赖系统使用示例

## 创建带依赖的模组

### 示例 1：无依赖的基础模组 (TestModA)

```java
package net.lemoncookie.neko.modloader.test;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;

import java.util.Collections;
import java.util.List;

public class TestModA implements IModAPI {
    
    @Override
    public String getModId() {
        return "TestModA";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.test";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        // 无依赖
        return Collections.emptyList();
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        modLoader.getConsole().printSuccess("[TestModA] Loaded successfully!");
    }
    
    @Override
    public void onUnload() {
        modLoader.getConsole().printLine("[TestModA] Unloaded!");
    }
}
```

### 示例 2：依赖其他模组的模组 (TestModB)

```java
package net.lemoncookie.neko.modloader.test;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;

import java.util.Arrays;
import java.util.List;

public class TestModB implements IModAPI {
    
    @Override
    public String getModId() {
        return "TestModB";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.test";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        // 依赖 TestModA 1.0.0 或更高版本
        return Arrays.asList(new ModDependency("TestModA", "1.0.0"));
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        modLoader.getConsole().printSuccess("[TestModB] Loaded successfully! (Depends on TestModA)");
    }
    
    @Override
    public void onUnload() {
        modLoader.getConsole().printLine("[TestModB] Unloaded!");
    }
}
```

## 打包模组

### 方法 1：使用 Gradle 打包

1. 创建独立的模组项目

2. 在 `build.gradle.kts` 中添加：

```kotlin
plugins {
    java
}

group = "net.lemoncookie.neko.modloader.test"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    manifest {
        attributes(
            "Mod-Id" to "TestModA",
            "Mod-Version" to "1.0.0",
            "Mod-Dependencies" to "" // 无依赖留空，或填写 "TestModA:1.0.0,OtherMod:2.0.0"
        )
    }
}
```

3. 执行打包：
```bash
gradle jar
```

4. 将生成的 JAR 文件放到 `mods/` 目录

### 方法 2：手动打包

1. 编译 Java 文件：
```bash
javac -cp "Neko-Hub-ModLoader.jar" TestModA.java
```

2. 创建 manifest 文件 `MANIFEST.MF`：
```
Manifest-Version: 1.0
Mod-Id: TestModA
Mod-Version: 1.0.0
Mod-Dependencies: 

```

3. 打包 JAR：
```bash
jar cfm TestModA.jar MANIFEST.MF net/lemoncookie/neko/modloader/test/*.class
```

4. 将 JAR 文件放到 `mods/` 目录

## 测试依赖系统

### 测试场景 1：依赖满足

1. 将 TestModA.jar 和 TestModB.jar 放到 mods 目录
2. 运行 ModLoader
3. 执行 `/autoboot` 生成 auto.boot
4. 查看生成的 auto.boot 文件，TestModA 应该在 TestModB 之前
5. 重启 ModLoader，两个模组都应该成功加载

### 测试场景 2：依赖不满足

1. 只将 TestModB.jar 放到 mods 目录（不放置 TestModA.jar）
2. 运行 ModLoader
3. TestModB 应该被拒绝加载
4. 控制台应该显示错误：`模组 [TestModB] 所需的依赖 [TestModA-1.0.0] 不存在或未加载`

### 测试场景 3：版本不满足

1. 放置 TestModA 0.5.0 版本和 TestModB 1.0.0 版本
2. TestModB 依赖 TestModA 1.0.0
3. TestModB 应该被拒绝加载
4. 控制台显示版本错误

### 测试场景 4：循环依赖

1. 创建 TestModC 依赖 TestModD
2. 创建 TestModD 依赖 TestModC
3. 执行 `/autoboot`
4. 应该报错：`Circular dependency detected involving mod: TestModC`

## Manifest 属性说明

JAR 文件的 `MANIFEST.MF` 可以包含以下模组相关属性：

| 属性名 | 必需 | 说明 | 示例 |
|--------|------|------|------|
| Mod-Id | 否 | 模组 ID，不填则使用文件名 | TestModA |
| Mod-Version | 否 | 模组版本 | 1.0.0 |
| Mod-Dependencies | 否 | 依赖列表，格式：`modId1:version1,modId2:version2` | TestModA:1.0.0,OtherMod:2.0.0 |

## 注意事项

1. **模组加载顺序**：使用 `/autoboot` 生成的 auto.boot 会自动按依赖关系排序
2. **手动加载**：使用 `/load` 命令手动加载时，如果依赖不满足会拒绝加载
3. **循环依赖**：系统会检测并报错，需要避免循环依赖
4. **版本格式**：推荐使用语义化版本（major.minor.patch）
