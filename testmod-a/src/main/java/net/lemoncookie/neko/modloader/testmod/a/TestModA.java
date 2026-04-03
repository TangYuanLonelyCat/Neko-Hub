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
        // 无依赖
        return Collections.emptyList();
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        modLoader.getConsole().printSuccess("[TestModA] Loaded successfully!");
        modLoader.getBroadcastManager().broadcast("Hub.Console", "[TestModA] Hello from TestModA!", "TestModA");
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
                modLoader.getConsole().printLine("[TestModA] OK");
                
                // 第二步：尝试调用 TestModB 的函数
                try {
                    TestModB testModB = getTestModB(modLoader);
                    if (testModB != null) {
                        String result = testModB.doComplete();
                        modLoader.getConsole().printLine("[TestModA] " + result);
                    } else {
                        modLoader.getConsole().printWarning("[TestModA] TestModB not found");
                    }
                } catch (Exception e) {
                    modLoader.getConsole().printError("[TestModA] Error calling TestModB: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取 TestModB 实例
     */
    private TestModB getTestModB(ModLoader modLoader) {
        // 通过反射或其他方式获取 TestModB 实例
        // 这里简化处理，假设 TestModB 已经注册到 ModLoader
        for (IModAPI mod : modLoader.getJavaMods()) {
            if (mod.getModId().equals("TestModB") && mod instanceof TestModB) {
                return (TestModB) mod;
            }
        }
        return null;
    }
}
