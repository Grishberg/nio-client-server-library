package com.grishberg.utils.network;

import com.grishberg.utils.network.interfaces.OnConnectionErrorListener;
import com.grishberg.utils.network.interfaces.OnFinderConnectionEstablishedListener;
import com.grishberg.utils.network.interfaces.OnServerConnectionEstablishedListener;

/**
 * Created by grishberg on 08.05.16.
 */
public interface ServerFinder {
    void findServer();
    void release();
    void setConnectionListener(OnFinderConnectionEstablishedListener listener);
    void setErrorListener(OnConnectionErrorListener listener);
}
