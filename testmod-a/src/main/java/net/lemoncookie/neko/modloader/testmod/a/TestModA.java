package net.lemoncookie.neko.modloader.testmod.a;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.command.BaseCommandListener;
import net.lemoncookie.neko.modloader.command.CommandMessage;

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
        return "2.0.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.testmod.a";
    }
    
    @Override
    public List<ModDependency> getDependencies() {
        return Collections.emptyList();
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
    public void registerBroadcastListeners(ModLoader modLoader, String modId) {
        modLoader.getBroadcastManager().listen(
            BroadcastManager.HUB_COMMAND,
            new BaseCommandListener(modLoader, "test") {
                @Override
                protected void execute(CommandMessage commandMessage, String senderModId) {
                    modLoader.getConsole().printLine("OK");
                    modLoader.getBroadcastManager().broadcast("Hub.Log", "OK", modId);
                    
                    try {
                        Object testModB = getTestModB(modLoader);
                        if (testModB != null) {
                            java.lang.reflect.Method method = testModB.getClass().getMethod("doComplete");
                            String result = (String) method.invoke(testModB);
                            modLoader.getConsole().printLine(result);
                            modLoader.getBroadcastManager().broadcast("Hub.Log", result, modId);
                        } else {
                            modLoader.getConsole().printWarning("TestModB not found");
                            modLoader.getBroadcastManager().broadcast("Hub.Log", "TestModB not found", modId);
                        }
                    } catch (Throwable e) {
                        modLoader.getConsole().printError("Error calling TestModB: " + e.getMessage());
                        modLoader.getBroadcastManager().broadcast("Hub.Log", "Error calling TestModB: " + e.getMessage(), modId);
                    }
                }
            },
            modId,
            "TestModA"
        );
    }
    
    private Object getTestModB(ModLoader modLoader) {
        for (IModAPI mod : modLoader.getJavaMods()) {
            if (mod.getModId().equals("TestModB")) {
                return mod;
            }
        }
        return null;
    }
}
