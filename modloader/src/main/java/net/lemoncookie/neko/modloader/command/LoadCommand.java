package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.lang.reflect.Constructor;

/**
 * 加载模组命令
 * 支持包名和文件名加载（不支持路径）
 */
public class LoadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/load [模组文件名]")
            );
            return;
        }

        // 移除可能的引号
        args = args.trim();
        if ((args.startsWith("\"") && args.endsWith("\"")) || 
            (args.startsWith("'") && args.endsWith("'"))) {
            args = args.substring(1, args.length() - 1);
        }

        // 确保文件名以.jar 结尾
        if (!args.endsWith(".jar")) {
            args = args + ".jar";
        }

        // 文件名加载
        loadModByFileName(modLoader, args);
    }

    /**
     * 通过文件名加载模组
     */
    private void loadModByFileName(ModLoader modLoader, String fileName) {
        File modsDir;
        File modFile;
        try {
            modsDir = new File("mods").getCanonicalFile();
            modFile = new File(modsDir, fileName).getCanonicalFile();
            
            // 确保文件在 mods 目录内
            if (!modFile.getCanonicalPath().startsWith(modsDir.getCanonicalPath() + File.separator)) {
                modLoader.getConsole().printError("Invalid mod file path: " + fileName);
                return;
            }
        } catch (IOException e) {
            modLoader.getConsole().printError("Invalid mod file path: " + fileName);
            return;
        }
        
        if (!modFile.exists()) {
            modLoader.getConsole().printError("Mod file not found: " + fileName);
            return;
        }

        try {
            // 从 JAR 文件加载模组
            loadModFromJar(modLoader, modFile);
        } catch (Exception e) {
            String errorMsg = "Failed to load mod: " + e.getMessage();
            modLoader.getConsole().printError(errorMsg);
            // 通过广播域发送错误消息
            modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "LoadCommand");
        }
    }

    /**
     * 从 JAR 文件加载模组的通用方法
     */
    @SuppressWarnings("unchecked")
    private void loadModFromJar(ModLoader modLoader, File modFile) throws Exception {
        String loadingMsg = "Loading mod from: " + modFile.getAbsolutePath();
        modLoader.getConsole().printInfo(loadingMsg);
        modLoader.getBroadcastManager().broadcast("Hub.Log", "[INFO] " + loadingMsg, "LoadCommand");

        IModAPI modInstance = null;
        
        // 读取 MANIFEST.MF 获取主类
        try (JarFile jarFile = new JarFile(modFile)) {
            Manifest manifest = jarFile.getManifest();
            
            if (manifest == null) {
                throw new Exception("No manifest found in JAR file");
            }

            // 获取模组实现类（需要在 MANIFEST.MF 中指定 Mod-Impl-Class）
            String modClassName = manifest.getMainAttributes().getValue("Mod-Impl-Class");
            if (modClassName == null) {
                // 尝试使用 Main-Class 作为备选
                modClassName = manifest.getMainAttributes().getValue("Main-Class");
            }

            if (modClassName == null) {
                throw new Exception("No Mod-Impl-Class or Main-Class found in manifest");
            }

            // 创建 URLClassLoader 加载 JAR 文件
            URL jarUrl = modFile.toURI().toURL();
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarUrl}, ModLoader.class.getClassLoader())) {
                try {
                    // 加载模组类
                    Class<?> modClass = urlClassLoader.loadClass(modClassName);
                    
                    // 检查是否实现了 IModAPI 接口
                    if (!IModAPI.class.isAssignableFrom(modClass)) {
                        throw new Exception("Mod class does not implement IModAPI: " + modClassName);
                    }

                    // 创建模组实例
                    Constructor<?> constructor = modClass.getDeclaredConstructor();
                    modInstance = (IModAPI) constructor.newInstance();

                    // 检查模组 ID 是否重复
                    for (IModAPI loadedMod : modLoader.getJavaMods()) {
                        if (loadedMod.getModId().equals(modInstance.getModId())) {
                            throw new Exception("Mod with ID '" + modInstance.getModId() + "' is already loaded");
                        }
                    }

                    // 注册模组
                    modLoader.registerJavaMod(modInstance);
                } catch (Throwable e) {
                    String warningMsg = "Error during mod loading: " + e.getMessage();
                    modLoader.getConsole().printWarning(warningMsg);
                    modLoader.getBroadcastManager().broadcast("Hub.Log", "[WARNING] " + warningMsg, "LoadCommand");
                    throw e;
                }
            }
        }
        
        if (modInstance != null) {
            // 成功消息由 registerJavaMod 统一处理
        }
    }

    @Override
    public String getDescription() {
        return "加载模组（通过文件名）";
    }

    @Override
    public String getUsage() {
        return "/load [模组文件名]";
    }
}
