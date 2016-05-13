package com.mipu.hooking.mipu_hooking.hooks;
import android.os.Build;
import android.telephony.gsm.GsmCellLocation;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Athos on 4/22/2016.
 */
public class RealDevice {
    public static void initHooking(final XC_LoadPackage.LoadPackageParam lpparam ) throws NoSuchMethodException {
        HashMap<String,String> mapfied = new HashMap<String,String>();
        mapfied.put("MANUFACTURER","PANTECH");
        mapfied.put("BOARD","APQ8064Pro");
        mapfied.put("MODEL","IM-A870L");
        mapfied.put("BRAND", "VEGA");
        mapfied.put("HARDWARE", "qcom");
        mapfied.put("DEVICE", "ef52l");
        mapfied.put("FINGERPRINT", "VEGA/VEGA_IM-A870L/ef52l:4.4.2/KOT49H/IM-A870L.010:user/release-keys");
        mapfied.put("SERIAL", "5df93606");
        mapfied.put("PRODUCT", "cm_ef521");
        //mapfied.put("CPU_ABI", "mycpu");
        mapfied.put("DISPLAY", "A build ID string");



        for(Map.Entry <String,String>entry: mapfied.entrySet() )
            XposedHelpers.setStaticObjectField(Build.class,entry.getKey(),entry.getValue());
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getDeviceId", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("356172050717749");
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getNetworkCountryIso", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("vn");
                return;
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getNetworkOperator", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("45201");
                return;
            }
        });

       XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getCellLocation", new XC_MethodHook() {
           @Override
           protected void afterHookedMethod(MethodHookParam param) throws Throwable {
               GsmCellLocation a = new GsmCellLocation();
               a.setLacAndCid(22090, 687741);
               param.setResult(a);
           }
       });


        //bypass check special emulator file

        Class<?> file = XposedHelpers.findClass("java.io.File",lpparam.classLoader);
        Method exists = file.getMethod("exists");
        XposedBridge.hookMethod(exists, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                File file = (File)param.thisObject;
                String name = file.getAbsolutePath();
                String[] emufile = {"/dev/socket/qemud", "/sys/qemu_trace",
                        "/system/lib/libc_malloc_debug_qemu.so", "/dev/qemu_pipe","/system/bin/qemu-props"};
                for (String emu : emufile){
                    if(name.equals(emu)) {
                        XposedBridge.log("check file detected : "+name);
                        //param.setResult(false);
                        return;
                    }
                }
            }
        });


        //bypass read cpuinfo
        Class<?>  pb = XposedHelpers.findClass("java.lang.ProcessBuilder", lpparam.classLoader);
        Method start= pb.getMethod("start");
        XposedBridge.hookMethod(start ,new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                XposedBridge.log("nope");
                /*String[] arrcmd = (String[]) param.args[0];
                if(arrcmd.length>0)
                {
                    for(int i=0;i<arrcmd.length;i++){
                        if( arrcmd[i].contains("/proc/cpuinfo"))
                        {
                            XposedBridge.log("Read file cpuinfo Detected -> /tmp/cpuinfo");
                            arrcmd[i]="/tmp/cpuinfo";
                            param.args[0] = arrcmd;
                        }
                    }
                }*/
            }
        });



    }
}
