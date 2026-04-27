package net.lemoncookie.neko.modloader.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器
 */
public class LanguageManager {

    private static final String DEFAULT_LANG = "en";
    private final Map<String, String> messages;

    /**
     * 构造函数
     */
    public LanguageManager() {
        this.messages = new HashMap<>();
        loadLanguage(DEFAULT_LANG);
    }

    /**
     * 加载语言
     */
    public void loadLanguage(String lang) {
        loadLanguage(lang, false);
    }

    /**
     * 加载语言（内部方法）
     * @param lang 语言代码
     * @param isDefaultAttempt 是否是默认语言尝试
     */
    private void loadLanguage(String lang, boolean isDefaultAttempt) {
        try (InputStream is = getClass().getResourceAsStream("/lang/" + lang + ".json")) {
            if (is != null) {
                Map<String, String> langMessages = parseJson(is);
                messages.clear();
                messages.putAll(langMessages);
            } else if (!isDefaultAttempt) {
                loadLanguage(DEFAULT_LANG, true);
            } else {
                messages.clear();
            }
        } catch (IOException e) {
            if (!isDefaultAttempt) {
                loadLanguage(DEFAULT_LANG, true);
            } else {
                messages.clear();
            }
        }
    }

    /**
     * 手动解析JSON文件
     * @param is 输入流
     * @return 键值对映射
     */
    private Map<String, String> parseJson(InputStream is) throws IOException {
        Map<String, String> result = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }

        String json = content.toString().trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }

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

            ParseResult valueResult = parseString(json, i);
            if (valueResult != null) {
                result.put(key, valueResult.value);
                i = valueResult.endIndex;
            } else {
                i = skipValue(json, i);
            }

            i = skipWhitespace(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++;
            }
        }

        return result;
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
     * 获取消息
     */
    public String getMessage(String key, Object... args) {
        String message = messages.get(key);
        if (message == null) {
            return key;
        }
        if (args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }

    /**
     * 检查消息是否存在
     */
    public boolean hasMessage(String key) {
        return messages.containsKey(key);
    }
}