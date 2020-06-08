package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    public void onMessageReceived(RemoteMessage remote){
        if(remote.getData().size() > 0){
            handleNow();
        }

        if(remote.getNotification() != null){
            String getMessage = remote.getNotification().getBody();

            Map<String, String> MessageMap = new HashMap<>();
            assert getMessage != null;
            MessageMap.put("key", getMessage);

            Intent intent = new Intent("alert_data");
            intent.putExtra("msg", getMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void handleNow(){

    }

    public void onNewToken(String newToken){
        super.onNewToken(newToken);

        sendRegistrationToServer(newToken);
    }

    private void sendRegistrationToServer(String token){

    }
}
