package com.sampleunittest.mylibrary;

import android.content.Context;

import org.webrtc.SurfaceViewRenderer;

public class InAppSDK {

    public static void connectServer(String serverUrl, String selfId, ActionCallBack callBack) {
        InAppPeer.getInstance().connectServer(serverUrl, selfId, callBack);
    }

    public static void instaListener(InAppListener instaListener) {
        InAppPeer.getInstance().setListener(instaListener);
    }

    public static void initialise(Context context, SurfaceViewRenderer localView, SurfaceViewRenderer remoteView, boolean localMirror, boolean remoteMirror) {
        InAppPeer.getInstance().initialise(context, localView, remoteView, localMirror, remoteMirror);
    }

    public static void makeCall(String remoteId, ActionCallBack callBack) {
        InAppPeer.getInstance().makeCall(remoteId, callBack);
    }

    public static void answerCall(ActionCallBack callBack) {
        InAppPeer.getInstance().answerCall(callBack);
    }

    public static void disconnect() {
        InAppPeer.getInstance().disconnect();
    }

    public static void leave() {
        InAppPeer.getInstance().leave();
    }

    public static void audioMute() {
        InAppPeer.getInstance().audioMute();
    }

    public static void audioUnMute() {
        InAppPeer.getInstance().audioUnMute();
    }

    public static void videoMute() {
        InAppPeer.getInstance().videoMute();
    }

    public static void videoUnMute() {
        InAppPeer.getInstance().videoUnMute();
    }

    public static void switchCamera() {
        InAppPeer.getInstance().switchCamera();
    }

    /*public static void call(Context context, SurfaceViewRenderer localView, SurfaceViewRenderer remoteView, String serverUrl, String selfId, String remoteId) {
        InstaPeer.getInstance().connectServer(context, localView, remoteView, serverUrl, selfId, remoteId);
    }*/

}
