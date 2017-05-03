package ipglobal.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ipglobal.webserver.WebServerClass;

/**
 * Created by oscarcid on 24/04/2017.
 */

public class WebServerService extends Service {

    private WebServerClass server = null;

    @Override
    public void onCreate() {
        super.onCreate();
        server = new WebServerClass(WebServerService.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.startServer();
            }
        }).start();
        //server = new WebServerClass(this);
        //server.startServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        server.stopServer();
        Intent receiverIntent = new Intent("ipglobal.webserver.webServiceReceiver");
        receiverIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        receiverIntent.setFlags(Intent.FLAG_RECEIVER_NO_ABORT);
        sendBroadcast(receiverIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        server.stopServer();
        super.onDestroy();
        Intent receiverIntent = new Intent("ipglobal.webserver.webServiceReceiver");
        receiverIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        receiverIntent.setFlags(Intent.FLAG_RECEIVER_NO_ABORT);
        sendBroadcast(receiverIntent);

    }

}
