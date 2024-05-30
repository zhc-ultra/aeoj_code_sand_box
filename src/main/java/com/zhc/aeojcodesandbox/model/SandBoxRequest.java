package com.zhc.aeojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhc
 * @description
 * @date 2024/5/24 10:25
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandBoxRequest {
    // 输入用例列表
    private List<String> inputList;
    // 输入代码
    private String code;
    // 编程语言
    private String language;
}