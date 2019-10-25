package com.github.puhiayang.utils;

import com.github.puhiayang.bean.ClientRequest;
import com.github.puhiayang.bean.Constans;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 代理请求工具类
 *
 * @author puhaiyang
 * created on 2019/10/25 23:12
 */
public class ProxyRequestUtil {
    private ProxyRequestUtil() {
    }

    /**
     * 获取代理请求
     *
     * @param httpRequest http请求
     */
    public static ClientRequest getClientReuqest(HttpRequest httpRequest) {
        //从header中获取出host
        String host = httpRequest.headers().get("host");
        //从host中获取出端口
        String[] hostStrArr = host.split(":");
        int port = 80;
        if (hostStrArr.length > 1) {
            port = Integer.parseInt(hostStrArr[1]);
        } else if (httpRequest.uri().startsWith(Constans.HTTPS_PROTOCOL_NAME)) {
            port = 443;
        }
        return new ClientRequest(hostStrArr[0], port);
    }
}
