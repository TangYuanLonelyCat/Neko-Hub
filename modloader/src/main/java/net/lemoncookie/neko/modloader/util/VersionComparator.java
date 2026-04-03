package net.lemoncookie.neko.modloader.util;

/**
 * 版本比较器
 * 支持语义化版本比较（major.minor.patch）
 */
public class VersionComparator {
    
    /**
     * 比较两个版本号
     * @param version1 版本 1
     * @param version2 版本 2
     * @return 正数表示 version1 > version2，负数表示 version1 < version2，0 表示相等
     */
    public static int compare(String version1, String version2) {
        // 移除可能的 'v' 或 'V' 前缀
        version1 = version1.replaceAll("^[vV]", "").trim();
        version2 = version2.replaceAll("^[vV]", "").trim();
        
        // 分割版本号
        String[] parts1 = version1.split("[.-]");
        String[] parts2 = version2.split("[.-]");
        
        int maxLen = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLen; i++) {
            int num1 = (i < parts1.length) ? parseVersionPart(parts1[i]) : 0;
            int num2 = (i < parts2.length) ? parseVersionPart(parts2[i]) : 0;
            
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        
        return 0; // 版本相同
    }
    
    /**
     * 解析版本号部分
     * @param part 版本号部分
     * @return 解析后的数字，如果解析失败返回 0
     */
    private static int parseVersionPart(String part) {
        try {
            // 尝试解析数字
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            // 如果包含非数字字符（如 alpha, beta），尝试提取数字部分
            String digits = part.replaceAll("\\D+", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        }
    }
    
    /**
     * 检查版本是否兼容（version >= minVersion）
     * @param version 当前版本
     * @param minVersion 最低要求版本
     * @return 如果 version >= minVersion 返回 true，否则返回 false
     */
    public static boolean isCompatible(String version, String minVersion) {
        return compare(version, minVersion) >= 0;
    }
}
