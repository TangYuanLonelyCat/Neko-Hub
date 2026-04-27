package net.lemoncookie.neko.modloader.command;

import java.util.ArrayList;
import java.util.List;

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
     * 手动解析，不依赖GSON
     */
    public static CommandMessage fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                return null;
            }

            String command = null;
            String senderModId = "unknown";
            List<String> partsList = new ArrayList<>();

            int i = 1;
            while (i < json.length() - 1) {
                i = skipWhitespace(json, i);
                if (i >= json.length() - 1) break;

                if (json.charAt(i) != '"') {
                    i++;
                    continue;
                }

                ParseResult keyResult = parseString(json, i);
                if (keyResult == null) {
                    i++;
                    continue;
                }
                String key = keyResult.value;
                i = keyResult.endIndex;

                i = skipWhitespace(json, i);
                if (i >= json.length() || json.charAt(i) != ':') {
                    i++;
                    continue;
                }
                i++;

                i = skipWhitespace(json, i);

                if (key.equals("command")) {
                    ParseResult valueResult = parseString(json, i);
                    if (valueResult != null) {
                        command = valueResult.value;
                        i = valueResult.endIndex;
                    } else {
                        i = skipValue(json, i);
                    }
                } else if (key.equals("sender")) {
                    ParseResult valueResult = parseString(json, i);
                    if (valueResult != null) {
                        senderModId = valueResult.value;
                        i = valueResult.endIndex;
                    } else {
                        i = skipValue(json, i);
                    }
                } else if (key.equals("parts")) {
                    if (i < json.length() && json.charAt(i) == '[') {
                        ParseArrayResult arrayResult = parseArray(json, i);
                        if (arrayResult != null) {
                            partsList = arrayResult.values;
                            i = arrayResult.endIndex;
                        } else {
                            i = skipValue(json, i);
                        }
                    } else {
                        i = skipValue(json, i);
                    }
                } else {
                    i = skipValue(json, i);
                }

                i = skipWhitespace(json, i);
                if (i < json.length() && json.charAt(i) == ',') {
                    i++;
                }
            }

            if (command == null) {
                return null;
            }

            return new CommandMessage(command, partsList.toArray(new String[0]), senderModId);
        } catch (Exception e) {
            return null;
        }
    }

    private static int skipWhitespace(String s, int start) {
        int i = start;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static class ParseResult {
        final String value;
        final int endIndex;

        ParseResult(String value, int endIndex) {
            this.value = value;
            this.endIndex = endIndex;
        }
    }

    private static class ParseArrayResult {
        final List<String> values;
        final int endIndex;

        ParseArrayResult(List<String> values, int endIndex) {
            this.values = values;
            this.endIndex = endIndex;
        }
    }

    private static ParseResult parseString(String s, int start) {
        if (start >= s.length() || s.charAt(start) != '"') {
            return null;
        }

        StringBuilder result = new StringBuilder();
        int i = start + 1;

        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"' && (i == start + 1 || s.charAt(i - 1) != '\\')) {
                return new ParseResult(result.toString(), i + 1);
            } else if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"': result.append('"'); i += 2; break;
                    case '\\': result.append('\\'); i += 2; break;
                    case 'n': result.append('\n'); i += 2; break;
                    case 't': result.append('\t'); i += 2; break;
                    case 'r': result.append('\r'); i += 2; break;
                    default: result.append(c); i++; break;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return null;
    }

    private static ParseArrayResult parseArray(String s, int start) {
        if (start >= s.length() || s.charAt(start) != '[') {
            return null;
        }

        List<String> values = new ArrayList<>();
        int i = start + 1;

        i = skipWhitespace(s, i);

        if (i < s.length() && s.charAt(i) == ']') {
            return new ParseArrayResult(values, i + 1);
        }

        while (i < s.length()) {
            i = skipWhitespace(s, i);

            if (i < s.length() && s.charAt(i) == '"') {
                ParseResult strResult = parseString(s, i);
                if (strResult != null) {
                    values.add(strResult.value);
                    i = strResult.endIndex;
                } else {
                    break;
                }
            } else {
                break;
            }

            i = skipWhitespace(s, i);

            if (i < s.length() && s.charAt(i) == ',') {
                i++;
            } else if (i < s.length() && s.charAt(i) == ']') {
                return new ParseArrayResult(values, i + 1);
            } else {
                break;
            }
        }

        return null;
    }

    private static int skipValue(String s, int start) {
        if (start >= s.length()) return start;

        char c = s.charAt(start);
        if (c == '"') {
            ParseResult result = parseString(s, start);
            return result != null ? result.endIndex : start + 1;
        } else if (c == '[') {
            int depth = 1;
            int i = start + 1;
            boolean inString = false;
            while (i < s.length() && depth > 0) {
                char ch = s.charAt(i);
                if (ch == '"' && s.charAt(i - 1) != '\\') {
                    inString = !inString;
                } else if (!inString) {
                    if (ch == '[') depth++;
                    else if (ch == ']') depth--;
                }
                i++;
            }
            return i;
        } else if (c == '{') {
            int depth = 1;
            int i = start + 1;
            boolean inString = false;
            while (i < s.length() && depth > 0) {
                char ch = s.charAt(i);
                if (ch == '"' && s.charAt(i - 1) != '\\') {
                    inString = !inString;
                } else if (!inString) {
                    if (ch == '{') depth++;
                    else if (ch == '}') depth--;
                }
                i++;
            }
            return i;
        } else {
            int i = start;
            while (i < s.length()) {
                char ch = s.charAt(i);
                if (ch == ',' || ch == '}' || ch == ']') {
                    break;
                }
                i++;
            }
            return i;
        }
    }

    /**
     * 转换为 JSON 字符串
     * 手动生成，不依赖GSON
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"command\":\"");
        sb.append(escapeString(command));
        sb.append("\",");
        sb.append("\"sender\":\"");
        sb.append(escapeString(senderModId));
        sb.append("\",");
        sb.append("\"parts\":[");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"");
            sb.append(escapeString(parts[i]));
            sb.append("\"");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\r", "\\r");
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