package com.grishberg.utils.network.tcp;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by grishberg on 14.05.16.
 * Read stream and parse packets.
 */
public abstract class BaseBufferedReader implements Runnable {
    private int needToRead;
    private final Map<SocketChannel, ByteBuffer> notCompletedBuffers = new ConcurrentHashMap<>();

    protected List<byte[]> convertFromLvContainer(SocketChannel socketChannel,
                                                  ByteBuffer buffer,
                                                  int numRead) {
        List<byte[]> buffers = new LinkedList<>();
        buffer.flip();
        int available;
        ByteBuffer byteBuffer = notCompletedBuffers.get(socketChannel);
        while (numRead > 0) {
            if (needToRead == 0) {
                needToRead = buffer.getInt();
                numRead -= 4;
                byteBuffer = ByteBuffer.allocate(needToRead);
                notCompletedBuffers.put(socketChannel, byteBuffer);
            }
            if (byteBuffer == null) {
                System.out.println("byteBuffer is null, create template buffer");
                byteBuffer = ByteBuffer.allocate(numRead);
            }
            available = needToRead <= numRead ? needToRead : numRead;
            int i = 0;
            try {
                for (i = 0; i < available; i++) {
                    byteBuffer.put(buffer.get());
                }
            } catch (BufferOverflowException e) {
                System.out.println(e.getMessage() + " " + i);
            }
            numRead -= available;
            needToRead -= available;
            if (needToRead == 0) {
                buffers.add(byteBuffer.array());
            }
        }
        return buffers;
    }

    protected void onDisconnect(SocketChannel socketChannel) {
        System.out.println("onDisconnect " + socketChannel);
        if (socketChannel != null) {
            notCompletedBuffers.remove(socketChannel);
        }
    }

    protected void onStopped() {
        System.out.println("onStopped ");
        notCompletedBuffers.clear();
    }
}
