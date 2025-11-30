package org.example.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private GameWebSocketHandler gameWebSocketHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP codec
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));

        // WebSocket handshake handler
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, false));

        // ⭐ FIX: aggregate fragmented WebSocket frames into 1 UTF-8 message
        pipeline.addLast(new io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator(65536));

        // Now your handler always receives proper full JSON text frames
        pipeline.addLast(gameWebSocketHandler);
    }
}
