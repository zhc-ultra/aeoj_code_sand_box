package com.zhc.aeojcodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import com.zhc.aeojcodesandbox.model.CompileResult;
import com.zhc.aeojcodesandbox.model.SandBoxRequest;
import com.zhc.aeojcodesandbox.model.SandBoxResponse;
import com.zhc.aeojcodesandbox.model.ExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zhc
 * @description 代码沙箱模板
 * @date 2024/5/27 08:23
 **/
@Slf4j
public abstract class SandBoxTemplate implements SandBox {
    public static final String C_EXTENSION = ".c";
    public static final String GO_EXTENSION = ".go";
    public static final String CPP_EXTENSION = ".cpp";
    public static final String PYTHON_EXTENSION = ".py";
    public static final String JAVA_EXTENSION = ".java";
    public static final String JAVASCRIPT_EXTENSION = ".js";
    public static final String CONTAINER_MOUNT_DIR = "java-docker-work-dir";
    public static final String EXECUTION_CODE_FILE_NAME = "Main";
    public static final String INPUT_FILE_NAME = "in.txt";
    public static final String OUTPUT_FILE_NAME = "out.txt";
    public static final String ERR_FILE_NAME = "err.txt";
    public static final String USER_DIR = System.getProperty("user.dir");
    public static Map<String, String> extensionMap = new HashMap<>();

    /**
     * 将测试用例、用户代码写入到到临时文件
     *
     * @param code     代码
     * @param language 语言
     * @return 用户的工作目录
     */
    protected String save(String code, String language) {
        String uuid = UUID.randomUUID().toString();
        // 1) 在容器根目录下生成随机目录，如果不存在就将目录创建出来
        String workDir = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid;
        if (!FileUtil.exist(workDir)) FileUtil.mkdir(workDir);
        // 2) 将用户的代码写入用户的工作目录中
        String srcPath = workDir + File.separator + EXECUTION_CODE_FILE_NAME + extensionMap.get(language);
        FileUtil.writeString(code, srcPath, StandardCharsets.UTF_8);
        // 3) 创建出标准输入文件
        FileUtil.writeString("", workDir + File.separator + INPUT_FILE_NAME, StandardCharsets.UTF_8);
        // 4) 创建出标准输出文件
        FileUtil.writeString("", workDir + File.separator + OUTPUT_FILE_NAME, StandardCharsets.UTF_8);
        return uuid;
    }

    /**
     * (仅编译型语言) 编译 对于编译型语言，编译后生成可执行文件，对于解释型语言，直接生成可执行文件
     * 每个编译型语言的编译过程都不同，因此各个语言的实现都需要重写这个方法
     *
     * @param uuid 用户的工作目录
     * @return 编译结果
     */
    protected abstract CompileResult compile(String uuid);

    /**
     * 重定向标准输入到输入文件，输出重定向到输出文件，并执行程序得到执行结果
     *
     * @param uuid      用户的工作目录
     * @param inputList 测试用例列表
     * @return 程序执行的结果
     */
    protected abstract ExecuteResult execute(String uuid, List<String> inputList);

    /**
     * 释放资源，结束本次代码沙箱调用
     *
     * @param uuid 用户的工作目录
     */
    protected void freeResources(String uuid) {
        // 直接将整个临时目录删除即可
        String workDir = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid;
        boolean del = FileUtil.del(workDir);
        if (!del) {
            log.error("释放资源失败," + workDir);
        }
    }

    public static void interruptStatisticsThread() {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (lstThreads[i].getName().contains("docker-java-stream-")) {
                log.info("终止统计线程，线程号：" + i + " = " + lstThreads[i].getName());
                lstThreads[i].interrupt();
            }
        }
    }

    /**
     * 执行代码沙箱，各个语言自己实现
     *
     * @param executeCodeRequest 执行代码请求
     * @return 沙箱的执行结果
     */
    @Override
    public abstract SandBoxResponse run(SandBoxRequest executeCodeRequest);

    static {
        extensionMap.put("c", C_EXTENSION);
        extensionMap.put("go", GO_EXTENSION);
        extensionMap.put("cpp", CPP_EXTENSION);
        extensionMap.put("python", PYTHON_EXTENSION);
        extensionMap.put("java", JAVA_EXTENSION);
        extensionMap.put("javascript", JAVASCRIPT_EXTENSION);
    }
}
