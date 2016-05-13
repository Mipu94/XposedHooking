package com.mipu.hooking.mipu_hooking.hooks;
import android.os.Build;
import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.getObjectField;


/**
 * Created by Athos on 4/21/2016.
 */
public class httpHook  {

    public static void initHooking(XC_LoadPackage.LoadPackageParam lpparam) throws NoSuchMethodException {

       //xposed tìm class java.net.HttpURLConnection
        final Class <?> httpUrlConnection = findClass("java.net.HttpURLConnection",lpparam.classLoader);

        //hook các constructor của class HttpURLConnection để lấy httpURLConnection
        hookAllConstructors(httpUrlConnection, new XC_MethodHook() {
            @Override //trước khi getoutputstream
            protected void beforeHookedMethod(MethodHookParam param) {
                //nếu không dúng class và len=1 thì thoát
                if (param.args.length != 1 || param.args[0].getClass() != URL.class)
                    return;
                //log lại url mà app gửi ra ngoài
                XposedBridge.log("HttpURLConnection: " + param.args[0] + "");
            }
        });

        //tạo object XC_MethodHook để sử dụng cho việc hook method getOutputStream ở dưới
        XC_MethodHook RequestHook = new XC_MethodHook() {
            @Override
            protected  void beforeHookedMethod(MethodHookParam param) throws Throwable{
                //lấy object HttpURLConnection để tìm địa chỉ đã gửi ra
                HttpURLConnection urlConn = (HttpURLConnection)param.thisObject;

                if(urlConn!=null)
                {
                    StringBuilder sb = new StringBuilder();
                    boolean isconnected = (boolean) getObjectField(param.thisObject,"connected");
                    if(!isconnected)
                    {   //lấy header của http request
                        Map<String,List<String>> properties = urlConn.getRequestProperties();

                        if(properties!=null && properties.size()>0)
                        {
                            for(Map.Entry<String,List<String>> entry : properties.entrySet())
                            {
                                sb.append(entry.getKey()+":"+entry.getValue()+"\n");
                            }
                        }
                        Log.d("ANTT2012","HTTPrequest "+ "REQUEST: \nmethod="+urlConn.getRequestMethod()+" \nURL="+urlConn.getURL().toString()
                                        +"\nHeader : "+ sb.toString()
                        );
                    }
                }
            }

        };
        //tạo object cho response hook
        XC_MethodHook ResponseHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                HttpURLConnection urlconn = (HttpURLConnection) param.thisObject;

                int code=urlconn.getResponseCode();
                if(code == 200){//nếu reponse code =200 (status :ok) thì in ra thông tin url,method,header
                    Map<String, List<String>> properties = urlconn.getHeaderFields();

                    StringBuilder sb = new StringBuilder();
                    if(properties!=null && properties.size()>0){
                        for(Map.Entry<String,List<String>> entry:properties.entrySet())
                        {
                            sb.append(entry.getKey()+":"+entry.getValue()+"\n");
                        }
                        Log.d("ANTT2012","RESPONSE: method :"+urlconn.getRequestMethod()+"\nURL:"+urlconn.getURL().toString()+"\nheader: "+ sb.toString() );
                    }
                }
            }
        };
        //hàm findAndHookMethod dùng để hook 1 method mà chúng ta mong muốn trong 1 class nào đó,ở đây là class OutputStream với method là write
        //cần classloader và kiểu giá trị args của hàm write
        findAndHookMethod("java.io.OutputStream", lpparam.classLoader, "write", byte[].class,int.class,int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream)param.thisObject;
                if(!os.toString().contains("internal.http")) //nếu không phải outputstream kểu http thì return
                    return;
                String print = new String((byte[]) param.args[0]);
                Log.d("DATA",print);
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if(match.matches())
                {
                    Log.d("ANTT2012 : ","POST DATA: "+print);
                }
            }
        });

        //method này tương tự như write ở trên chỉ khác nhau về args truyền vào chỉ là byte[]
        findAndHookMethod("java.io.OutputStream", lpparam.classLoader, "write", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream)param.thisObject;
                if(!os.toString().contains("internal.http"))
                    return;
                String print = new String((byte[]) param.args[0]);
                Log.d("ANTT2012 : ","DATA: "+print);
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if(match.matches())
                {
                    Log.d("ANTT2012 : ","POST DATA: "+print);
                }
            }
        });

        //vì API23 android đã loại bỏ okhttp nên ta cần phải checkversion của android trước khi hook
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

                findAndHookMethod("libcore.net.http.HttpURLConnectionImpl", lpparam.classLoader, "getOutputStream", RequestHook);
            } else {
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", lpparam.classLoader, "getOutputStream", RequestHook);
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", lpparam.classLoader, "getInputStream", ResponseHook);
            }
        } catch (Error e){
        }

    }
}
