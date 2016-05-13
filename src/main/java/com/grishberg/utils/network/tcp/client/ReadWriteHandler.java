package com.grishberg.utils.network.tcp.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

/**
 * Created by grishberg on 09.05.16.
 */
public class ReadWriteHandler implements CompletionHandler<Integer, ClientAttachment> {
    @Override
    public void completed(Integer result, ClientAttachment attach) {
        if (attach.isRead) {
            attach.buffer.flip();
            Charset cs = Charset.forName("UTF-8");
            int limits = attach.buffer.limit();
            byte bytes[] = new byte[limits];
            attach.buffer.get(bytes, 0, limits);
            String msg = new String(bytes, cs);
            if (attach.messageListener != null) {
                attach.messageListener.onReceivedMessage(((InetSocketAddress) attach.clientAddr).getHostString(), bytes);
            }
            attach.buffer.clear();
        } else {
            attach.isRead = true;
            attach.buffer.clear();
            attach.channel.read(attach.buffer, attach, this);
        }
    }

    @Override
    public void failed(Throwable e, ClientAttachment attach) {
        e.printStackTrace();
    }

    private String getTextFromUser() throws Exception {
        System.out.print("Please enter a  message  (Bye  to quit):");
        BufferedReader consoleReader = new BufferedReader(
                new InputStreamReader(System.in));
        String msg = consoleReader.readLine();
        return msg;
    }
}
