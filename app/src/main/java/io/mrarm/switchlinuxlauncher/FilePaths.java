package io.mrarm.switchlinuxlauncher;

import android.content.Context;

import java.io.File;

public class FilePaths {

    public static File getShofEL2Dir(Context ctx) {
        return ctx.getExternalFilesDir("shofel2");
    }

    public static File getImxDir(Context ctx) {
        return ctx.getExternalFilesDir("imxusb");
    }

    public static File getImxConfigPath(Context ctx) {
        return new File(getImxDir(ctx), "switch.conf");
    }

}
