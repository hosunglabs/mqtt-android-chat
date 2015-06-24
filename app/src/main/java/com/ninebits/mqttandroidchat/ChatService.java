package com.ninebits.mqttandroidchat;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class ChatService extends Service  implements MqttCallback, MqttTraceHandler,
        IMqttActionListener {

    public static final String CLIENT_ID = "Client id";
    static final String TAG = "ChatActivity";

    private MqttAndroidClient mClient;
    private boolean mIsConnecting;
    private ServiceBinder mBinder = new ServiceBinder(this);

    public ChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws MqttException {
        SharedPreferences prefs = this.getSharedPreferences("default", MODE_PRIVATE);

        if (prefs.contains(CLIENT_ID) == false) {
            String clientId = java.util.UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CLIENT_ID, clientId);
            editor.commit();
        }

        String clientId = prefs.getString(CLIENT_ID, "");
        String server = "m20.cloudmqtt.com";
        String port = "18744";
        boolean cleanSession = false;

        String uri = "tcp://" + server + ":" + port;

        mClient = new MqttAndroidClient(this, uri, clientId);
        MqttConnectOptions conOpt = new MqttConnectOptions();

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(10000);
        conOpt.setKeepAliveInterval(600000);
        conOpt.setUserName("pykswtkr");
        conOpt.setPassword("8obiEj8vq-Rn".toCharArray());

        mClient.setCallback(this);
        mClient.setTraceCallback(this);

        mIsConnecting = true;
        mClient.connect(conOpt, null, this);
    }

    public void sendMessage(String message) {
        String topic = "office";
        String textToSend = message;
        int qos = 2;

        boolean retained = false;

        String[] args = new String[2];
        args[0] = textToSend;
        args[1] = topic+";qos:"+qos+";retained:"+retained;

        try {
            mClient.publish(topic, textToSend.getBytes(), qos, retained, null, this);
        }
        catch (MqttSecurityException e) {
            e.printStackTrace();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.i(TAG, "Connection lost");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        Log.i(TAG, mqttMessage.toString());
        mBinder.messageArrived(mqttMessage.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void traceDebug(String source, String message) {

    }

    @Override
    public void traceError(String source, String message) {

    }

    @Override
    public void traceException(String source, String message, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if(mIsConnecting) {
            mIsConnecting = false;
            subscribe();
        }
    }

    private void subscribe()
    {
        String topic = "office";
        int qos = 2;

        try {
            String[] topics = new String[1];
            topics[0] = topic;
            getClient().subscribe(topic, qos, null, this);
        }
        catch (MqttSecurityException e) {
            e.printStackTrace();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        Log.e(ChatService.TAG, "something went wrong: " + throwable.toString());
    }

    public MqttAndroidClient getClient() {
        return mClient;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
