package com.grishberg.utils.network;

import com.grishberg.utils.network.interfaces.*;
import com.grishberg.utils.network.tcp.server.TcpServer;
import com.grishberg.utils.network.tcp.server.TcpServerImpl;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by grishberg on 15.05.16.
 */
public class ServerTest {
    public static final int SEND_MESSAGE_COUNT = 1000;
    private final Charset cs = Charset.forName("UTF-8");
    public static final int UDP_PORT = 5001;
    public static final int TIMEOUT = 100;
    public static final int BACK_TCP_PORT = 5002;
    public static final int PORT = 5003;
    public static final String PONG = "pong";
    private boolean isSuccessReceived;
    private boolean isSuccessEstablished;
    private boolean serverMessageReceived;
    private boolean clientMessageReceived;
    private TcpServer tcpServer;
    private int serverReceivedCount = 0;
    private int clientReceivedCount = 0;

    @Test
    public void testStart() throws Exception {
        long startTime = System.currentTimeMillis();
        isSuccessReceived = false;
        isSuccessEstablished = false;
        serverMessageReceived = false;
        clientMessageReceived = false;
        final CountDownLatch broadcastConnectionSignal = new CountDownLatch(1);
        final CountDownLatch signalFinder = new CountDownLatch(1);
        final CountDownLatch signalServerMessageReceived = new CountDownLatch(1);
        final CountDownLatch signalClientMessageReceived = new CountDownLatch(1);
        tcpServer = new TcpServerImpl(PORT, new OnMessageListener() {
            @Override
            public void onReceivedMessage(String address, byte[] message) {
                String msg = new String(message, cs);
                System.out.printf("tcpServer onReceivedMessage address = %s, msg = %s\n", address, msg);
                serverMessageReceived = true;
                tcpServer.sendMessage(address, "response: " + msg);
                serverReceivedCount++;
                signalServerMessageReceived.countDown();
            }
        },
                new OnAcceptedListener() {
                    @Override
                    public void onAccepted(String address) {
                        System.out.printf("accepted from %s\n", address);
                    }
                }, new OnCloseConnectionListener() {
            @Override
            public void onCloseConnection(String address) {
                System.out.println("on connection close");
            }
        });
        tcpServer.start();

        ConnectionReceiver connectionReceiver = new ConnectionReceiverImpl(UDP_PORT, BACK_TCP_PORT);
        connectionReceiver.setConnectionListener(new OnServerConnectionEstablishedListener() {
            @Override
            public void onConnectionEstablished(String address) {
                isSuccessReceived = true;
                broadcastConnectionSignal.countDown();
            }
        });

        connectionReceiver.setErrorListener(new OnConnectionErrorListener() {
            @Override
            public void onError(Throwable t) {
                assertTrue(t.getMessage(), false);
                broadcastConnectionSignal.countDown();
            }
        });

        // listen incoming broadcast connection
        connectionReceiver.start();

        // wait for connection
        broadcastConnectionSignal.await(TIMEOUT, TimeUnit.SECONDS);
        signalClientMessageReceived.await(TIMEOUT, TimeUnit.SECONDS);

        connectionReceiver.stop();
        assertTrue("not success received", isSuccessReceived);
        //assertTrue("not success established", isSuccessEstablished);
        //assertTrue("not success server message received", serverMessageReceived);
        //assertTrue("not success client message received", clientMessageReceived);
        System.out.printf("server count = %d\n", serverReceivedCount);
        System.out.printf("client count = %d\n", clientReceivedCount);
    }

    @Test
    public void testServerFinder() throws Exception {
        ServerFinder serverFinder = new ServerFinderImpl(UDP_PORT, BACK_TCP_PORT);
        serverFinder.setErrorListener(new OnConnectionErrorListener() {
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        });
        serverFinder.setConnectionListener(new OnFinderConnectionEstablishedListener() {
            @Override
            public void onServerFound(String address, String serverName) {
                System.out.println("onServerFound " + address + " server name: " + serverName);
            }
        });
        serverFinder.startListeningServers();
        serverFinder.findServer();
        Thread.sleep(1000);
        serverFinder.stopListening();
    }
}