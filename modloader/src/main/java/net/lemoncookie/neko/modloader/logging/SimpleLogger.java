package net.lemoncookie.neko.modloader.logging;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 简单日志系统
 * 监听所有广播域的消息并记录到日志文件
 */
public class SimpleLogger implements MessageListener {
    
    private static final String LOGS_DIR = "logs";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final AtomicReference<BufferedWriter> currentWriter;
    private final AtomicReference<LocalDate> currentDate;
    private final ConcurrentHashMap<String, LogLevel> domainLogLevels;
    private final ModLoader modLoader;
    
    /**
     * 日志级别
     */
    public enum LogLevel {
        INFO("INFO"),
        WARNING("WARN"),
        ERROR("ERROR"),
        DEBUG("DEBUG");
        
        private final String displayName;
        
        LogLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public SimpleLogger(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.currentWriter = new AtomicReference<>();
        this.currentDate = new AtomicReference<>();
        this.domainLogLevels = new ConcurrentHashMap<>();
        
        // 创建日志目录
        createLogsDirectory();
        
        // 初始化日志文件
        initLogFile();
    }
    
    /**
     * 创建日志目录
     */
    private void createLogsDirectory() {
        try {
            Path logsPath = Paths.get(LOGS_DIR);
            if (!Files.exists(logsPath)) {
                Files.createDirectories(logsPath);
            }
        } catch (IOException e) {
            String errorMsg = "Failed to create logs directory: " + e.getMessage();
            modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "SimpleLogger");
        }
    }
    
    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        try {
            LocalDate today = LocalDate.now();
            String logFileName = "neko-hub-" + today.format(FILE_DATE_FORMAT) + ".log";
            Path logFilePath = Paths.get(LOGS_DIR, logFileName);
            
            // 如果已有写入器，先关闭
            BufferedWriter oldWriter = currentWriter.getAndSet(null);
            if (oldWriter != null) {
                try {
                    oldWriter.close();
                } catch (IOException e) {
                    // 忽略关闭错误
                }
            }
            
            // 创建新的写入器（追加模式）
            BufferedWriter writer = Files.newBufferedWriter(
                logFilePath,
                StandardCharsets.UTF_8
            );
            currentWriter.set(writer);
            currentDate.set(today);
            
            // 写入日志文件头
            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("Neko-Hub Log File - " + today.format(FILE_DATE_FORMAT));
            writer.newLine();
            writer.write("Started at: " + LocalDateTime.now().format(LOG_DATE_FORMAT));
            writer.newLine();
            writer.write("=".repeat(80));
            writer.newLine();
            writer.flush();
            
        } catch (IOException e) {
            String errorMsg = "Failed to initialize log file: " + e.getMessage();
            modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "SimpleLogger");
        }
    }
    
    /**
     * 检查并切换日志文件（跨天时自动切换）
     */
    private void checkAndRotateLogFile() {
        LocalDate today = LocalDate.now();
        LocalDate storedDate = currentDate.get();
        
        if (storedDate == null || !storedDate.equals(today)) {
            initLogFile();
        }
    }
    
    /**
     * 写入日志
     */
    private void writeLog(LogLevel level, String domain, String message, String senderModId) {
        checkAndRotateLogFile();
        
        BufferedWriter writer = currentWriter.get();
        if (writer == null) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(LOG_DATE_FORMAT);
            String logLine = String.format(
                "[%s] [%s] [%s] (from: %s) %s",
                timestamp,
                level.getDisplayName(),
                domain,
                senderModId,
                message
            );
            
            writer.write(logLine);
            writer.newLine();
            writer.flush();
            
        } catch (IOException e) {
            String errorMsg = "Failed to write log: " + e.getMessage();
            modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "SimpleLogger");
        }
    }
    
    @Override
    public void onMessageReceived(String domain, String message, String senderModId) {
        // 判断日志级别
        LogLevel level = determineLogLevel(message);
        writeLog(level, domain, message, senderModId);
    }
    
    /**
     * 根据消息内容判断日志级别
     */
    private LogLevel determineLogLevel(String message) {
        if (message == null || message.isEmpty()) {
            return LogLevel.INFO;
        }
        
        String upperMessage = message.toUpperCase();
        
        if (upperMessage.contains("[ERROR]") || upperMessage.contains("ERROR")) {
            return LogLevel.ERROR;
        } else if (upperMessage.contains("[WARNING]") || upperMessage.contains("WARN")) {
            return LogLevel.WARNING;
        } else if (upperMessage.contains("[DEBUG]") || upperMessage.contains("DEBUG")) {
            return LogLevel.DEBUG;
        } else {
            return LogLevel.INFO;
        }
    }
    
    /**
     * 设置特定域的日志级别
     */
    public void setDomainLogLevel(String domain, LogLevel level) {
        domainLogLevels.put(domain, level);
    }
    
    /**
     * 关闭日志系统
     */
    public void close() {
        BufferedWriter writer = currentWriter.getAndSet(null);
        if (writer != null) {
            try {
                writer.write("=".repeat(80));
                writer.newLine();
                writer.write("Log file closed at: " + LocalDateTime.now().format(LOG_DATE_FORMAT));
                writer.newLine();
                writer.write("=".repeat(80));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                String errorMsg = "Failed to close log file: " + e.getMessage();
                modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "SimpleLogger");
            }
        }
    }
}
