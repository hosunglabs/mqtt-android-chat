package com.ninebits.mqttandroidchat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

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
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;


public class ChatActivity extends ActionBarActivity implements MqttCallback, MqttTraceHandler,
        IMqttActionListener {

    public static final String CLIENT_ID = "Client id";
    private static final String TAG = "ChatActivity";
    private TextView messages;
    private EditText message;
    private ScrollView scrollView;
    private MqttAndroidClient client;
    private boolean isConnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messages = (TextView) findViewById(R.id.messages);
        message = (EditText) findViewById(R.id.message);
        scrollView = (ScrollView) findViewById(R.id.textAreaScroller);

        try {
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    private void connect() throws MqttException {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        if (prefs.contains(CLIENT_ID) == false) {
            String clientId = java.util.UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CLIENT_ID, clientId);
            editor.commit();
        }

        String clientId = prefs.getString(CLIENT_ID, "");
        String server = "192.168.0.21";
        String port = "1883";
        boolean cleanSession = false;

        String uri = "tcp://" + server + ":" + port;

        client = new MqttAndroidClient(this, uri, clientId);
        MqttConnectOptions conOpt = new MqttConnectOptions();

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(10000);
        conOpt.setKeepAliveInterval(600000);
        conOpt.setUserName("android phone");
        conOpt.setPassword("android phone".toCharArray());

        getClient().setCallback(this);
        getClient().setTraceCallback(this);

        isConnecting = true;
        getClient().connect(conOpt, null, this);
    }

    /**
     * Subscribe to a topic that the user has specified
     */
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view) {
        String topic = "office";
        String textToSend = this.message.getText().toString();
        int qos = 2;

        boolean retained = false;

        String[] args = new String[2];
        args[0] = textToSend;
        args[1] = topic+";qos:"+qos+";retained:"+retained;

        try {
            client.publish(topic, textToSend.getBytes(), qos, retained, null, this);
        }
        catch (MqttSecurityException e) {
            e.printStackTrace();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }

        message.setText("");
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        messages.setText(messages.getText() + "\n" + mqttMessage.toString());
        scrollView.fullScroll(View.FOCUS_DOWN);
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

    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if(isConnecting) {
            isConnecting = false;
            subscribe();
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        Log.e(TAG, "something went wrong: " + throwable.toString());
    }

    public MqttAndroidClient getClient() {
        return client;
    }

    public void setClient(MqttAndroidClient client) {
        this.client = client;
    }
}
