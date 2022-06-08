package com.sampleunittest.mylibrary;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InAppPeer {

    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final int VIDEO_RESOLUTION_WIDTH = 320;
    private static final int VIDEO_RESOLUTION_HEIGHT = 240;
    private static final int VIDEO_FPS = 30;

    private String remoteUserId;
    private WebSocketClient webSocketClient;
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;
    //FOR DISPOSE
    private PeerConnectionFactory mPeerConnectionFactory;
    private SurfaceTextureHelper mSurfaceTextureHelper;
    private VideoCapturer mVideoCapturer;
    private PeerConnection mPeerConnection;
    private SurfaceViewRenderer mLocalSurfaceView, mRemoteSurfaceView;


    private String stunUrl;
    private String udpUrl;
    private String tcpUrl;
    private String userName;
    private String credential;

    private static InAppPeer instance = null;

    private InAppListener instaListener;
    private boolean isInitialised = false;

    private InAppPeer() {
    }

    protected static InAppPeer getInstance() {
        if (instance == null) {
            instance = new InAppPeer();
        }
        return instance;
    }

    public void setListener(InAppListener listener) {
        instaListener = listener;
    }

    protected void connectServer(String serverUrl, String selfId, ActionCallBack callBack) {
        if (webSocketClient == null || webSocketClient.isClosed()) {
            webSocketClient = new WebSocketClient(URI.create(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    //Log.d("testcase", "onOpen " + webSocketClient.getConnection().isOpen());
                    if (!isInitialised) {
                        setUserID(selfId);
                        callBack.onSuccess("Connected to server");
                        isInitialised = true;
                    }
                }

                @Override
                public void onMessage(String message) {
                    if (!TextUtils.isEmpty(message)) {
                        try {
                            JSONObject mainJson = new JSONObject(message);
                            String type = mainJson.getString("type");
                            switch (type) {
                                case "hello":
//                                    if (mPeerConnection == null) {
//                                        mPeerConnection = createPeerConnection();
//                                    }
                                    break;
                                case "iceServers":
                                    JSONArray iceArrayMain = mainJson.getJSONArray("iceServers");
                                    try {
                                        JSONObject jsonObjectOne = iceArrayMain.getJSONObject(0);
                                        stunUrl = jsonObjectOne.getString("urls");
                                    } catch (JSONException e) {
                                        Log.d("testcase", "Exception " + e.getMessage());
                                    }
                                    try {
                                        JSONObject jsonObjectTwo = iceArrayMain.getJSONObject(1);
                                        JSONArray turnArray = jsonObjectTwo.getJSONArray("urls");
                                        udpUrl = turnArray.getString(0);
                                        tcpUrl = turnArray.getString(1);
                                        userName = jsonObjectTwo.getString("username");
                                        credential = jsonObjectTwo.getString("credential");
                                    } catch (JSONException e) {
                                        Log.d("testcase", "Exception " + e.getMessage());
                                    }
                                    break;

                                case "offer": {
                                    remoteUserId = mainJson.getString("id");
                                    onRemoteOfferReceived(mainJson, remoteUserId);
                                    break;
                                }

                                case "answer":
                                    onRemoteAnswerReceived(mainJson);
                                    break;

                                case "candidate":
                                    onRemoteCandidateReceived(mainJson);
                                    break;

                                case "bye":
//                                    leave();
                                    instaListener.remoteUserDisconnected();
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("testcase", e.getMessage());
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (code != 1000) {
//                        abcCaller();
                        reconnectIfNecessary();
                    } else {
                        isInitialised = false;
                    }
                }

                @Override
                public void onError(Exception ex) {
                    callBack.onFailure(ex.getMessage());
                }
            };
            try {
                webSocketClient.connectBlocking();
            } catch (InterruptedException e) {
                callBack.onFailure(e.getMessage() + " Trying to reconnect");
                reconnectIfNecessary();
            }
        } else {
            callBack.onSuccess("already connected to server");
        }

    }

    private void setUserID(String userid) {
        JSONObject jsonMain = new JSONObject();
        try {
            jsonMain.put("type", "userid");
            jsonMain.put("value", userid);
            send(jsonMain.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void initialise(Context context, SurfaceViewRenderer localView, SurfaceViewRenderer remoteView, boolean localMirror, boolean remoteMirror) {
        mLocalSurfaceView = localView;
        mRemoteSurfaceView = remoteView;

        EglBase mRootEglBase = EglBase.create();

        mLocalSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        mLocalSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalSurfaceView.setMirror(localMirror);
        mLocalSurfaceView.setEnableHardwareScaler(false);

        mRemoteSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        mRemoteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteSurfaceView.setMirror(remoteMirror);
        mRemoteSurfaceView.setEnableHardwareScaler(true);
        mRemoteSurfaceView.setZOrderMediaOverlay(true);

        //CAN INITIALIZE SEPARATE
        mPeerConnectionFactory = createPeerConnectionFactory(context, mRootEglBase);
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        mVideoCapturer = createVideoCapturer(context);
        mVideoCapturer.initialize(mSurfaceTextureHelper, context, videoSource.getCapturerObserver());
        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(mLocalSurfaceView);
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        mAudioTrack.setEnabled(true);

        //TO START LOCAL CAPTURE
        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);

    }

    protected void initialise(Context context) {

        EglBase mRootEglBase = EglBase.create();

        //CAN INITIALIZE SEPARATE
        mPeerConnectionFactory = createPeerConnectionFactory(context, mRootEglBase);
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        mVideoCapturer = createVideoCapturer(context);
        mVideoCapturer.initialize(mSurfaceTextureHelper, context, videoSource.getCapturerObserver());
        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(mLocalSurfaceView);
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        mAudioTrack.setEnabled(true);

        //TO START LOCAL CAPTURE
        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);

    }

    private PeerConnectionFactory createPeerConnectionFactory(Context context, EglBase mRootEglBase) {
        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(mRootEglBase.getEglBaseContext(),
                true,
                true);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory
                .InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(decoderFactory)
                .setVideoEncoderFactory(encoderFactory);
        builder.setOptions(null);

        return builder.createPeerConnectionFactory();
    }

    private VideoCapturer createVideoCapturer(Context context) {
        if (Camera2Enumerator.isSupported(context)) {
            return createCameraCapturer(new Camera2Enumerator(context));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) return videoCapturer;
            }
        }

        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) return videoCapturer;
            }
        }

        return null;
    }

    private PeerConnection createPeerConnection() {

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        if (stunUrl != null) {
            PeerConnection.IceServer stun = PeerConnection.IceServer.builder(stunUrl).createIceServer();
            iceServers.add(stun);
        }
        if (udpUrl != null) {
            PeerConnection.IceServer udp = PeerConnection.IceServer.builder(udpUrl).setUsername(userName).setPassword(credential).createIceServer();
            iceServers.add(udp);
        }
        if (tcpUrl != null) {
            PeerConnection.IceServer tcp = PeerConnection.IceServer.builder(tcpUrl).setUsername(userName).setPassword(credential).createIceServer();
            iceServers.add(tcp);
        }

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.enableDtlsSrtp = true;
        //rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        PeerConnection connection = mPeerConnectionFactory.createPeerConnection(rtcConfig, mPeerConnectionObserver);
        if (connection == null) {
            return null;
        }
        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        connection.addTrack(mVideoTrack, mediaStreamLabels);
        connection.addTrack(mAudioTrack, mediaStreamLabels);

        return connection;
    }

    private PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d("testcase", "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d("testcase", "onIceConnectionChange: " + iceConnectionState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d("testcase", "onIceConnectionChange: " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d("testcase", "onIceGatheringChange: " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

            try {
                JSONObject childObj = new JSONObject();
                childObj.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                childObj.put("sdpMid", iceCandidate.sdpMid);
                childObj.put("candidate", iceCandidate.sdp);

                JSONObject message = new JSONObject();
                message.put("type", "candidate");
                message.put("id", getRemoteUserId());
                message.put("candidate", childObj);

                try {
                    send(message.toString());
                } catch (Exception e) {
                    Log.d("testcase", "@@@@@@@@@@Exp " + e.getMessage());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            mPeerConnection.removeIceCandidates(iceCandidates);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d("testcase", "onAddStream: " + mediaStream.videoTracks.size());
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d("testcase", "onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d("testcase", "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d("testcase", "onRenegotiationNeeded");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            MediaStreamTrack track = rtpReceiver.track();
            if (track instanceof VideoTrack) {
                Log.d("testcase", "onAddVideoTrack");
                VideoTrack remoteVideoTrack = (VideoTrack) track;
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(mRemoteSurfaceView);
            }
        }
    };

    private String getRemoteUserId() {
        return remoteUserId;
    }

    private void onRemoteOfferReceived(JSONObject message, String remoteId) {
        Log.d("testcase", "offReceived " + message.toString());
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }

        try {
            String description = message.getString("sdp");
            mPeerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(
                            SessionDescription.Type.OFFER,
                            description));
            answerCall();
//            instaListener.offerReceived(remoteId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("testcase", "offReceived Exp " + e.getMessage());
        }
    }

    private void onRemoteAnswerReceived(JSONObject message) {
        Log.d("testcase", "ansReceived " + message.toString());
        try {
            String description = message.getString("sdp");
            mPeerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(
                            SessionDescription.Type.ANSWER,
                            description));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("testcase", "ansReceived Exp " + e.getMessage());
        }
    }

    private void onRemoteCandidateReceived(JSONObject message) {
        try {
            JSONObject childJson = message.getJSONObject("candidate");
            IceCandidate remoteIceCandidate =
                    new IceCandidate(childJson.getString("sdpMid"), childJson.getInt("sdpMLineIndex"), childJson.getString("candidate"));

            mPeerConnection.addIceCandidate(remoteIceCandidate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void makeCall(String remoteId, ActionCallBack callBack) {
        remoteUserId = remoteId;
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }

        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        mPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i("testcase", "Create local offer success: \n" + sessionDescription.description);
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("id", getRemoteUserId());
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    //aaa
                    send(message.toString());
                    callBack.onSuccess("Offer sent success");
                } catch (JSONException e) {
                    e.printStackTrace();
                    callBack.onFailure("Offer failed " + e.getMessage());

                }
            }
        }, mediaConstraints);
    }

    protected void answerCall() {
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        Log.d("testcase", "Create answer ...");
        mPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d("testcase", "Create answer success !");
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(),
                        sessionDescription);

                JSONObject message = new JSONObject();
                try {
                    message.put("id", getRemoteUserId());
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    //aaa
                    send(message.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    protected void disconnect() {
        JSONObject message = new JSONObject();
        try {
            message.put("id", getRemoteUserId());
            message.put("type", "bye");
            send(message.toString());
        } catch (JSONException e) {
            Log.d("testcase", "Disconnect " + e.getMessage());
            e.printStackTrace();
        }
        leave();
    }

    protected void leave() {
        try {
            mVideoCapturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mPeerConnection != null) {
            mPeerConnection.close();
            mPeerConnection = null;
        }

        mLocalSurfaceView.release();
        mRemoteSurfaceView.release();
        mVideoCapturer.dispose();
        mSurfaceTextureHelper.dispose();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        mPeerConnectionFactory.dispose();

        webSocketClient.close();

        instaListener.onFinished();
    }

    protected void audioMute() {
        mAudioTrack.setEnabled(false);
    }

    protected void audioUnMute() {
        mAudioTrack.setEnabled(true);
    }

    protected void videoMute() {
        mVideoTrack.setEnabled(false);
    }

    protected void videoUnMute() {
        mVideoTrack.setEnabled(true);
    }

    protected void switchCamera() {
        if (mVideoCapturer != null) {
            if (mVideoCapturer instanceof CameraVideoCapturer) {
                CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) mVideoCapturer;
                cameraVideoCapturer.switchCamera(null);
            }
        }
    }

    public synchronized void reconnectIfNecessary() {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (webSocketClient.isClosed() || webSocketClient.isClosing()) {
                try {
                    webSocketClient.reconnectBlocking();
                } catch (InterruptedException e) {
                    Log.d("testcase", "Recon " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 500, TimeUnit.MICROSECONDS);
    }


    public boolean isOpen() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public boolean isClosed() {
        return webSocketClient != null && webSocketClient.isClosed();
    }

    public void send(String msg) {
        if (isOpen()) {
            webSocketClient.send(msg);
        } else {
            Log.e("testcase", "Can't send message. WebSocket is null or closed");
        }
    }


}
