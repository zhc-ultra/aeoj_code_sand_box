package com.zhc.aeojcodesandbox.model;

import lombok.Data;

import java.util.List;

/**
 * @author zhc
 * @description 进程执行结果
 * @date 2024/5/24 18:58
 **/
@Data
public class ExecuteResult {
    // 程序执行时间 xx/ms
    private Long time;
    // 程序执行的内存消耗 xx/kb
    private Long memory;
    private Integer Status;
    private String in;
    private String err;
    private String out;
    private List<String> outputList;

    public ExecuteResult(Long time, Long memory, Integer status, String in, String err, String out) {
        this.time = time;
        this.memory = memory;
        Status = status;
        this.in = in;
        this.err = err;
        this.out = out;
    }

    public ExecuteResult() {
    }
}