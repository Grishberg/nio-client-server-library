package com.grishberg.utils.network;

import com.grishberg.utils.network.common.Utils;
import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnFinderConnectionEstablishedListener;
import com.grishberg.utils.network.interfaces.OnServerConnectionEstablishedListener;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by grishberg on 08.05.16.
 */
public class ServerFinderImpl implements ServerFinder, Runnable {
    public static final int TIMEOUT = 5000;
    private static final String PING_MESSAGE = "ping";
    public static final String WLAN = "wlan0";
    public static final String LOCALHOST = "127.0.0.1";
    private Thread thread;
    private OnFinderConnectionEstablishedListener listener;
    private OnConnectionErrorListener errorListener;
    private final int udpPort;
    private final int backTcpPort;

    public ServerFinderImpl(int udpPort, int backTcpPort) {
        this.udpPort = udpPort;
        this.backTcpPort = backTcpPort;
        thread = new Thread(this);
    }

    @Override
    public void setConnectionListener(OnFinderConnectionEstablishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setErrorListener(OnConnectionErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public void findServer() {
        thread.start();
    }

    @Override
    public void release() {
        errorListener = null;
        listener = null;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(48);
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().setSoTimeout(TIMEOUT);
            serverSocketChannel.socket().bind(new InetSocketAddress(backTcpPort));
            serverSocketChannel.configureBlocking(true);
            new Thread(sendPacketRunnable).start();
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (listener != null) {
                listener.onServerFound(socketAddressToString(socketChannel));
            }
            socketChannel.read(buf);
            socketChannel.close();
            serverSocketChannel.close();
            // listen answer

        } catch (IOException e) {
            if (errorListener != null) {
                errorListener.onError(e);
            }
        }
    }

    private Runnable sendPacketRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //Open a udpPort to send the package
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                byte[] sendData = PING_MESSAGE.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), udpPort);
                socket.send(sendPacket);
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e);
                }
            }
        }
    };

    private InetAddress getBroadcastAddress() throws IOException {
        InetAddress address = Utils.getIpAddress(true, WLAN);
        if (address == null) {
            address = InetAddress.getByName(LOCALHOST);
        }
        return address;
    }

    private String socketAddressToString(SocketChannel socketChannel) {
        return socketChannel.socket().getInetAddress().getHostAddress();
    }
}
