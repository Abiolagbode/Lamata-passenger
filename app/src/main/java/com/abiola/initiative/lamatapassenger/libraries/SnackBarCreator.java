package com.abiola.initiative.lamatapassenger.libraries;

import android.support.design.widget.Snackbar;
import android.view.View;


public final class SnackBarCreator {
    private static String message;
    private static int resId;

    public static void set(String message) {
        SnackBarCreator.message = message;
    }

    public static void set(int resId) {
        SnackBarCreator.resId = resId;
    }

    public static void show(View view) {
        try {
            if (!(message.isEmpty() || message.matches("[\\s]+")))
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {}
        try{
            Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {}
        message = null;
        resId = -1;
    }

    public static void show(View view, boolean lengthLong) {
        try {
            if (!(message.isEmpty() || message.matches("[\\s]+")))
                Snackbar.make(view, message, (lengthLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)).show();
        } catch (Exception e) {}
        try{
            Snackbar.make(view, resId, (lengthLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)).show();
        } catch (Exception e) {}
        message = null;
        resId = -1;
    }
}
