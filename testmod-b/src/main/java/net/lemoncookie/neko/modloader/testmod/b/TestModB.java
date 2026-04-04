package net.lemoncookie.neko.modloader.testmod.b;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;

import java.util.Collections;
import java.util.List;

/**
 * 测试模组 B - 被 TestModA 依赖的基础模组
 */
public class TestModB implements IModAPI {
    
    private ModLoader modLoader;
    
    @Override
    public String getModId() {
        return "TestModB";
    }
    
    @Override
    public String getVersion() {
        return "1.1.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.testmod.b";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        // 无依赖
        return Collections.emptyList();
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        modLoader.getBroadcastManager().broadcast("Hub.Console", "Loaded successfully!", "TestModB");
        modLoader.getBroadcastManager().broadcast("Hub.Console", "Hello from TestModB!", "TestModB");
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
