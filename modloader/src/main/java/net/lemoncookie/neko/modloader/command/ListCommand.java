package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * 列出已注册模组命令
 * 监听 Hub.Command 广播域
 */
public class ListCommand extends BaseCommandListener {
    
    private static final int MODS_PER_PAGE = 10;
    
    public ListCommand(ModLoader modLoader) {
        super(modLoader, "list");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        try {
            String targetType = "mod";
            int page = 1;
            
            if (commandMessage.getPartCount() >= 1) {
                targetType = commandMessage.getPart(0).toLowerCase();
            }
            if (commandMessage.getPartCount() >= 2) {
                try {
                    page = Integer.parseInt(commandMessage.getPart(1));
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
    
    private void listMods(ModLoader modLoader, int page) {
        List<IModAPI> allMods = new ArrayList<>();
        allMods.addAll(modLoader.getJavaMods());
        
        int totalMods = allMods.size();
        int totalPages = (int) Math.ceil((double) totalMods / MODS_PER_PAGE);
        
        if (page > totalPages) {
            modLoader.getConsole().printWarning(modLoader.getLanguageManager().getMessage("command.list.warning.page_invalid", page, totalPages));
            page = totalPages;
        }
        
        int startIndex = (page - 1) * MODS_PER_PAGE;
        int endIndex = Math.min(startIndex + MODS_PER_PAGE, totalMods);
        
        modLoader.getConsole().printLine();
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.divider"));
        modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.list.info.mods_title", page, totalPages));
        modLoader.getConsole().printLine();
        
        for (int i = startIndex; i < endIndex; i++) {
            IModAPI mod = allMods.get(i);
            modLoader.getConsole().printLine(
                modLoader.getLanguageManager().getMessage("command.list.mod_entry", i + 1, mod.getName(), mod.getVersion())
            );
            modLoader.getConsole().printLine(
                modLoader.getLanguageManager().getMessage("command.list.mod_id", mod.getModId())
            );
            modLoader.getConsole().printLine(
                modLoader.getLanguageManager().getMessage("command.list.mod_package", mod.getPackageName())
            );
            modLoader.getConsole().printLine();
        }
        
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.info.total_loaded", totalMods));
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.info.use_list"));
        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.list.divider"));
    }
}
