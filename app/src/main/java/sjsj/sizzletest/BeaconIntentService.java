package sjsj.sizzletest;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BeaconIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "sjsj.sizzletest.action.FOO";
    private static final String ACTION_BAZ = "sjsj.sizzletest.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "sjsj.sizzletest.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "sjsj.sizzletest.extra.PARAM2";

    public BeaconIntentService() {
        super("BeaconIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);

        return START_STICKY;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BeaconIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BeaconIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            KontaktSDK.initialize("YBVNhCQdbYqQtkxOZyAPmyshZkQXhKUl");
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        int i=1;
        Log.d(TAG, "handleActionFoo: ");
        NotificationCompat.Builder mNCB=new NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.ic_menu_zoom).setContentTitle("SJ ROXX").setContentText(String.valueOf(i));
        int t= (int) SystemClock.uptimeMillis();
        NotificationManager nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1,mNCB.build());
        i++;
        while(true){
            if(SystemClock.uptimeMillis()-t>500){
                t= (int) SystemClock.uptimeMillis();
                mNCB.setContentText(String.valueOf(i));
                nm.notify(1,mNCB.build());
                i++;
                Log.d(TAG, "handleActionFoo: "+i);
            }
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        scanBeacon();
    }

    private void scanBeacon(){
        proximityManager = new ProximityManager(this);
        proximityManager.setEddystoneListener(createEddystoneListener());
        startScanning();
    }

    private ProximityManagerContract proximityManager;


    private EddystoneListener createEddystoneListener(){
        final NotificationCompat.Builder mmNCB=new NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.ic_menu_zoom).setContentTitle("BEACON FOUND");

        return new EddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystoneDiscovered: Beacon found from service"+eddystone.getName()+eddystone.getUrl());
                mmNCB.setContentText(eddystone.getUniqueId());
                NotificationManager nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(123,mmNCB.build());
            }

            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystonesUpdated: Beacon Updated");
            }

            @Override
            public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.d(TAG, "onEddystoneLost: Beacon Lost");
            }
        };
    }

    @Override
    public void onDestroy() {
        proximityManager.stopScanning();
        proximityManager.disconnect();
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }
}
