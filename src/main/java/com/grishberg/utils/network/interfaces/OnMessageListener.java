package com.grishberg.utils.network.interfaces;

/**
 * Created by grishberg on 08.05.16.
 */
public interface OnMessageListener {
    void onReceivedMessage(String address, byte[] message);
}
