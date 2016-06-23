package com.grishberg.utils.network;

import com.grishberg.utils.network.common.Utils;
import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnFinderConnectionEstablishedListener;
import com.grishberg.utils.network.interfaces.OnServerConnectionEstablishedListener;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by grishberg on 08.05.16.
 */
public class ServerFinderImpl implements ServerFinder, Runnable {
    public static final int TIMEOUT = 5000;
    private static final String PING_MESSAGE = "ping";
    public static final String LOCALHOST = "127.0.0.1";
    private static final String[] PROTOCOLS = new String[]{"wlan0", "eth0", "en0"};
    private Thread thread;
    private OnFinderConnectionEstablishedListener listener;
    private OnConnectionErrorListener errorListener;
    private final int udpPort;
    private final int backTcpPort;
    private boolean isStarted;
    private ServerSocketChannel serverSocketChannel;

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
    public void startListeningServers() {
        if (isStarted) {
            return;
        }
        thread = new Thread(this);

        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().setSoTimeout(TIMEOUT);
            serverSocketChannel.socket().bind(new InetSocketAddress(backTcpPort));
            serverSocketChannel.configureBlocking(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread.start();
        isStarted = true;
    }

    @Override
    public void findServer() {
        new Thread(sendPacketRunnable).start();
    }

    @Override
    public void stopListening() {
        errorListener = null;
        listener = null;
        if (thread != null) {
            thread.interrupt();
        }
        isStarted = false;
        if (serverSocketChannel != null) {
            try {
                System.out.println("stop listening connections");
                serverSocketChannel.socket().close();
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ByteBuffer buf = ByteBuffer.allocate(48);
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    if (listener != null) {
                        listener.onServerFound(socketAddressToString(socketChannel));
                    }
                    socketChannel.read(buf);
                    socketChannel.close();
                }
                // listen answer
            } catch (AsynchronousCloseException e) {
                break;
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e);
                }
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
                DatagramPacket sendPacket = new DatagramPacket(sendData,
                        sendData.length,
                        getBroadcastAddress(),
                        udpPort);
                socket.send(sendPacket);
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e);
                }
            }
        }
    };

    private InetAddress getBroadcastAddress() throws IOException {
        InetAddress address = Utils.getIpAddress(true, PROTOCOLS);
        if (address == null) {
            address = InetAddress.getByName(LOCALHOST);
        }
        byte[] addr = address.getAddress();
        addr[addr.length - 1] = (byte) 255;
        return InetAddress.getByAddress(addr);
    }

    private String socketAddressToString(SocketChannel socketChannel) {
        return socketChannel.socket().getInetAddress().getHostAddress();
    }
}
