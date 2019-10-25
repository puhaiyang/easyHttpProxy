package com.github.puhaiyang;

import com.github.puhiayang.EasyHttpProxyServer;

/**
 * 启动类
 *
 * @author puhaiyang
 * created on 2019/10/25 22:34
 */
public class EasyHttpProxyServerStart {
    public static void main(String[] args) {
        EasyHttpProxyServer.getInstace().start(6667);
    }
}
