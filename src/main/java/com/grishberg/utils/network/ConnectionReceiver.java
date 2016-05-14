package com.grishberg.utils.network;

import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnServerConnectionEstablishedListener;

/**
 * Created by grishberg on 08.05.16.
 */
public interface ConnectionReceiver {
    void start();
    void stop();
    void setConnectionListener(OnServerConnectionEstablishedListener listener);
    void setErrorListener(OnConnectionErrorListener listener);
}
