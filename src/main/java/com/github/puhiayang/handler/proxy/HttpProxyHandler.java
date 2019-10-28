package com.github.puhiayang.handler.proxy;

import com.github.puhiayang.bean.ClientRequest;
import com.github.puhiayang.bean.Constans;
import com.github.puhiayang.handler.response.HttpProxyResponseHandler;
import com.github.puhiayang.utils.ProxyRequestUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.puhiayang.bean.Constans.CLIENTREQUEST_ATTRIBUTE_KEY;

/**
 * 对http请求进行代理
 * created on 2019/10/25 18:00
 *
 * @author puhaiyang
 */
public class HttpProxyHandler extends ChannelInboundHandlerAdapter implements IProxyHandler {
    private Logger logger = LoggerFactory.getLogger(HttpProxyHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("[HttpProxyHandler]");
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            //获取客户端请求
            ClientRequest clientRequest = ProxyRequestUtil.getClientRequest(ctx.channel());
            if (clientRequest == null) {
                //从本次请求中获取
                Attribute<ClientRequest> clientRequestAttribute = ctx.channel().attr(CLIENTREQUEST_ATTRIBUTE_KEY);
                clientRequest = ProxyRequestUtil.getClientReuqest(httpRequest);
                //将clientRequest保存到channel中
                clientRequestAttribute.setIfAbsent(clientRequest);
            }
            //如果是connect代理请求，返回成功以代表代理成功
            if (sendSuccessResponseIfConnectMethod(ctx, httpRequest.method().name())) {
                logger.debug("[HttpProxyHandler][channelRead] sendSuccessResponseConnect");
                ctx.channel().pipeline().remove("httpRequestDecoder");
                ctx.channel().pipeline().remove("httpResponseEncoder");
                ctx.channel().pipeline().remove("httpAggregator");
                ReferenceCountUtil.release(msg);
                return;
            }
            if (clientRequest.isHttps()) {
                //https请求不在此处转发
                super.channelRead(ctx, msg);
                return;
            }
            sendToServer(clientRequest, ctx, msg);
            return;
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 如果是connect请求的话，返回连接建立成功
     *
     * @param ctx        ChannelHandlerContext
     * @param methodName 请求类型名
     * @return 是否为connect请求
     */
    private boolean sendSuccessResponseIfConnectMethod(ChannelHandlerContext ctx, String methodName) {
        if (Constans.CONNECT_METHOD_NAME.equalsIgnoreCase(methodName)) {
            //代理建立成功
            //HTTP代理建立连接
            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, Constans.CONNECT_SUCCESS);
            ctx.writeAndFlush(response);
            return true;
        }
        return false;
    }


    @Override
    public void sendToServer(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                // 注册线程池
                .channel(ctx.channel().getClass())
                // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        //添加接收远程server的handler
                        ch.pipeline().addLast(new HttpRequestEncoder());
                        ch.pipeline().addLast(new HttpResponseDecoder());
                        ch.pipeline().addLast(new HttpObjectAggregator(6553600));
                        //代理handler,负责给客户端响应结果
                        ch.pipeline().addLast(new HttpProxyResponseHandler(ctx.channel()));
                    }
                });

        //连接远程server
        ChannelFuture cf = bootstrap.connect(clientRequest.getHost(), clientRequest.getPort());
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    //连接成功
                    future.channel().writeAndFlush(msg);
                    logger.debug("[operationComplete] connect remote server success!");
                } else {
                    //连接失败
                    logger.error("[operationComplete] 连接远程server失败了");
                    ctx.channel().close();
                }
            }
        });
    }

    @Override
    public void sendToClient(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {

    }
}
