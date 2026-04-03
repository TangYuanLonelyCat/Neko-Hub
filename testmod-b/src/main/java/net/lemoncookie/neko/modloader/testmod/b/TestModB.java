package net.lemoncookie.neko.modloader.testmod.b;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;

import java.util.Arrays;
import java.util.List;

/**
 * 测试模组 B - 依赖 TestModA
 */
public class TestModB implements IModAPI {
    
    private ModLoader modLoader;
    
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
        return "net.lemoncookie.neko.modloader.testmod.b";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        // 依赖 TestModA 1.0.0 或更高版本
        return Arrays.asList(new ModDependency("TestModA", "1.0.0"));
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        modLoader.getConsole().printSuccess("[TestModB] Loaded successfully! (Depends on TestModA)");
        modLoader.getBroadcastManager().broadcast("Hub.Console", "[TestModB] Hello from TestModB! I depend on TestModA.", "TestModB");
    }
    
    @Override
    public void onUnload() {
        modLoader.getConsole().printLine("[TestModB] Unloaded!");
    }
    
    @Override
    public String getName() {
        return "TestModB - Dependent Module";
    }
    
    /**
     * 被 TestModA 调用的函数
     * @return 完成消息
     */
    public String doComplete() {
        return "[TestModB] complete";
    }
}
