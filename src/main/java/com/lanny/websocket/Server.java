package com.lanny.websocket;

import com.lanny.web.service.NetworkService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class Server {

    @Autowired
    NetworkService networkService;

    public void initServer(int port) {

        WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                networkService.getSockets().add(webSocket);
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                networkService.getSockets().remove(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String message) {
                networkService.messageHandler(webSocket, message, networkService.getSockets());
            }

            @Override
            public void onError(WebSocket webSocket, Exception e) {
                networkService.getSockets().remove(webSocket);
            }

            @Override
            public void onStart() {

            }
        };
        webSocketServer.start();
        System.out.println("listening websocket p2p port on: " + port);
    }

}
