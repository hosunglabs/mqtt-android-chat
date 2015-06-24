package com.ninebits.mqttandroidchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class ChatActivity extends ActionBarActivity implements IMessageArrivedListener {

    private TextView mMessages;
    private EditText mMessage;
    private ScrollView mScrollView;
    private boolean mBound;
    private ChatService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessages = (TextView) findViewById(R.id.messages);
        mMessage = (EditText) findViewById(R.id.message);
        mScrollView = (ScrollView) findViewById(R.id.textAreaScroller);

        Intent startServiceIntent = new Intent(this, ChatService.class);
        startService(startServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ServiceBinder binder = (ServiceBinder) service;
            mService = binder.getService();
            binder.setMessageArrivedListener(ChatActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void sendMessage(View view) {
        mService.sendMessage(this.mMessage.getText().toString());
        mMessage.setText("");
    }

    @Override
    public void messageArrived(String message) {
        mMessages.setText(mMessages.getText() + "\n" + message.toString());
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
