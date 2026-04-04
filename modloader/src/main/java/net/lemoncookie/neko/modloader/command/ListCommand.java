package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * 列出已注册模组命令
 */
public class ListCommand implements Command {

    private static final int MODS_PER_PAGE = 10;

    @Override
    public void execute(ModLoader modLoader, String args) {
        try {
            String[] parts = args.trim().split("\\s+");
            String targetType = "mod"; // 默认显示模组列表
            int page = 1;
            
            // 解析参数
            if (parts.length >= 1 && !parts[0].isEmpty()) {
                targetType = parts[0].toLowerCase();
            }
            if (parts.length >= 2) {
                try {
                    page = Integer.parseInt(parts[1]);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }
            
            if ("mod".equals(targetType)) {
                listMods(modLoader, page);
            } else {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.list.error.unknown_type", targetType));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.usage"));
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.list.error.failed", e.getMessage()));
        }
    }
    
    /**
     * 列出所有已注册的模组
     */
    private void listMods(ModLoader modLoader, int page) {
        List<IModAPI> allMods = new ArrayList<>();
        allMods.addAll(modLoader.getJavaMods());
        // 如果需要也可以添加 Kotlin 模组
        
        int totalMods = allMods.size();
        int totalPages = (int) Math.ceil((double) totalMods / MODS_PER_PAGE);
        
        if (page > totalPages) {
            modLoader.getConsole().printWarning(modLoader.getLanguageManager().getMessage("command.list.warning.page_invalid", page, totalPages));
            page = totalPages;
        }
        
        int startIndex = (page - 1) * MODS_PER_PAGE;
        int endIndex = Math.min(startIndex + MODS_PER_PAGE, totalMods);
        
        modLoader.getConsole().printLine();
        modLoader.getConsole().printLine("═══════════════════════════════════════");
        modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.list.info.mods_title", page, totalPages));
        modLoader.getConsole().printLine();
        
        for (int i = startIndex; i < endIndex; i++) {
            IModAPI mod = allMods.get(i);
            modLoader.getConsole().printLine("  " + (i + 1) + ". " + mod.getName() + " v" + mod.getVersion());
            modLoader.getConsole().printLine("     ID: " + mod.getModId());
            modLoader.getConsole().printLine("     Package: " + mod.getPackageName());
            modLoader.getConsole().printLine();
        }
        
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.info.total_loaded", totalMods));
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.info.use_list"));
        modLoader.getConsole().printLine("═══════════════════════════════════════");
    }

    @Override
    public String getDescription() {
        return "List loaded mods or commands";
    }

    @Override
    public String getUsage() {
        return "/list mod [page]";
    }
}
