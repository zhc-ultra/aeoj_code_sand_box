package com.zhc.aeojcodesandbox.utils;

import com.zhc.aeojcodesandbox.model.CompileResult;
import com.zhc.aeojcodesandbox.model.enums.ExecStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhc
 * @description 进程工具类
 * @date 2024/5/24 18:58
 **/
@Slf4j
public class ProcessUtil {
    public static CompileResult compile(Process process) {
        CompileResult result = new CompileResult();
        try {
            // 等待程序执行，获取退出码
            int exitCode = process.waitFor();
            result.setExitCode(exitCode);
            if (exitCode != 0) {
                // 读取进程的标准错误流
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                List<String> errorOutputList = new ArrayList<>();
                String errorCompileOutputLine;
                // 逐行读取
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputList.add(errorCompileOutputLine);
                }
                result.setExitCode(exitCode);
                result.setErrorMessage(StringUtils.join(errorOutputList, "\n"));
                result.setOk(false);
            } else {
                result.setOk(true);
                result.setExitCode(0);
            }
        } catch (Exception e) {
            result.setErrorMessage(e.getMessage());
            result.setExitCode(ExecStateEnum.COMPILE_ERROR.getValue());
            result.setOk(false);
        }
        return result;
    }
}
