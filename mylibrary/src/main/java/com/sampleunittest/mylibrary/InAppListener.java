package com.sampleunittest.mylibrary;

public interface InAppListener {
    void offerReceived(String remoteId);
    void answerReceived(String message);
    void onFinished();
    void remoteUserDisconnected();
}
