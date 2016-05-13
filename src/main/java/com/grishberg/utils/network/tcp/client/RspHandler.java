package com.grishberg.utils.network.tcp.client;

import com.grishberg.utils.network.interfaces.OnMessageListener;

/**
 * Created by grishberg on 12.05.16.
 */
public class RspHandler {
    private byte[] rsp = null;

    public synchronized boolean handleResponse(byte[] rsp) {
        this.rsp = rsp;
        this.notify();
        return true;
    }

    public synchronized void waitForResponse() {
        while(this.rsp == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        System.out.println(new String(this.rsp));
    }
}
