package net.lemoncookie.neko.modloader.testmod.a;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.command.Command;

import java.util.Collections;
import java.util.List;

/**
 * 测试模组 A - 无依赖的基础模组
 */
public class TestModA implements IModAPI {
    
    private ModLoader modLoader;
    
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
        return "net.lemoncookie.neko.modloader.testmod.a";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        // 依赖 TestModB 1.0.0 或更高版本
        return Collections.singletonList(new ModDependency("TestModB", "1.0.0"));
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        modLoader.getBroadcastManager().broadcast("Hub.Console", "Loaded successfully!", "TestModA");
        modLoader.getBroadcastManager().broadcast("Hub.Console", "Hello from TestModA!", "TestModA");
    }
    
    @Override
    public void onUnload() {
        modLoader.getConsole().printLine("[TestModA] Unloaded!");
    }
    
    @Override
    public String getName() {
        return "TestModA - Base Module";
    }
    
    @Override
    public void registerCommands(ModLoader modLoader) {
        // 注册 /test 命令
        modLoader.getCommandSystem().registerCommand("test", new Command() {
            @Override
            public void execute(ModLoader modLoader, String args) {
                // 第一步：返回 OK
                modLoader.getConsole().printLine("OK");
                modLoader.getBroadcastManager().broadcast("Hub.Log", "OK", "TestModA");
                
                // 第二步：尝试调用 TestModB 的函数
                try {
                    Object testModB = getTestModB(modLoader);
                    if (testModB != null) {
                        // 使用反射调用 doComplete 方法，避免编译时依赖
                        java.lang.reflect.Method method = testModB.getClass().getMethod("doComplete");
                        String result = (String) method.invoke(testModB);
                        modLoader.getConsole().printLine(result);
                        modLoader.getBroadcastManager().broadcast("Hub.Log", result, "TestModA");
                    } else {
                        modLoader.getConsole().printWarning("TestModB not found");
                        modLoader.getBroadcastManager().broadcast("Hub.Log", "TestModB not found", "TestModA");
                    }
                } catch (Exception e) {
                    modLoader.getConsole().printError("Error calling TestModB: " + e.getMessage());
                    modLoader.getBroadcastManager().broadcast("Hub.Log", "Error calling TestModB: " + e.getMessage(), "TestModA");
                }
            }
            
            @Override
            public String getDescription() {
                return "Test command for inter-mod communication";
            }
            
            @Override
            public String getUsage() {
                return "/test";
            }
        });
    }
    
    /**
     * 获取 TestModB 实例
     */
    private Object getTestModB(ModLoader modLoader) {
        // 通过反射或其他方式获取 TestModB 实例
        // 这里简化处理，假设 TestModB 已经注册到 ModLoader
        for (IModAPI mod : modLoader.getJavaMods()) {
            if (mod.getModId().equals("TestModB")) {
                return mod;
            }
        }
        return null;
    }
}
