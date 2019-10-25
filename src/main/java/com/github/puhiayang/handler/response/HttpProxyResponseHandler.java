package com.github.puhiayang.handler.response;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * http代理responseHandler
 *
 * @author puhaiyang
 * created on 2019/10/25 23:25
 */
public class HttpProxyResponseHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(HttpProxyResponseHandler.class);
    private Channel clientChannel;

    public HttpProxyResponseHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        logger.info("[channelRead] content:{}", response.content().toString(Charset.defaultCharset()));
        //发送给客户端
        clientChannel.writeAndFlush(msg);
    }
}
