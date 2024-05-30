package com.zhc.aeojcodesandbox.model;

import lombok.Data;

/**
 * @author zhc
 * @description
 * @date 2024/5/27 09:21
 **/
@Data
public class CompileResult {
    private Boolean ok;
    private String errorMessage;
    private Integer exitCode;

    public CompileResult(boolean ok, String errorMessage) {
        this.ok = ok;
        this.errorMessage = errorMessage;
    }

    public CompileResult() {
    }
}
