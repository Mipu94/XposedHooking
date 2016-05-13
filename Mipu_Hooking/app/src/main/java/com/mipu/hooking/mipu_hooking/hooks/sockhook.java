package com.mipu.hooking.mipu_hooking.hooks;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by Athos on 4/23/2016.
 */
public class sockhook {

    public static void initHooking(final XC_LoadPackage.LoadPackageParam lpparam ) throws NoSuchMethodException {
        //tim class socket nhờ classloader của xposed
        Class<?> sock = XposedHelpers.findClass("java.net.Socket", lpparam.classLoader);
        //tìm contructor Socket(String,int)
        Constructor<?> msock = sock.getConstructor(String.class, int.class);
        //hook constructor này để lấy adđr và port
        XposedBridge.hookMethod(msock, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String addr = (String) param.args[0];
                int port = (int) param.args[1];
                Log.d("ANTT2012", "new socket: " + addr + " port :"+String.valueOf(port));
            }
        });
        //tìm và hook method  readLine của class BufferedReader để lấy được dữ liệu trước khi nó đọc lên
        Class<?> br = XposedHelpers.findClass("java.io.BufferedReader", lpparam.classLoader);
        Method read = br.getMethod("readLine");
        XposedBridge.hookMethod(read, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String recv = (String) param.getResult();
                Log.d("ANTT2012", "recv : " + recv);
            }
        });
        //tìm và hook method write của class Writer để in ra dữ liệu trước khi nó viết lên
        Class<?> bw = XposedHelpers.findClass("java.io.Writer", lpparam.classLoader);
        Method mwriter = bw.getMethod("write", String.class);
        XposedBridge.hookMethod(mwriter, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log((String) param.args[0]);
                Log.d("ANTT2012", "send : " + (String) param.args[0]);
            }
        });
    }
}
