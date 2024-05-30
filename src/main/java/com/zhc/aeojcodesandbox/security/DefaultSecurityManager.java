package com.zhc.aeojcodesandbox.security;

/**
 * @author zhc
 * @description 默认的权限管理器
 * @date 2024/5/24 21:26
 **/
public class DefaultSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(java.security.Permission perm) {
        // 默认不做任何处理,检查所有权限
        System.out.println("DefaultSecurityManager");
        super.checkPermission(perm);
    }
}