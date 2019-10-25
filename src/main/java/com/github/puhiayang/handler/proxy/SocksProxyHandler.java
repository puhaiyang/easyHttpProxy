package com.github.puhiayang.handler.proxy;

import com.github.puhiayang.bean.ClientRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * socks的代理handler
 *
 * @author puhaiyang
 * created on 2019/10/25 20:56
 */
public class SocksProxyHandler extends ChannelInboundHandlerAdapter implements IProxyHandler {
    @Override
    public void sendToServer(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {
        //todo
    }
}