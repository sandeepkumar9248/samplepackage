package com.sampleunittest.mylibrary;

public interface InAppListener {
    void offerReceived(String remoteId);
    void onFinished();
    void remoteUserDisconnected();
}
