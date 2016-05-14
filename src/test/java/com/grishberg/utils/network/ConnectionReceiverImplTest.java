package com.grishberg.utils.network;

import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnFinderConnectionEstablishedListener;
import com.grishberg.utils.network.interfaces.OnServerConnectionEstablishedListener;
import com.grishberg.utils.network.interfaces.OnMessageListener;
import com.grishberg.utils.network.tcp.client.TcpClientImpl;
import com.grishberg.utils.network.tcp.client.TcpClient;
import com.grishberg.utils.network.tcp.server.TcpServer;
import com.grishberg.utils.network.tcp.server.TcpServerImpl;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by grishberg on 08.05.16.
 */
public class ConnectionReceiverImplTest {
    public static final int SEND_MESSAGE_COUNT = 1000;
    private final Charset cs = Charset.forName("UTF-8");
    public static final int UDP_PORT = 5050;
    public static final int TIMEOUT = 100;
    public static final int BACK_TCP_PORT = 5051;
    public static final int PORT = 5052;
    public static final String PONG = "pong";
    private boolean isSuccessReceived;
    private boolean isSuccessEstablished;
    private boolean serverMessageReceived;
    private boolean clientMessageReceived;
    private TcpServer tcpServer;
    private TcpClient tcpClient;
    private int serverReceivedCount = 0;
    private int clientReceivedCount = 0;

    @Test
    public void testStart() throws Exception {
        long startTime = System.currentTimeMillis();
        isSuccessReceived = false;
        isSuccessEstablished = false;
        serverMessageReceived = false;
        clientMessageReceived = false;
        final CountDownLatch signal = new CountDownLatch(1);
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
        });
        tcpServer.start();
        tcpClient = new TcpClientImpl(new OnMessageListener() {
            @Override
            public void onReceivedMessage(String hostString, byte[] message) {
                clientMessageReceived = true;
                String msg = new String(message, cs);
                System.out.printf("tcpClient onReceivedMessage msg = %s\n", msg);
                //assertTrue("message from server not equals pong, but " + msg, PONG.equals(msg));
                clientReceivedCount++;
                signalClientMessageReceived.countDown();
            }
        }, new OnServerConnectionEstablishedListener() {
            @Override
            public void onConnectionEstablished(String address) {
            }
        }, null);

        ConnectionReceiver connectionReceiver = new ConnectionReceiverImpl(UDP_PORT, BACK_TCP_PORT);
        final ServerFinder serverFinder = new ServerFinderImpl(UDP_PORT, BACK_TCP_PORT);
        connectionReceiver.setConnectionListener(new OnServerConnectionEstablishedListener() {
            @Override
            public void onConnectionEstablished(String address) {
                isSuccessReceived = true;
                signal.countDown();
            }
        });

        connectionReceiver.setErrorListener(new OnConnectionErrorListener() {
            @Override
            public void onError(Throwable t) {
                assertTrue(t.getMessage(), false);
                signal.countDown();
            }
        });

        serverFinder.setConnectionListener(new OnFinderConnectionEstablishedListener() {
            @Override
            public void onServerFound(String address) {
                isSuccessEstablished = true;
                tcpClient.connect(address, PORT);
                tcpClient.sendMessage("test");
                signalFinder.countDown();
            }
        });
        serverFinder.setErrorListener(new OnConnectionErrorListener() {
            @Override
            public void onError(Throwable t) {
                assertTrue(t.getMessage(), false);
                signalFinder.countDown();
            }
        });
        // test
        connectionReceiver.start();
        serverFinder.findServer();
        // wait for connection
        signal.await(TIMEOUT, TimeUnit.SECONDS);
        signalClientMessageReceived.await(TIMEOUT, TimeUnit.SECONDS);
        System.out.printf("connection %d ms\n", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < SEND_MESSAGE_COUNT; i++) {
            tcpClient.sendMessage(String.format("test %d", i));
            //Thread.sleep(1);
        }
        System.out.printf("sent 1000 messages %d ms\n", System.currentTimeMillis() - startTime);
        Thread.sleep(10000);
        connectionReceiver.stop();
        serverFinder.release();
        assertTrue("not success received", isSuccessReceived);
        assertTrue("not success established", isSuccessEstablished);
        assertTrue("not success server message received", serverMessageReceived);
        assertTrue("not success client message received", clientMessageReceived);
        System.out.printf("server count = %d\n", serverReceivedCount);
        System.out.printf("client count = %d\n", clientReceivedCount);
    }
}