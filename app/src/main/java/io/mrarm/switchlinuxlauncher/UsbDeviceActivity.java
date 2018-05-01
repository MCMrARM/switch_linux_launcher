package io.mrarm.switchlinuxlauncher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class UsbDeviceActivity extends AppCompatActivity {

    private static final String TAG = "UsbDeviceActivity";

    private static final String ACTION_USB_PERMISSION =
            "io.mrarm.switchlinuxlauncher.UsbDeviceActivity.USB_PERMISSION";

    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (!(usbDevice.equals(device)))
                        return;

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
                            false)) {
                        onPermissionGranted();
                    } else {
                        Log.d(TAG, "Permission denied - " + device);
                        finish();
                    }
                }
            }
        }

    };

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private PendingIntent usbPermissionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_device);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        usbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (usbDevice == null) {
            finish();
            return;
        }

        Log.d(TAG, "Device id = " + usbDevice.getDeviceId());

        if (getIntent().getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            onPermissionGranted();
        else
            requestPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbPermissionIntent != null)
            unregisterReceiver(usbPermissionReceiver);
    }

    private void requestPermission() {
        if (usbPermissionIntent == null) {
            usbPermissionIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(usbPermissionReceiver, filter);
        }
        usbManager.requestPermission(usbDevice, usbPermissionIntent);
    }

    public void onPermissionGranted() {
        Log.d(TAG, "Permission granted");
    }

}
