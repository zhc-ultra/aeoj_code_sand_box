package com.zhc.aeojcodesandbox.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * @author zhc
 * @description Docker 工具类
 * @date 2024/5/25 07:33
 **/
@Slf4j
public class DockerUtil {
    public static final String JAVA_DOCKER_CONTAINER_NAME = "java_code_sand_box";
    public static final String JAVA_DOCKER_IMAGE_NAME = "openjdk:8-alpine";
    public static final String JAVA_DOCKER_CONTAINER_WORK_DIR = "java-docker-work-dir";

    /**
     * 检查 java docker container 容器环境
     *
     * @param docker docker 客户端
     */
    public static void checkDockerContainer(DockerClient docker) {
        boolean containerExists = false;
        // 获取所有docker容器
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + JAVA_DOCKER_CONTAINER_NAME)) {
                    containerExists = true;
                    break;
                }
            }
            if (containerExists) {
                break;
            }
        }
        if (!containerExists) {
            // java docker container 未创建
            log.info("java docker 环境未初始化，尝试初始化 java docker 环境 ...");
            createDockerContainer(docker);
        } else {
            log.info("java docker 环境初始化完成");
        }
    }

    /**
     * 尝试拉取 java docker image
     *
     * @param docker docker 客户端
     */
    public static void tryPullJavaDockerImage(DockerClient docker) {
        PullImageCmd pullImageCmd = docker.pullImageCmd(JAVA_DOCKER_IMAGE_NAME);
        PullImageResultCallback callback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                // 拉取镜像时，分批完成回调
                log.info(item.toString());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd.exec(callback)
                    // 阻塞到下载完成
                    .awaitCompletion();
            log.info("拉取完成");
        } catch (InterruptedException e) {
            log.error("拉取 " + JAVA_DOCKER_IMAGE_NAME + " 镜像失败");
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建 名为 java_code_sand_box 的 java docker container
     *
     * @param docker docker 客户端
     */
    public static void createDockerContainer(DockerClient docker) {
        // 1. (尝试) 拉取 java docker image
        tryPullJavaDockerImage(docker);
        // 2. 创建 java docker container
        CreateContainerCmd createContainerCmd = docker.createContainerCmd(JAVA_DOCKER_IMAGE_NAME);
        HostConfig hostConfig = HostConfig.newHostConfig();
        String workspaceDir = System.getProperty("user.dir");
        String volumePath = workspaceDir + File.separator + JAVA_DOCKER_CONTAINER_WORK_DIR;
        // (容器挂载目录)创建容器时，指定文件路径(Volume)映射，将本地文件同步到容器中
        hostConfig.setBinds(new Bind(volumePath, new Volume("/app")));
        hostConfig.withMemory(100 * 100 * 1024L) // 限制容器的内存
                // linux 安全限制
                // .withSecurityOpts(Arrays.asList("seccomp=unconfined"))// 安全管理配置字符串
                .withMemorySwap(0L)
                .withCpuCount(1L);

        // 创建一个交互式容器，同时将需要执行的文件复制到docker容器
        CreateContainerResponse createContainerResponse = createContainerCmd
                .withName(JAVA_DOCKER_CONTAINER_NAME)
                // 将配置信息传入
                .withHostConfig(hostConfig)
                // 禁止docker容器,修改root容器的根目录文件
                .withReadonlyRootfs(true)
                // 禁止docker容器访问网络
                .withNetworkDisabled(true)
                // 将宿主机与 docker 容器的表准输入进行连接
                .withAttachStdin(true)
                // 将宿主机与 docker 容器的表准错误进行连接
                .withAttachStderr(true)
                // 将宿主机与 docker 容器的表准输出进行连接
                .withAttachStdout(true)
                // 为容器分配一个伪TTY（终端） 交互式容器。
                .withTty(true)
                .exec();

        log.info("创建容器 " + JAVA_DOCKER_CONTAINER_NAME + " 成功");
    }

    /**
     * 销毁 docker 镜像
     *
     * @param docker      docker客户端
     * @param containerId 容器 id
     */
    public static void destroyDockerContainer(DockerClient docker, String containerId) {
        log.info("销毁 docker 容器 " + containerId);
        try {
            docker.removeContainerCmd(containerId)
                    .withForce(true)
                    // 删除容器的时候，删除容器的卷
                    .withRemoveVolumes(true)
                    .exec();
            log.info("销毁 docker 容器 " + containerId + " 成功");
        } catch (Exception e) {
            log.error("销毁 docker 容器 {} 失败", containerId);
        }
    }

    /**
     * 获取 docker 容器 id
     *
     * @param docker docker客户端
     * @return 返回名为 java_code_sand_box 的容器 id
     */
    public static String getDockerContainerId(DockerClient docker) {
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + JAVA_DOCKER_CONTAINER_NAME)) {
                    return container.getId();
                }
            }
        }
        return null;
    }

    /**
     * 初始化 java docker 客户端
     *
     * @return java docker 客户端实例
     */
    public static DockerClient initializeDocker() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * 确保容器处于运行状态，如果docker容器未运行，那么就启动容器
     *
     * @param docker      docker 客户端
     * @param containerId 容器 id
     */
    public static void ensureContainerRunning(DockerClient docker, String containerId) {
        InspectContainerResponse containerResponse = docker.inspectContainerCmd(containerId).exec();
        if (Boolean.FALSE.equals(containerResponse.getState().getRunning())) {
            docker.startContainerCmd(containerId).exec();
        }
    }
}