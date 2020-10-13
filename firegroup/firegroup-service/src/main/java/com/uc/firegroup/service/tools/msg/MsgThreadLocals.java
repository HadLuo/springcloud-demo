package com.uc.firegroup.service.tools.msg;

import com.uc.external.bilin.res.MqCallbackMessage;

public class MsgThreadLocals {

    private static final ThreadLocal<MqCallbackMessage> local = new ThreadLocal<>();

    public static void set(MqCallbackMessage value) {
        local.set(value);
    }

    public static MqCallbackMessage get() {
        return local.get();
    }

    public static void clear() {
        local.remove();
    }

}
