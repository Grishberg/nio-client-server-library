package com.grishberg.utils.network;

import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnConnectionEstablishedListener;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;

/**
 * Created by grishberg on 08.05.16.
 */
public class ConnectionReceiverImpl implements ConnectionReceiver, Runnable {
    private OnConnectionEstablishedListener listener;
    private OnConnectionErrorListener errorListener;
    private final int udpPort;
    private final int backTcpPort;
    private DatagramSocket socket;
    private Thread thread;
    private SocketChannel socketChannel;

    public ConnectionReceiverImpl(int udpPort, int backTcpPort) {
        this.udpPort = udpPort;
        this.backTcpPort = backTcpPort;
        thread = new Thread(this);
    }

    @Override
    public void setConnectionListener(OnConnectionEstablishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setErrorListener(OnConnectionErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this udpPort
            socket = new DatagramSocket(udpPort, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                socketChannel = SocketChannel.open();
                //Receive a packet
                byte[] recvBuf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received with client ip
                String data = new String(packet.getData()).trim();
                //TODO: check signature
                connectToClient(packet.getAddress());
                if (listener != null) {
                    listener.onConnectionEstablished(packet.getAddress().getHostAddress());
                }
            }
        } catch (IOException ex) {
            if (errorListener != null) {
                errorListener.onError(ex);
            }
        } finally {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Connect to recipient and disconnect
     *
     * @param address
     * @throws IOException
     */
    private void connectToClient(InetAddress address) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(address, backTcpPort);
        socketChannel.connect(socketAddress);
        socketChannel.close();
    }

    private String addressToString(byte[] src) {
        return String.format("%d.%d.%d.%d", src[0], src[1], src[2], src[3]);
    }
}
