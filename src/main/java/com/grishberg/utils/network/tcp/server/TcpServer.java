package com.grishberg.utils.network.tcp.server;

import com.grishberg.utils.network.interfaces.OnMessageListener;

/**
 * Created by grishberg on 08.05.16.
 */
public interface TcpServer {
    void start();

    void stop();

    boolean sendMessage(String address, byte[] msg);

    boolean sendMessage(String address, String msg);
}
