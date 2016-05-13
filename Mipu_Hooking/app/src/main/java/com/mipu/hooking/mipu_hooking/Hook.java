package com.mipu.hooking.mipu_hooking;
import com.mipu.hooking.mipu_hooking.hooks.RealDevice;
import com.mipu.hooking.mipu_hooking.hooks.httpHook;
import com.mipu.hooking.mipu_hooking.hooks.sockhook;
import com.mipu.hooking.mipu_hooking.hooks.testhook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Athos on 4/21/2016.
 */

public class Hook implements IXposedHookLoadPackage {

    //đây là được tự động gọi khi xposed load module này,nên ta sẽ dùng nó để chọn app và các hàm ta muốn hook
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        testhook.initHooking(lpparam);//import hàm test hook
        if (lpparam.packageName.equals("com.mipu.tcp.tcptest") )//chọn app ta muốn hook
        {
            sockhook.initHooking(lpparam);//chọn sockhook
            //httpHook.initHooking(lpparam);//chọn httphook
        }
    }
}
