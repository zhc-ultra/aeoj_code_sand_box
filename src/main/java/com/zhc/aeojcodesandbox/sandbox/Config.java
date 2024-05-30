package com.zhc.aeojcodesandbox.sandbox;


/**
 * @author zhc
 * @description 沙箱配置，对应C语言结构体，进行传参
 * @date 2024/5/30 09:21
 **/
public class Config {
    private int maxCpuTime;
    private int maxRealTime;
    private long maxMemory;
    private long maxStack;
    private int maxProcessNumber;
    private long maxOutputSize;
    private int memoryLimitCheckOnly;
    private String exePath;
    private String inputPath;
    private String outputPath;
    private String errorPath;
    private String[] args;
    private String[] env;
    private String logPath;
    private String seccompRuleName;
    private int uid;
    private int gid;

    public Config(int maxCpuTime, int maxRealTime, long maxMemory, long maxStack, int maxProcessNumber, long maxOutputSize, int memoryLimitCheckOnly, String exePath, String inputPath, String outputPath, String errorPath, String[] args, String[] env, String logPath, String seccompRuleName, int uid, int gid) {
        this.maxCpuTime = maxCpuTime;
        this.maxRealTime = maxRealTime;
        this.maxMemory = maxMemory;
        this.maxStack = maxStack;
        this.maxProcessNumber = maxProcessNumber;
        this.maxOutputSize = maxOutputSize;
        this.memoryLimitCheckOnly = memoryLimitCheckOnly;
        this.exePath = exePath;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.args = args;
        this.env = env;
        this.logPath = logPath;
        this.seccompRuleName = seccompRuleName;
        this.uid = uid;
        this.gid = gid;
    }

    public int getMaxCpuTime() {
        return maxCpuTime;
    }

    public void setMaxCpuTime(int maxCpuTime) {
        this.maxCpuTime = maxCpuTime;
    }

    public int getMaxRealTime() {
        return maxRealTime;
    }

    public void setMaxRealTime(int maxRealTime) {
        this.maxRealTime = maxRealTime;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(long maxStack) {
        this.maxStack = maxStack;
    }

    public int getMaxProcessNumber() {
        return maxProcessNumber;
    }

    public void setMaxProcessNumber(int maxProcessNumber) {
        this.maxProcessNumber = maxProcessNumber;
    }

    public long getMaxOutputSize() {
        return maxOutputSize;
    }

    public void setMaxOutputSize(long maxOutputSize) {
        this.maxOutputSize = maxOutputSize;
    }

    public int getMemoryLimitCheckOnly() {
        return memoryLimitCheckOnly;
    }

    public void setMemoryLimitCheckOnly(int memoryLimitCheckOnly) {
        this.memoryLimitCheckOnly = memoryLimitCheckOnly;
    }

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getEnv() {
        return env;
    }

    public void setEnv(String[] env) {
        this.env = env;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getSeccompRuleName() {
        return seccompRuleName;
    }

    public void setSeccompRuleName(String seccompRuleName) {
        this.seccompRuleName = seccompRuleName;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    //    struct config {
//        int max_cpu_time;
//        int max_real_time;
//        long max_memory;
//        long max_stack;
//        int max_process_number;
//        long max_output_size;
//        int memory_limit_check_only;
//        char *exe_path;
//        char *input_path;
//        char *output_path;
//        char *error_path;
//        char *args[ARGS_MAX_NUMBER];
//        char *env[ENV_MAX_NUMBER];
//        char *log_path;
//        char *seccomp_rule_name;
//        uid_t uid;
//        gid_t gid;
//    };

}
