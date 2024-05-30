package com.zhc.aeojcodesandbox.model;

import lombok.Data;

import java.util.List;

/**
 * @author zhc
 * @description
 * @date 2024/5/24 10:29
 **/
@Data
public class SandBoxResponse {
    // 输出列表
    private List<String> outputList;
    // 接口信息信息
    private String error;
    // 执行状态
    private Integer status;
    // 判题信息
    private JudgeInfo judgeInfo;

    public SandBoxResponse(List<String> outputList, String error, Integer status, JudgeInfo judgeInfo) {
        this.outputList = outputList;
        this.error = error;
        this.status = status;
        this.judgeInfo = judgeInfo;
    }

    public SandBoxResponse() {
    }
}

