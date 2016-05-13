package com.mipu.hooking.mipu_hooking.hooks;

import android.graphics.Color;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Athos on 4/21/2016.
 */
public class testhook  {
    //class này có chức năng thay đổi đồng hồ bằng dòng chữ xanh QA ANTT2012
    public static void initHooking(XC_LoadPackage.LoadPackageParam lpparam) {
        XC_MethodHook hookclock = new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            }

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TextView tv = (TextView) param.thisObject;
                String text = tv.getText().toString();
                text="QA ANTT2012 :) ";
                tv.setText(text);
                tv.setTextColor(Color.GREEN);
            }
        };
        //nếu packetname là systemui thì sẽ bắt đầu hook
        if (lpparam.packageName.equals("com.android.systemui"))
            try {
                //hook method updateclock của class policy.Clock
                XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock",hookclock);
            }
        catch (Error e)
        {

        }
    }

}