package io.mrarm.switchlinuxlauncher;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler handler = new Handler();
    private Runnable scanRunnable = this::scanForDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(scanRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanForDevicesAndQueue();
    }

    public void scanForDevices() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        for (UsbDevice dev : manager.getDeviceList().values()) {
            Log.d(TAG, "USB device: " + Integer.toString(dev.getVendorId(), 16) + ":" +
                    Integer.toString(dev.getProductId(), 16));
            if ((dev.getVendorId() == 0x0955 && dev.getProductId() == 0x7321) ||
                    (dev.getVendorId() == 0x0955 && dev.getProductId() == 0x701a)) {
                Intent intent = new Intent(this, UsbDeviceActivity.class);
                intent.putExtra(UsbManager.EXTRA_DEVICE, dev);
                startActivity(intent);
                finish();
            }
        }
    }

    private void scanForDevicesAndQueue() {
        scanForDevices();
        handler.postDelayed(scanRunnable, 500);
    }

}
