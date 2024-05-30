package com.zhc.aeojcodesandbox.sandbox;


/**
 * @author zhc
 * @description 沙箱执行结果
 * @date 2024/5/30 09:27
 **/
public class Result {
    private int cpuTime;
    private int realTime;
    private int memory;
    private int signal;
    private int exitCode;
    private int result;
    private int error;

    public Result(int cpuTime, int realTime, int memory, int signal, int exitCode, int result, int error) {
        this.cpuTime = cpuTime;
        this.realTime = realTime;
        this.memory = memory;
        this.signal = signal;
        this.exitCode = exitCode;
        this.result = result;
        this.error = error;
    }

    @Override
    public String toString() {
        return "Result{" +
                "cpuTime=" + cpuTime +
                ", realTime=" + realTime +
                ", memory=" + memory +
                ", signal=" + signal +
                ", exitCode=" + exitCode +
                ", result=" + result +
                ", error=" + error +
                '}';
    }

    public int getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(int cpuTime) {
        this.cpuTime = cpuTime;
    }

    public int getRealTime() {
        return realTime;
    }

    public void setRealTime(int realTime) {
        this.realTime = realTime;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }
}
