package com.github.puhiayang.handler.proxy;

import com.github.puhiayang.bean.ClientRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.puhiayang.bean.Constans.CLIENTREQUEST_ATTRIBUTE_KEY;


/**
 * socks的代理handler
 *
 * @author puhaiyang
 * created on 2019/10/25 20:56
 */
public class SocksProxyHandler extends ChannelInboundHandlerAdapter implements IProxyHandler {
    private Logger logger = LoggerFactory.getLogger(HttpsProxyHandler.class);

    private ChannelFuture notHttpReuqstCf;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("[SocksProxyHandler]");
        Attribute<ClientRequest> clientRequestAttribute = ctx.channel().attr(CLIENTREQUEST_ATTRIBUTE_KEY);
        ClientRequest clientRequest = clientRequestAttribute.get();
        sendToServer(clientRequest, ctx, msg);
    }

    @Override
    public void sendToServer(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {
        //不是http请求就不管，全转发出去
        if (notHttpReuqstCf == null) {
            //连接至目标服务器
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    // 复用客户端连接线程池
                    .channel(ctx.channel().getClass())
                    // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
                                    ctx.channel().writeAndFlush(msg);
                                }
                            });
                        }
                    });
            notHttpReuqstCf = bootstrap.connect(clientRequest.getHost(), clientRequest.getPort());
            notHttpReuqstCf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        } else {
            notHttpReuqstCf.channel().writeAndFlush(msg);
        }
    }

    @Override
    public void sendToClient(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {

    }
}