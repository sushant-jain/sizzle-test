package sjsj.sizzletest;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    NotificationManager nm;
    private ProximityManagerContract proximityManager;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KontaktSDK.initialize("YBVNhCQdbYqQtkxOZyAPmyshZkQXhKUl");
        setContentView(R.layout.activity_main);

        proximityManager = new ProximityManager(this);
        proximityManager.setEddystoneListener(createEddystoneListener());


        tv= (TextView) findViewById(R.id.tv);

        Intent intent = new Intent();
        intent.setData(new Uri.Builder()
                .scheme("sjTestIntent")
                .authority("host")
                .appendPath("path")
                .build());
        intent.setPackage("sjsj.sizzletest");
        Log.i(TAG, "Use this intent url: " + intent.toUri(intent.URI_INTENT_SCHEME));
       //BeaconIntentService.startActionFoo(this,"abc","xyz");
        //BeaconIntentService.startActionBaz(this,"abc","xyz");
    }

    @Override
    protected void onStart() {

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startScanning();
        super.onStart();
    }

    @Override
    protected void onStop() {
       //proximityManager.stopScanning();
        super.onStop();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }
//
//    @Override
//    protected void onDestroy() {
//        proximityManager.disconnect();
//        super.onDestroy();
//    }

    private EddystoneListener createEddystoneListener(){
        return new EddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystoneDiscovered: Beacon found"+eddystone.getName()+eddystone.getUrl()+eddystone.);
                tv.setText("Beacon found  "+eddystone.getUniqueId()+eddystone.getDistance());
                Toast.makeText(MainActivity.this, "beacon found"+eddystone.getUniqueId(), Toast.LENGTH_SHORT).show();
                final NotificationCompat.Builder mmNCB=new NotificationCompat.Builder(MainActivity.this).setSmallIcon(android.R.drawable.ic_menu_zoom).setContentTitle("BEACON FOUND");
                mmNCB.setContentText(eddystone.getUniqueId());
                nm.notify(123,mmNCB.build());

            }

            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystonesUpdated: Beacon Updated  "+eddystones.get(0).getDistance());
                tv.setText("Beacon updated  "+eddystones.get(0).getUniqueId()+eddystones.get(0).getDistance());

            }

            @Override
            public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystoneLost: Beacon Lost");
                tv.setText("Beacon lost "+eddystone.getUniqueId());
            }
        };
    }
}
