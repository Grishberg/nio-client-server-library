package com.grishberg.utils.network.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by grishberg on 14.05.16.
 */
public abstract class BaseBufferedReader implements Runnable {
    private int needToRead;
    private final Map<SocketChannel, ByteBuffer> notCompletedBuffers = new ConcurrentHashMap<>();

    protected List<byte[]> convertFromLvContainer(SocketChannel socketChannel, ByteBuffer buffer, int numRead) {
        List<byte[]> buffers = new LinkedList<>();
        buffer.flip();
        int available;
        boolean isNeedInitBuffer;
        ByteBuffer byteBuffer = notCompletedBuffers.get(socketChannel);
        while (numRead > 0) {
            if (needToRead == 0) {
                needToRead = buffer.getInt();
                numRead -= 4;
                isNeedInitBuffer = byteBuffer == null;
                byteBuffer = ByteBuffer.allocate(needToRead);
                if (isNeedInitBuffer) {
                    notCompletedBuffers.put(socketChannel, byteBuffer);
                }
            }
            available = needToRead <= numRead ? needToRead : numRead;
            for (int i = 0; i < available; i++) {
                byteBuffer.put(buffer.get());
            }
            numRead -= available;
            needToRead -= available;
            if (needToRead == 0) {
                buffers.add(byteBuffer.array());
            }
        }
        return buffers;
    }
}
