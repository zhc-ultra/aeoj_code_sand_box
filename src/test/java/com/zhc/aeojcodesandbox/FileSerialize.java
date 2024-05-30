package com.zhc.aeojcodesandbox;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author zhc
 * @description
 * @date 2024/5/27 07:57
 **/
public class FileSerialize {
    public static void main(String[] args) {
        File srcFile = new File("/Users/yinger/Desktop/aeoj_code_sandbox/src/main/resources/temp/input.txt");
        String str = FileUtil.readString(srcFile, StandardCharsets.UTF_8);
        FileUtil.writeString(str, "/Users/yinger/Desktop/aeoj_code_sandbox/src/main/resources/temp/write.txt", StandardCharsets.UTF_8);
        File write = new File("/Users/yinger/Desktop/aeoj_code_sandbox/src/main/resources/temp/write.txt");
        System.out.println(FileUtil.readString(write, StandardCharsets.UTF_8));
    }
}