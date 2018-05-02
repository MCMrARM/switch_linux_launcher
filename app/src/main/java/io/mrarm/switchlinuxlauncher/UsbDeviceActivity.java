package io.mrarm.switchlinuxlauncher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import io.mrarm.switchlinuxlauncher.log.LogProxy;
import io.mrarm.switchlinuxlauncher.log.Logger;
import io.mrarm.switchlinuxlauncher.log.ui.LogView;

public class UsbDeviceActivity extends AppCompatActivity {

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
                        log.e("Permission denied - " + device);
                        finish();
                    }
                }
            }
        }

    };

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private PendingIntent usbPermissionIntent;

    private Logger logger = new Logger();
    private LogProxy log = new LogProxy(logger, "UsbDeviceActivity");

    private boolean inProgress = false;

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_device);

        statusText = findViewById(R.id.status_text);
        logger.addOutput((LogView) findViewById(R.id.log_view));

        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        usbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (usbDevice == null) {
            finish();
            return;
        }
        checkPermissionAndStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null && !device.equals(usbDevice)) {
            if (inProgress) {
                Toast.makeText(this, R.string.error_processing_other_device,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            usbDevice = device;
            checkPermissionAndStart();
        }
    }

    private void checkPermissionAndStart() {
        log.d("Device = " + usbDevice.getDeviceName());

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
        onOperationStarted();
        log.i("Permission granted");
        UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
        if (connection == null) {
            log.e("Opening the device failed");
            onOperationFailed();
            return;
        }
        new Thread(() -> {
            try {
                if (DeviceType.isDeviceRCM(usbDevice)) {
                    log.i("Initializing USB exploit");
                    ShofEL2 exploit = new ShofEL2(this, logger, usbDevice, connection);
                    log.i("Executing USB exploit");
                    exploit.run();
                } else if (DeviceType.isDeviceUBoot(usbDevice)) {
                    log.i("Starting IMX USB Loader");
                    ImxUsbLoader loader = new ImxUsbLoader(logger, usbDevice, connection);
                    loader.load("/sdcard/imxusb/switch.conf");
                }
                onOperationSucceeded();
            } catch (Throwable t) {
                log.e("An error has occurred", t);
                onOperationFailed();
            }
        }).start();
    }

    private synchronized void onOperationStarted() {
        inProgress = true;
        if (DeviceType.isDeviceUBoot(usbDevice))
            postStatus(R.string.state_loader_in_progress, R.color.status_in_progress);
        else
            postStatus(R.string.state_exploit_in_progress, R.color.status_in_progress);
    }

    private synchronized void onOperationSucceeded() {
        inProgress = false;
        if (DeviceType.isDeviceUBoot(usbDevice))
            postStatus(R.string.state_loader_success, R.color.status_success);
        else
            postStatus(R.string.state_exploit_success, R.color.status_success);
    }

    private synchronized void onOperationFailed() {
        inProgress = false;
        if (DeviceType.isDeviceUBoot(usbDevice))
            postStatus(R.string.state_loader_error, R.color.status_error);
        else
            postStatus(R.string.state_exploit_error, R.color.status_error);
    }

    private void postStatus(int strId, int colorId) {
        runOnUiThread(() -> {
            statusText.setText(strId);
            statusText.setBackgroundResource(colorId);
        });
    }

    @Override
    public void onBackPressed() {
        if (inProgress)
            return;
        super.onBackPressed();
    }
}
