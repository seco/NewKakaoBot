package com.astro.kakaobot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import com.astro.kakaobot.script.JSScriptEngine;

import java.util.ArrayList;


public class KakaoTalkListener extends NotificationListenerService {
    private static final String KAKAOTALK_PACKAGE = "com.kakao.talk";
    private static Context context;
    private static ArrayList<JSScriptEngine> jsEngines = new ArrayList<>();
    private static ArrayList<Session> sessions = new ArrayList<>();

    public static void addJsEngine(JSScriptEngine engine) throws Exception {
        engine.run();

        jsEngines.add(engine);
    }

    public static void clearEngine() {
        jsEngines.clear();
        Log.d("KakaoBot/Listener", jsEngines.size() + "");
    }

    public static JSScriptEngine[] getJsEngines() {
        return jsEngines.toArray(new JSScriptEngine[0]);
    }

    public static ArrayList<Session> getSessions() {
        return sessions;
    }

    public static void send(String room, String message) throws IllegalArgumentException { // @author ManDongI
        Notification.Action session = null;

        for (Session i : sessions) {
            if (i.room.equals(room)) {
                session = i.session;

                break;
            }
        }

        if (session == null) {
            throw new IllegalArgumentException("Can't find the room");
        }

        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for (RemoteInput inputable : session.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), message);
        RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);

        try {
            session.actionIntent.send(context, 0, sendIntent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) { // @author ManDongI
        super.onNotificationPosted(statusBarNotification);

        Log.d("KakaoBot/Listener", "name: " + statusBarNotification.getPackageName());

        if (statusBarNotification.getPackageName().equals(KAKAOTALK_PACKAGE)) {
            Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());

            Log.d("KakaoBot/Listener", "Kakao!");

            for (Notification.Action act : extender.getActions()) {
                if (act.getRemoteInputs() != null && act.getRemoteInputs().length > 0) {
                    String title = statusBarNotification.getNotification().extras.getString("android.title");
                    Object index = statusBarNotification.getNotification().extras.get("android.text");

                    Type.Message message = parsingMessage(title, index);

                    context = getApplicationContext();

                    Session session = new Session();
                    session.session = act;
                    session.message = message.message;
                    session.sender = message.sender;
                    session.room = message.room;

                    sessions.add(session);

                    for (JSScriptEngine engine : jsEngines) {
                        try {
                            engine.invoke("talkReceivedHook", message.room, message.message, message.sender, !message.room.equals(message.sender));
                        } catch (final Exception e) {
                            MainActivity.UIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, e.toString().split("SCRIPTSPLITTAG")[1], Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private Type.Message parsingMessage(String title, Object index) {
        Type.Message result = new Type.Message();
        result.room = title;

        if (index instanceof String) {
            result.sender = title;
            result.message = (String) index;
        } else {
            String html = Html.toHtml((SpannableString) index);
            result.sender = Html.fromHtml(html.split("<b>")[1].split("</b>")[0]).toString();
            result.message = Html.fromHtml(html.split("</b>")[1].split("</p>")[0].substring(1)).toString();
        }

        return result;
    }

    public class Session {
        public String message;
        public String room;
        public String sender;
        public Notification.Action session;
    }
}
