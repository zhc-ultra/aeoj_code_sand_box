package com.zhc.aeojcodesandbox.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 */
@Getter
public enum ExecStateEnum {
    SUCCESS("SUCCESS", 0),
    TIME_LIMIT_EXCEEDED("运行时间超限制", 1),
    MEMORY_LIMIT_EXCEEDED("内存超限制", 2),
    RUNTIME_ERROR("运行时错误", 3),
    SYSTEM_ERROR("系统错误", 4),
    OUTPUT_LIMIT_EXCEEDED("输出超限制", 5),
    COMPILE_ERROR("编译错误", 6);

    private final String text;
    private final Integer value;

    ExecStateEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static ExecStateEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ExecStateEnum anEnum : ExecStateEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
