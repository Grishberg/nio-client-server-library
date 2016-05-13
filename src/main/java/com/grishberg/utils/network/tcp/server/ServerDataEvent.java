package com.grishberg.utils.network.tcp.server;

import java.nio.channels.SocketChannel;

/**
 * Created by grishberg on 12.05.16.
 */
class ServerDataEvent {
    public TcpServerImpl server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(TcpServerImpl server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}
