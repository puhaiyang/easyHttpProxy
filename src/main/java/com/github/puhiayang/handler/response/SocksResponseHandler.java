package com.github.puhiayang.handler.response;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * socks代理responseHandler
 * created on 2019/10/28 15:56
 *
 * @author puhaiyang
 */
public class SocksResponseHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(SocksResponseHandler.class);
    private Channel clientChannel;

    public SocksResponseHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //直接返回给客户端
        ctx.channel().writeAndFlush(msg);
    }
}
