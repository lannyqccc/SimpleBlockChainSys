package com.lanny.websocket;

import com.lanny.web.service.NetworkService;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class Client {

    @Autowired
    NetworkService networkService;

    public void connectToPeer(String address) {

        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI(address)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    //apply to get the latest block.
                    networkService.write(this, networkService.applyTheLatestBlock());
                    networkService.getSockets().add(this);
                }

                @Override
                public void onMessage(String message) {
                    networkService.messageHandler(this, message, networkService.getSockets());
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    networkService.getSockets().remove(this);
                }

                @Override
                public void onError(Exception e) {
                    networkService.getSockets().remove(this);
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
