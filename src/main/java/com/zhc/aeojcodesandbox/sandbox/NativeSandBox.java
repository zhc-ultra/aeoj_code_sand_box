package com.zhc.aeojcodesandbox.sandbox;


/**
 * @author zhc
 * @description
 * @date 2024/5/28 19:30
 **/
public abstract class NativeSandBox {
        static {
        String runLibPath = "/Users/yinger/Desktop/aeoj_code_sandbox/src/main/java/com/zhc/aeojcodesandbox/sandbox/run.dylib";
        System.load(runLibPath);
    }
    protected native Result run(Config config);
}