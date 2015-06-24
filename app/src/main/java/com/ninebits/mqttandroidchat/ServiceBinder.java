package com.ninebits.mqttandroidchat;

import android.os.Binder;

/**
 * Created by 9bits on 6/24/15.
 */
public class ServiceBinder extends Binder {
    private ChatService chatService;
    private IMessageArrivedListener mListener;

    public ServiceBinder(ChatService chatService) {
        this.chatService = chatService;
    }

    ChatService getService() {
        return chatService;
    }

    public void setMessageArrivedListener(IMessageArrivedListener listener) {
        mListener = listener;
    }

    public void messageArrived(String message) {
        if (mListener != null)
            mListener.messageArrived(message);
    }
}
