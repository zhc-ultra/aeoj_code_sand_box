package com.zhc.aeojcodesandbox.sandbox;

import com.zhc.aeojcodesandbox.model.SandBoxRequest;
import com.zhc.aeojcodesandbox.model.SandBoxResponse;

import java.io.IOException;

/**
 * @author zhc
 * @description 代码沙箱
 * @date 2024/5/24 10:24
 **/
public interface SandBox {



    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行代码请求
     * @return 执行代码响应
     */
    SandBoxResponse run(SandBoxRequest executeCodeRequest) throws IOException;
}
