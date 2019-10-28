package com.github.puhiayang.handler.proxy;

import com.github.puhiayang.bean.ClientRequest;
import com.github.puhiayang.utils.HttpsSupport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.puhiayang.bean.Constans.CLIENTREQUEST_ATTRIBUTE_KEY;

/**
 * 对https请求进行代理
 * created on 2019/10/25 18:00
 *
 * @author puhaiyang
 */
public class HttpsProxyHandler extends ChannelInboundHandlerAdapter implements IProxyHandler {
    private Logger logger = LoggerFactory.getLogger(HttpsProxyHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("[HttpsProxyHandler]");
        ByteBuf byteBuf = (ByteBuf) msg;
        // ssl握手
        if (byteBuf.getByte(0) == 22) {
            Attribute<ClientRequest> clientRequestAttribute = ctx.channel().attr(CLIENTREQUEST_ATTRIBUTE_KEY);
            ClientRequest clientRequest = clientRequestAttribute.get();
            sendToServer(clientRequest, ctx, msg);
        }
    }

    @Override
    public void sendToServer(ClientRequest clientRequest, ChannelHandlerContext ctx, Object msg) {
        try {
            SslContext sslCtx = SslContextBuilder
                    .forServer(HttpsSupport.getInstance().getServerPriKey(), HttpsSupport.getInstance().getCert(clientRequest.getHost())).build();
            //接收客户端请求，将客户端的请求内容解码
            ctx.pipeline().addFirst("httpRequestDecoder", new HttpRequestDecoder());
            //发送响应给客户端，并将发送内容编码
            ctx.pipeline().addFirst("httpResponseEncoder", new HttpResponseEncoder());
            //http聚合
            ctx.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
            //ssl处理
            ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
            // 重新过一遍pipeline，拿到解密后的的http报文
            ctx.pipeline().fireChannelRead(msg);
            return;
        } catch (Exception e) {
            logger.error("[sendToServer] err:{}", e.getMessage());
        }
    }
}
