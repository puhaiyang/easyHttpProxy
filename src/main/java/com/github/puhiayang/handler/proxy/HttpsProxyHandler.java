package com.github.puhiayang.handler.proxy;

import com.github.puhiayang.bean.ClientRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 对https请求进行代理
 * created on 2019/10/25 18:00
 *
 * @author puhaiyang
 */
public class HttpsProxyHandler extends ChannelInboundHandlerAdapter implements IProxyHandler {
    @Override
    public void sendToServer(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {
        //todo
    }
}
