package com.astro.kakaobot.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.InvocationTargetException;

public class JSScriptEngine {
    private String name;
    private String source;
    private ScriptThread thread;

    public JSScriptEngine() {
        thread = new ScriptThread();
    }

    public Context getContext() {
        return this.thread.getContext();
    }

    public ScriptableObject getScope() {
        return this.thread.getScope();
    }

    public void invoke(String func, Object... parameter) {
        this.thread.invoke(func, parameter);
    }

    public void run() {
        this.thread = new ScriptThread();
        this.thread.start();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScript(String str) {
        this.source = str;
    }

    public void stop() {
        this.thread.exit();
    }

    private class ScriptThread extends Thread {
        private Context context;
        private ScriptableObject scope;

        public void exit() {
            context.exit();
        }

        public Context getContext() {
            return context;
        }

        public ScriptableObject getScope() {
            return scope;
        }

        public void invoke(String func, Object... parameter) {
            Function function = (Function) scope.get(func);
            function.call(context, scope, scope, parameter);
        }

        @Override
        public void run() {
            context = Context.enter();
            context.setOptimizationLevel(-1);

            Script script = context.compileString(source, "", 0, null);
            scope = context.initStandardObjects();

            try {
                ScriptableObject.defineClass(scope, JSKakaoTalk.class);
                script.exec(context, scope);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
