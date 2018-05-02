package io.mrarm.switchlinuxlauncher;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler handler = new Handler();
    private Runnable scanRunnable = this::scanForDevices;

    private UsbDevice detectedDevice = null;

    private TextView statusText;
    private Button buttonHack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        buttonHack = findViewById(R.id.button_hack);

        buttonHack.setOnClickListener((View v) -> {
            if (detectedDevice == null)
                return;
            Intent intent = new Intent(this, UsbDeviceActivity.class);
            intent.putExtra(UsbManager.EXTRA_DEVICE, detectedDevice);
            startActivity(intent);
        });
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
        assert manager != null;
        detectedDevice = null;
        for (UsbDevice dev : manager.getDeviceList().values()) {
            Log.d(TAG, "USB device: " + Integer.toString(dev.getVendorId(), 16) + ":" +
                    Integer.toString(dev.getProductId(), 16));
            if (isSupportedDevice(dev)) {
                if (detectedDevice != null)
                    Log.w(TAG, "More than one supported device");
                detectedDevice = dev;
            }
        }
        if (detectedDevice == null)
            statusText.setText(R.string.state_waiting_for_device);
        else if (isDeviceRCM(detectedDevice))
            statusText.setText(R.string.state_device_connected_rcm);
        else if (isDeviceUBoot(detectedDevice))
            statusText.setText(R.string.state_device_connected_uboot);
        else
            statusText.setText(R.string.state_device_connected);
        buttonHack.setVisibility(detectedDevice != null ? View.VISIBLE : View.GONE);
    }

    private void scanForDevicesAndQueue() {
        scanForDevices();
        handler.postDelayed(scanRunnable, 500);
    }

    private boolean isDeviceRCM(UsbDevice dev) {
        return (dev.getVendorId() == 0x0955 && dev.getProductId() == 0x7321);
    }

    private boolean isDeviceUBoot(UsbDevice dev) {
        return (dev.getVendorId() == 0x0955 && dev.getProductId() == 0x701a);
    }

    private boolean isSupportedDevice(UsbDevice dev) {
        return isDeviceRCM(dev) || isDeviceUBoot(dev);
    }

}
