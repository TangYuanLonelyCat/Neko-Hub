package net.lemoncookie.neko.modloader.api;

/**
 * 模组依赖信息
 * 表示一个模组对另一个模组的依赖关系
 */
public class ModDependency {
    
    private final String modId;
    private final String minVersion;
    
    /**
     * 创建模组依赖
     * 
     * @param modId 依赖的模组 ID
     * @param minVersion 依赖的最小版本号
     */
    public ModDependency(String modId, String minVersion) {
        this.modId = modId;
        this.minVersion = minVersion;
    }
    
    /**
     * 获取依赖的模组 ID
     */
    public String getModId() {
        return modId;
    }
    
    /**
     * 获取依赖的最小版本号
     */
    public String getMinVersion() {
        return minVersion;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ModDependency that = (ModDependency) obj;
        return modId.equals(that.modId) && minVersion.equals(that.minVersion);
    }
    
    @Override
    public int hashCode() {
        return 31 * modId.hashCode() + minVersion.hashCode();
    }
    
    @Override
    public String toString() {
        return modId + "-" + minVersion;
    }
}
