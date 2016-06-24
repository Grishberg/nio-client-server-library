package com.grishberg.utils.network.interfaces;

/**
 * Created by grishberg on 14.05.16.
 */
public interface OnFinderConnectionEstablishedListener {
    /**
     * Событие нахождения сервера
     * @param address
     * @param serverName
     */
    void onServerFound(String address, String serverName);
}
