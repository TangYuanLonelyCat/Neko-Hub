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
        if (part == null || part.trim().isEmpty()) {
            return 0;
        }
        try {
            // 尝试解析数字
            return Integer.parseInt(part.trim());
        } catch (NumberFormatException e) {
            // 如果包含非数字字符（如 alpha, beta），尝试提取数字部分
            String digits = part.replaceAll("\\D+", "");
            if (digits.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException e2) {
                return 0;  // Safe fallback
            }
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
    
    /**
     * 检查版本是否在可接受范围内
     * 用于 API 版本兼容性检查
     * 
     * 规则：
     * 1. 模组 API 版本高于 ModLoader API 版本 → 拒绝（不兼容）
     *    原因：模组基于不存在的 API 版本开发
     * 2. 模组 API 版本低于 ModLoader API 版本最多 1 个 Minor → 警告（兼容）
     *    例如：ModLoader 是 2.1.0，模组是 2.0.x 可以接受
     * 3. 模组 API 版本等于或略低于（同 Minor）→ 完全兼容
     * 
     * @param currentVersion 当前模组的 API 版本
     * @param requiredMinVersion ModLoader 要求的最低 API 版本
     * @return 返回兼容性级别：0=完全兼容，1=兼容但需要警告，2=不兼容
     */
    public static int checkCompatibilityLevel(String currentVersion, String requiredMinVersion) {
        int comparison = compare(currentVersion, requiredMinVersion);
        
        // 模组 API 版本高于 ModLoader API 版本 → 拒绝
        if (comparison > 0) {
            return 2; // 不兼容
        }
        
        // 模组 API 版本等于 ModLoader API 版本 → 完全兼容
        if (comparison == 0) {
            return 0;
        }
        
        // 模组 API 版本低于 ModLoader API 版本，检查差距
        String[] currentParts = currentVersion.replaceAll("^[vV]", "").trim().split("[.-]");
        String[] requiredParts = requiredMinVersion.replaceAll("^[vV]", "").trim().split("[.-]");
        
        int currentMajor = (currentParts.length > 0) ? parseVersionPart(currentParts[0]) : 0;
        int currentMinor = (currentParts.length > 1) ? parseVersionPart(currentParts[1]) : 0;
        
        int requiredMajor = (requiredParts.length > 0) ? parseVersionPart(requiredParts[0]) : 0;
        int requiredMinor = (requiredParts.length > 1) ? parseVersionPart(requiredParts[1]) : 0;
        
        // Major 版本不同 → 不兼容
        if (currentMajor != requiredMajor) {
            return 2;
        }
        
        // Major 相同，检查 Minor 差距
        int minorDiff = requiredMinor - currentMinor;
        
        // Minor 差距超过 1 → 不兼容
        if (minorDiff > 1) {
            return 2;
        }
        
        // Minor 差距为 1 → 警告（兼容但需要警告）
        if (minorDiff == 1) {
            return 1;
        }
        
        // Minor 相同（Patch 不同）→ 完全兼容
        return 0;
    }
    
    /**
     * 检查 API 版本是否过高
     * @param modApiVersion 模组 API 版本
     * @param minApiVersion 模组加载器最低 API 版本
     * @return true 表示模组 API 版本过高，false 表示正常或偏低
     */
    public static boolean isApiVersionTooHigh(String modApiVersion, String minApiVersion) {
        return compare(modApiVersion, minApiVersion) > 0;
    }
}
