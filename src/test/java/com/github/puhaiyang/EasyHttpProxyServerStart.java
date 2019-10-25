package com.github.puhaiyang;

import com.github.puhiayang.EasyHttpProxyServer;

public class EasyHttpProxyServerStart {
    public static void main(String[] args) {
        EasyHttpProxyServer.getInstace().start(6667);
    }
}
