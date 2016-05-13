package com.grishberg.utils.network.tcp.client;

/**
 * Created by grishberg on 09.05.16.
 */
public interface TcpClient {
    void connect(String address, int port);
    void sendMessage(byte[] message);
    void sendMessage(String message);
    void release();
}
