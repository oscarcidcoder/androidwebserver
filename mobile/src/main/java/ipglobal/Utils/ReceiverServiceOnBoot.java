package ipglobal.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ipglobal.Services.WebServerService;

/**
 * Receiver para realizar la escucha cuando se inicie el dispositivo o se detiene
 * el servicio para asi iniciarlo de nuevo
 * Created by oscarcid on 24/04/2017.
 */

public class ReceiverServiceOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, WebServerService.class);
        context.startService(serviceIntent);
    }

}
