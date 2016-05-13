package com.grishberg.utils.network.tcp.client;

import com.grishberg.utils.network.models.BaseAttachment;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by grishberg on 09.05.16.
 */
public class ClientAttachment extends BaseAttachment {
    public AsynchronousSocketChannel channel;
    public ByteBuffer buffer;
    public Thread mainThread;
    public boolean isRead;
    public SocketAddress clientAddr;
}
