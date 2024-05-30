package com.zhc.aeojcodesandbox.controller;

import com.zhc.aeojcodesandbox.model.SandBoxRequest;
import com.zhc.aeojcodesandbox.model.SandBoxResponse;
import com.zhc.aeojcodesandbox.sandbox.JavaSandBox;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhc
 * @description 沙箱控制器
 * @date 2024/5/24 12:15
 **/
@RestController("/")
public class SandBoxController {
    /**
     * 定义鉴权请求头，和秘钥 保证接口安全性
     */
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "fda80ec5306e44a3489562f105d74527";


    /**
     * 接口状态检测
     *
     * @return 状态
     */
    @GetMapping("/healthy")
    public String healthyCheck() {
        return "ok";
    }

    /**
     * 暴露接口
     *
     * @param executeCodeRequest 请求参数
     * @return 代码的执行结果
     */
    @PostMapping("/executeCode")
    public SandBoxResponse executeCode(@RequestBody SandBoxRequest executeCodeRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws InterruptedException {
        JavaSandBox javaSandBox = new JavaSandBox();
        String authHeader = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if (authHeader == null || !authHeader.equals(AUTH_REQUEST_SECRET)) {
            // 无权限 禁止访问(鉴权失败)
            httpServletResponse.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaSandBox.run(executeCodeRequest);
    }
}