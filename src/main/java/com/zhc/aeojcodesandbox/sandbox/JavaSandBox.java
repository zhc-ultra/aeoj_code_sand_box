package com.zhc.aeojcodesandbox.sandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.zhc.aeojcodesandbox.model.*;
import com.zhc.aeojcodesandbox.model.enums.ExecStateEnum;
import com.zhc.aeojcodesandbox.utils.DockerUtil;
import com.zhc.aeojcodesandbox.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.*;

/**
 * @author zhc
 * @description
 * @date 2024/5/27 09:11
 **/
@Slf4j
public class JavaSandBox extends SandBoxTemplate {
    public JavaSandBox() {
    }

    public static final String CLASS_EXTENSION = ".class";

    public static final Long TIME_OUT = 3000L;
    /**
     * IO 重定向代码段所需的包,直接插入到源代码的顶部
     */
    public static final String IO_REDIRECT_PACKAGE =
            "import java.io.FileInputStream;\n" +
                    "import java.io.FileNotFoundException;\n" +
                    "import java.io.FileOutputStream;\n" +
                    "import java.io.PrintStream;\n\n";
    /**
     * IO 重定向代码段，需要插入到 main 方法的第一行
     */
    public static final String IO_REDIRECT_CODE = "\n       try {\n" +
            "            System.setIn(new FileInputStream(args[0]));\n" +
            "            System.setOut(new PrintStream(new FileOutputStream(args[1])));\n" +
            "            System.setErr(new PrintStream(new FileOutputStream(args[2])));\n" +
            "        } catch (FileNotFoundException e) {\n" +
            "            throw new RuntimeException(\"if the IO redirect fails, check whether the method signature of the main method is correct\");\n" +
            "        }\n";

    public static final String IO_DIRECT_REGULAR_EXPRESSION = "\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*\\[\\s*\\]\\s*args\\s*\\)\\s*\\{";

    static {
        // 确保docker容器正常启动了
        DockerUtil.checkDockerContainer(DockerUtil.initializeDocker());
    }

    /**
     * 创建出一个临时目录，将用户的代码插入IO重定向代码段后保存到临时目录
     * 同时将测试用例写入文件并且创建出一个空的输出文件
     *
     * @param code     代码
     * @param language 语言
     * @return uuid
     */
    @Override
    protected String save(String code, String language) {
        String newCode = insertIORedirect(code);

        String uuid = UUID.randomUUID().toString();
        // 1) 在容器根目录下生成随机目录，如果不存在就将目录创建出来
        String workDir = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid;
        if (!FileUtil.exist(workDir)) FileUtil.mkdir(workDir);
        // 2) 将用户的代码写入用户的工作目录中
        String srcPath = workDir + File.separator + EXECUTION_CODE_FILE_NAME + extensionMap.get(language);
        FileUtil.writeString(newCode, srcPath, UTF_8);
        // 3) 创建标准输出文件
        FileUtil.writeString("", workDir + File.separator + INPUT_FILE_NAME, UTF_8);
        // 4) 创建标准输出文件
        FileUtil.writeString("", workDir + File.separator + OUTPUT_FILE_NAME, UTF_8);
        // 5) 创建标准错误文件
        FileUtil.writeString("", workDir + File.separator + ERR_FILE_NAME, UTF_8);
        return uuid;
    }

    /**
     * 将输入输出重定向的代码插入到源代码中(下策)
     *
     * @param code 源代码
     * @return 插入后的代码
     */
    private String insertIORedirect(String code) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(IO_REDIRECT_PACKAGE).append(code);

        String sourceCode = stringBuilder.toString();
        // 使用正则表达式对源代码进行模式匹配
        Pattern pattern = Pattern.compile(IO_DIRECT_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher(sourceCode);

        // 重置
        stringBuilder = new StringBuilder();
        if (matcher.find()) {
            // 获取匹配到的'{'
            int openingBraceIndex = matcher.end();
            // 确保匹配成功
            if (openingBraceIndex != 0) {
                // 插入代码
                stringBuilder.append(sourceCode.substring(0, openingBraceIndex))
                        .append(IO_REDIRECT_CODE)
                        .append(sourceCode.substring(openingBraceIndex));
                return stringBuilder.toString();
            } else {
                log.error("Pattern 'public static void main(String[] args)' not found.");
            }
        } else {
            log.error("Pattern 'public static void main(String[] args)' not found.");
        }
        return null;
    }

