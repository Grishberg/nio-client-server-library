package com.grishberg.utils.network.tcp.server;

import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by grishberg on 12.05.16.
 */
public class EchoWorker implements Runnable {
    private List queue = new LinkedList();

    public void processData(TcpServerImpl server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    public void run() {
        ServerDataEvent dataEvent;

        while(true) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
            }

            // Return to sender
            //dataEvent.server.send(dataEvent.socket, dataEvent.data);
        }
    }
}