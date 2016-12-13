package sjsj.sizzletest;

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    TextView tv, nb;
    NotificationManager nm;
    SharedPreferences shared;
    SharedPreferences.Editor editor;
    Context context;
    private ProximityManagerContract proximityManager;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_ENABLE_BT =1, base_id=123;
    private static final String TAG = "MainActivity";
    HashMap<String, IEddystoneDevice> hm=new HashMap<>();
    HashMap<String, Double> distances=new HashMap<>();
    HashMap<String, Integer> not_ids=new HashMap<>();//To store notification ids corresponding to particular device
    TreeSet<MBeacon> set= new TreeSet<>(new MyComparator());//To find nearest beacon in logarithmic time
    class MBeacon{
        String id;
        double dist;
        MBeacon(String id, double dist){
            this.id=id;
            this.dist=dist;
        }
    }
    class MyComparator implements Comparator<MBeacon>{

        @Override
        public int compare(MBeacon o1, MBeacon o2) {
            if(o1.dist<o2.dist)
                return -1;
            else if(o2.dist<o1.dist)
                return 1;
            return o1.id.compareTo(o2.id);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            //Device suprts bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            try {
                if (ActivityCompat.checkSelfPermission(this, mPermission)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{mPermission}, REQUEST_CODE_PERMISSION);

                    // If any permission above not allowed by user, this condition will execute every time, else your else part will work
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {

            proximityManager = new ProximityManager(this);
            proximityManager.setEddystoneListener(createEddystoneListener());

            startScanning();
        }

        //SharedPreference to store notification id
        shared=this.getSharedPreferences(getString(R.string.notification_preference_key), Context.MODE_PRIVATE);

        tv= (TextView) findViewById(R.id.tv);
        nb= (TextView) findViewById(R.id.nearestBeacon);

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
                Log.d(TAG, "onEddystoneDiscovered: Beacon found"+eddystone.getName()+eddystone.getUrl()+eddystone.getDistance());
                tv.setText("Beacon found  "+eddystone.getUniqueId()+" Distance: "+eddystone.getDistance());
                Toast.makeText(MainActivity.this, "beacon found"+eddystone.getUniqueId(), Toast.LENGTH_SHORT).show();

                hm.put(eddystone.getUniqueId(), eddystone);
                set.add(new MBeacon(eddystone.getUniqueId(), eddystone.getDistance()));
                distances.put(eddystone.getUniqueId(), eddystone.getDistance());

                //Find nearest beacon
                MBeacon beacon=set.first();
                IEddystoneDevice nearestBeacon=hm.get(beacon.id);
                nb.setText("Nearest Beacon: "+nearestBeacon.getUniqueId()+" Distance: "+ nearestBeacon.getDistance());

                //Create notification
                final NotificationCompat.Builder mmNCB=new NotificationCompat.Builder(MainActivity.this).setSmallIcon(android.R.drawable.ic_menu_zoom).setContentTitle("BEACON FOUND");
                mmNCB.setContentText(eddystone.getUniqueId());

                int id=shared.getInt(getString(R.string.notification_id), base_id);
                not_ids.put(eddystone.getUniqueId(), id);
                nm.notify(id,mmNCB.build());

                //Update latest id in shared preferences
                //So that notification ids are unique
                id++;
                editor=shared.edit();
                editor.putInt(getString(R.string.notification_id), id);
                editor.apply();
                editor.commit();

            }

            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystonesUpdated: Beacon Updated  "+eddystones.get(0).getDistance());

                for(IEddystoneDevice eddystone: eddystones)
                {
                    hm.put(eddystone.getUniqueId(), eddystone);
                    double d=distances.get(eddystone.getUniqueId());
                    distances.put(eddystone.getUniqueId(), eddystone.getDistance());
                    //To update value in a set first remove and then reinsert
                    set.remove(new MBeacon(eddystone.getUniqueId(), d));
                    set.add(new MBeacon(eddystone.getUniqueId(), eddystone.getDistance()));
                }
                tv.setText("Beacon updated  "+eddystones.get(eddystones.size()-1).getUniqueId()+eddystones.get(eddystones.size()-1).getDistance());

                //Find nearest beacon
                MBeacon beacon=set.first();
                IEddystoneDevice nearestBeacon=hm.get(beacon.id);
                nb.setText("Nearest Beacon: "+nearestBeacon.getUniqueId()+" Distance: "+ nearestBeacon.getDistance());

            }

            @Override
            public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystoneLost: Beacon Lost");

                tv.setText("Beacon lost "+eddystone.getUniqueId());

                double x = distances.get(eddystone.getUniqueId());
                hm.remove(eddystone.getUniqueId());
                set.remove(new MBeacon(eddystone.getUniqueId(), x));
                //Cancel Notification
                nm.cancel(not_ids.get(eddystone.getUniqueId()));
                not_ids.remove(eddystone.getUniqueId());

                if(set.isEmpty())
                    nb.setText("No beacons nearby");
                else
                {
                    MBeacon beacon=set.first();
                    IEddystoneDevice nearestBeacon=hm.get(beacon.id);
                    nb.setText("Nearest Beacon: "+nearestBeacon.getUniqueId()+" Distance: "+ nearestBeacon.getDistance());
                }
            }
        };
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ) {

                // Success Stuff here

                proximityManager = new ProximityManager(context);
                proximityManager.setEddystoneListener(createEddystoneListener());

                startScanning();

            }
            else{
                // Failure Stuff
            }
        }

    }

}
