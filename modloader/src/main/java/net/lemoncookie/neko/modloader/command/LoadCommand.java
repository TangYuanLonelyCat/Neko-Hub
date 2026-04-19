package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.KModAPI;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.lang.reflect.Constructor;

/**
 * 加载模组命令
 * 监听 Hub.Command 广播域
 */
public class LoadCommand extends BaseCommandListener {
    
    public LoadCommand(ModLoader modLoader) {
        super(modLoader, "load");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        if (commandMessage.getPartCount() == 0) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/load [模组文件名]")
            );
            return;
        }

        // 获取文件名参数
        String fileName = commandMessage.getPart(0);
        
        // 移除可能的引号
        fileName = fileName.trim();
        if ((fileName.startsWith("\"") && fileName.endsWith("\"")) || 
            (fileName.startsWith("'") && fileName.endsWith("'"))) {
            fileName = fileName.substring(1, fileName.length() - 1);
        }

        // 确保文件名以.jar 结尾
        if (!fileName.endsWith(".jar")) {
            fileName = fileName + ".jar";
        }

        // 文件名加载
        loadModByFileName(modLoader, fileName);
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
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.load.error.invalid_path", fileName));
                return;
            }
        } catch (IOException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.load.error.invalid_path", fileName));
            return;
        }
        
        if (!modFile.exists()) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.load.error.not_found", fileName));
            return;
        }

        try {
            // 从 JAR 文件加载模组
            loadModFromJar(modLoader, modFile);
        } catch (Exception e) {
            String errorMsg = modLoader.getLanguageManager().getMessage("command.load.error.load_failed", e.getMessage());
            modLoader.getConsole().printError(errorMsg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + errorMsg, "LoadCommand");
        }
    }

    /**
     * 从 JAR 文件加载模组的通用方法
     */
    @SuppressWarnings("unchecked")
    private void loadModFromJar(ModLoader modLoader, File modFile) throws Exception {
        String loadingMsg = modLoader.getLanguageManager().getMessage("command.load.info.loading", modFile.getAbsolutePath());
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
                    
                    // 检查是否实现了 IModAPI 或 KModAPI 接口
                    boolean isJavaMod = IModAPI.class.isAssignableFrom(modClass);
                    boolean isKotlinMod = KModAPI.class.isAssignableFrom(modClass);
                    
                    if (!isJavaMod && !isKotlinMod) {
                        throw new Exception("Mod class does not implement IModAPI or KModAPI: " + modClassName);
                    }

                    // 创建模组实例
                    Constructor<?> constructor = modClass.getDeclaredConstructor();
                    
                    if (isJavaMod) {
                        modInstance = (IModAPI) constructor.newInstance();
                        // 注册 Java 模组
                        modLoader.registerJavaMod(modInstance);
                    } else {
                        // Kotlin 模组需要通过 KModAPI 接口访问
                        Object kotlinModInstance = constructor.newInstance();
                        modLoader.registerKotlinMod((KModAPI) kotlinModInstance);
                    }
                } catch (Throwable e) {
                    String warningMsg = modLoader.getLanguageManager().getMessage("command.load.error.loading_error", e.getMessage());
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
}
