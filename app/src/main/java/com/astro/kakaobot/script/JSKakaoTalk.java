package com.astro.kakaobot.script;

import com.astro.kakaobot.KakaoTalkListener;
import com.astro.kakaobot.MainActivity;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class JSKakaoTalk extends ScriptableObject {
    public JSKakaoTalk() {
    }

    @JSStaticFunction
    public static Object getContext() {
        Context jsContext = KakaoTalkListener.getJsEngines()[0].getContext();
        ScriptableObject scope = KakaoTalkListener.getJsEngines()[0].getScope();

        return jsContext.javaToJS(MainActivity.getContext(), scope);
    }

    @JSStaticFunction
    public static void send(String room, String message) {
        if (message == null) {
            KakaoTalkListener.Session[] sessions = KakaoTalkListener.getSessions().toArray(new KakaoTalkListener.Session[0]);
            KakaoTalkListener.send(sessions[sessions.length - 1].room, message);
        } else {
            KakaoTalkListener.send(room, message);
        }
    }

    @Override
    public String getClassName() {
        return "KakaoTalk";
    }
}