    /**
     * 编译 .java 源文件 得到字节码文件
     *
     * @param uuid 用户工作目录
     */
    @Override
    protected CompileResult compile(String uuid) {
        String srcPath = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid + File.separator + EXECUTION_CODE_FILE_NAME + JAVA_EXTENSION;
        String compiledCmd = String.format("javac -encoding utf-8 %s", srcPath);
        CompileResult compileResult;
        try {
            Process compileProcess = Runtime.getRuntime().exec(compiledCmd);
            compileResult = ProcessUtil.compile(compileProcess);
        } catch (IOException e) {
            compileResult = new CompileResult(false, e.getMessage());
        }
        return compileResult;
    }

    /**
     * 将标准输入重定向到/{app}/{uuid}/{input.txt}
     * 将标准输出重定向到/{app}/{uuid}/{output.txt}
     * 并执行 .class 文件
     *
     * @param uuid 用户的工作目录id
     * @return 执行结果
     */

    @Override
    protected ExecuteResult execute(String uuid, List<String> inputList) {
        String basicPath = File.separator + "app" + File.separator + uuid + File.separator;
        // 标准输出路径
        String dockerIn = basicPath + INPUT_FILE_NAME;
        // 标准输入路径
        String dockerOut = basicPath + OUTPUT_FILE_NAME;
        // 标准错误路径
        String dockerErr = basicPath + ERR_FILE_NAME;
        // 宿主机的输入路径
        String hostIn = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid + File.separator + INPUT_FILE_NAME;
        // 宿主机的输出路径
        String hostOut = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid + File.separator + OUTPUT_FILE_NAME;
        // 宿主机的错误路径
        String hostErr = USER_DIR + File.separator + CONTAINER_MOUNT_DIR + File.separator + uuid + File.separator + ERR_FILE_NAME;
        // 初始化java-docker容器
        DockerClient docker = DockerUtil.initializeDocker();
        // 获取java-docker容器id
        String containerId = DockerUtil.getDockerContainerId(docker);

        // 执行结果
        ExecuteResult result = new ExecuteResult();
        // 确保java-docker容器处于运行状态
        DockerUtil.ensureContainerRunning(docker, containerId);

        // 计时器
        Long maxTime = 0L;
        List<JudgeInfo> judgeInfoList = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            JudgeInfo judgeInfo = new JudgeInfo();
            // 将测试用例写入输入文件
            FileUtil.writeString(inputList.get(i), hostIn, UTF_8);
            Long time = 0L;
            StopWatch stopWatch = new StopWatch();
            // 将标准输入、标准输出、标准错误文件已命令行参数的形式输入给程序
            String[] cmdArray = new String[]{"java", "-cp", basicPath, "Main", dockerIn, dockerOut, dockerErr};
            final boolean[] timeOut = {true};
            ExecCreateCmdResponse execCreateCmdResponse = docker
                    .execCreateCmd(containerId)
                    .withTty(true)
                    .withCmd(cmdArray)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ExecStartResultCallback callback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    timeOut[0] = false;
                }
            };

            final long[] maxMemory = {0L};
            StatsCmd statsCmd = docker.statsCmd(containerId);
            // 统计程序消耗的内存
            ResultCallback<Statistics> memoryStatisticsCallback = new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    try {
                        maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage().longValue(), maxMemory[0]);
                    } catch (NullPointerException e) {
                        log.error("Memory statistics callback error: {}", e.getMessage());
                    }
                }

                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void close() {
                }
            };
            statsCmd.exec(memoryStatisticsCallback);
            String cmdId = execCreateCmdResponse.getId();
            if (cmdId != null) {
                try {
                    stopWatch.start();
                    // 执行程序
                    docker
                            .execStartCmd(cmdId)
                            .exec(callback)
                            .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                    stopWatch.stop();
                    interruptStatisticsThread();
                    time = stopWatch.getLastTaskTimeMillis();
                    statsCmd.close();
                    if (timeOut[0]) {
                        // 超时了，将本次此时用例返回给调用方
                        return new ExecuteResult(
                                time,
                                maxMemory[0],
                                ExecStateEnum.TIME_LIMIT_EXCEEDED.getValue(),
                                inputList.get(i),
                                FileUtil.readString(hostErr, UTF_8),
                                FileUtil.readString(hostOut, UTF_8)
                        );
                    }
                    // 没有超时，收集该测试用例的执行信息(执行成功就不需要记录输出了)
                    judgeInfo.setMemory(maxMemory[0]);
                    judgeInfo.setTime(time);
                    judgeInfo.setOut(FileUtil.readString(hostOut, UTF_8));
                    judgeInfoList.add(judgeInfo);
                } catch (InterruptedException e) {
                    // 线程被中断 系统错误
                    return new ExecuteResult(
                            time,
                            maxMemory[0],
                            ExecStateEnum.SYSTEM_ERROR.getValue(),
                            inputList.get(i),
                            FileUtil.readString(hostErr, UTF_8),
                            FileUtil.readString(hostOut, UTF_8)
                    );
                }
            } else {
                result.setTime(time);
                result.setIn(inputList.get(i));
                result.setOut(FileUtil.readString(hostOut, UTF_8));
                result.setErr(FileUtil.readString(hostErr, UTF_8));
                result.setMemory(maxMemory[0]);
                result.setStatus(ExecStateEnum.SYSTEM_ERROR.getValue());
            }
        }

        maxTime = 0L;
        Long maxMemory = 0L;

        // 执行成功，统计程序的最大执行时间，和最大内存使用，返回给判题机
        List<String> outputList = new ArrayList<>();
        for (JudgeInfo judgeInfo : judgeInfoList) {
            if (judgeInfo.getTime() > maxTime) {
                maxTime = judgeInfo.getTime();
            }
            if (judgeInfo.getMemory() > maxMemory) {
                maxMemory = judgeInfo.getMemory();
            }
            outputList.add(judgeInfo.getOut());
        }
        result.setOutputList(outputList);
        result.setTime(maxTime);
        result.setMemory(maxMemory);
        result.setStatus(ExecStateEnum.SUCCESS.getValue());
        return result;
    }

    /**
     * 运行代码沙箱，得到响应
     *
     * @param executeCodeRequest 执行代码请求
     * @return 响应
     */
    @Override
    public SandBoxResponse run(SandBoxRequest executeCodeRequest) {
        SandBoxResponse response = new SandBoxResponse();
        List<String> inputList = executeCodeRequest.getInputList();
        // 1. 保存代码到本地
        String uuid = save(executeCodeRequest.getCode(), executeCodeRequest.getLanguage());
        // 2. 编译代码
        CompileResult compileResult = compile(uuid);
        if (!compileResult.getOk()) {
            // 编译失败，只返回编译失败的原因，和编译失败的状态即可
            response.setOutputList(new ArrayList<>());
            response.setError(compileResult.getErrorMessage());
            response.setStatus(ExecStateEnum.COMPILE_ERROR.getValue());
            response.setJudgeInfo(new JudgeInfo());
            freeResources(uuid);
            return response;
        }
        // 3. 执行代码
        ExecuteResult executeResult = execute(uuid, inputList);
        if (executeResult.getStatus().equals(ExecStateEnum.SUCCESS.getValue())) {
            // 执行成功
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMemory(executeResult.getMemory());
            judgeInfo.setTime(executeResult.getTime());
            response.setJudgeInfo(judgeInfo);
            response.setOutputList(executeResult.getOutputList());
            response.setError("");
            response.setStatus(ExecStateEnum.SUCCESS.getValue());
        } else {
            // 执行失败
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMemory(executeResult.getMemory());
            judgeInfo.setTime(executeResult.getTime());
            response.setOutputList(Arrays.asList(executeResult.getOut()));
            response.setError(executeResult.getErr());
            response.setStatus(executeResult.getStatus());
            response.setJudgeInfo(judgeInfo);
        }

        freeResources(uuid);
        return response;
    }

    public static void main(String[] args) {
        SandBoxRequest request = new SandBoxRequest();
        request.setCode("import java.util.*;\n\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner sc = new Scanner(System.in);\n" +
                "        int[] arr = new int[4];\n" +
                "        for (int i = 0; i < 4; i++) arr[i] = sc.nextInt();\n" +
                "        int target = sc.nextInt();\n" +
                "        System.out.println(Arrays.toString(twoSum(arr, target)));\n" +
                "    }\n" +
                "\n" +
                "    public static int[] twoSum(int[] nums, int target) {\n" +
                "        Map<Integer, Integer> map = new HashMap<>();\n" +
                "        for (int i = 0; i < nums.length; i++) {\n" +
                "            if (map.containsKey(target - nums[i])) {\n" +
                "                return new int[]{map.get(target - nums[i]), i};\n" +
                "            }\n" +
                "            map.put(nums[i], i);\n" +
                "        }\n" +
                "        return new int[]{-1, -1};\n" +
                "    }\n" +
                "}");

        request.setLanguage("java");
        request.setInputList(Arrays.asList("2 7 11 15 9"));
        SandBoxResponse response = new JavaSandBox().run(request);
    }


}
