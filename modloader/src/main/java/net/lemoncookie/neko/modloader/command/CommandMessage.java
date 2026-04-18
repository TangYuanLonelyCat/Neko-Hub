package net.lemoncookie.neko.modloader.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 命令消息类
 * 用于在 Hub.Command 广播域中传输命令数据
 */
public class CommandMessage {
    
    private final String command;
    private final String[] parts;
    private final String senderModId;
    
    /**
     * 构造函数
     * @param command 命令名称（如 "set"）
     * @param parts 命令参数数组（如 ["bootfile", "auto"]）
     * @param senderModId 发送者模组 ID
     */
    public CommandMessage(String command, String[] parts, String senderModId) {
        this.command = command;
        this.parts = parts != null ? parts : new String[0];
        this.senderModId = senderModId != null ? senderModId : "unknown";
    }
    
    /**
     * 从 JSON 字符串解析命令消息
     */
    public static CommandMessage fromJson(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String command = jsonObject.get("command").getAsString();
            String senderModId = jsonObject.has("sender") ? jsonObject.get("sender").getAsString() : "unknown";
            
            String[] parts;
            if (jsonObject.has("parts") && jsonObject.get("parts").isJsonArray()) {
                JsonArray array = jsonObject.getAsJsonArray("parts");
                parts = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    parts[i] = array.get(i).getAsString();
                }
            } else {
                parts = new String[0];
            }
            
            return new CommandMessage(command, parts, senderModId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 转换为 JSON 字符串
     */
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", command);
        jsonObject.addProperty("sender", senderModId);
        
        JsonArray partsArray = new JsonArray();
        for (String part : parts) {
            partsArray.add(part);
        }
        jsonObject.add("parts", partsArray);
        
        return jsonObject.toString();
    }
    
    /**
     * 获取命令名称
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * 获取命令参数数组
     */
    public String[] getParts() {
        return parts;
    }
    
    /**
     * 获取参数数量
     */
    public int getPartCount() {
        return parts.length;
    }
    
    /**
     * 获取指定索引的参数
     */
    public String getPart(int index) {
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }
    
    /**
     * 获取发送者模组 ID
     */
    public String getSenderModId() {
        return senderModId;
    }
    
    /**
     * 将所有参数合并为一个字符串（用空格分隔）
     */
    public String getPartsAsString() {
        return String.join(" ", parts);
    }
}
