package com.anxin.hybrid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/*
 * 从服务器获取xml解析并进行比对版本号 
 */
public class UpdateVersion extends CordovaPlugin{
    private String apkUrl = "";
    String UPDATE_SERVERAPK = "anxin_yl.apk";
    ProgressDialog pd = null; 
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try{
            if ("update".equals(action)) {
                this.apkUrl = args.getString(0);
                this.update(args.getString(0), callbackContext);
                return true;
            }
        } catch(Exception e) {
            callbackContext.error(e.getMessage());
        }
        return false;
    }
    public void update(final String url, CallbackContext callbackContext){
        pd = new ProgressDialog(webView.getContext()); 
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.show();
        new Thread(){
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();  
                HttpGet get = new HttpGet(url);  
                HttpResponse response;  
                try {  
                    response = client.execute(get);  
                    HttpEntity entity = response.getEntity();  
                    //long length = entity.getContentLength();  
                    pd.setMax((int) entity.getContentLength()); 
                    InputStream is =  entity.getContent();  
                    FileOutputStream fileOutputStream = null;  
                    if(is != null){  
                        File file = new File(Environment.getExternalStorageDirectory(),UPDATE_SERVERAPK);  
                        fileOutputStream = new FileOutputStream(file);  
                        byte[] b = new byte[1024];  
                        int charb = -1;  
                        int count = 0;  
                        while((charb = is.read(b))!=-1){  
                            fileOutputStream.write(b, 0, charb);  
                            count += charb;
                            //获取当前下载量  
                            pd.setProgress(count); 
                        }  
                    }  
                    fileOutputStream.flush();  
                    if(fileOutputStream!=null){  
                        fileOutputStream.close();  
                    }  
                    finishDownload();  
                }  catch (Exception e) {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();  
                }  
            }}.start();
    }
    
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);                  
            pd.cancel();  
            installApk();  
        }
    };
    
    /** 
     * 下载完成，通过handler将下载对话框取消 
     */  
    public void finishDownload(){  
        new Thread(){  
            public void run(){  
                Message message = handler.obtainMessage();  
                handler.sendMessage(message);  
            }  
        }.start();  
    }  

    /** 
     * 安装应用 
     */  
    public void installApk(){  
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(),UPDATE_SERVERAPK))  
                , "application/vnd.android.package-archive");  
        webView.getContext().startActivity(intent);  
    }  
}