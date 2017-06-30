package guepardoapps.lucahomeaccesscontrol.common.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

public class BroadcastController {

    private static String TAG = BroadcastController.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    public BroadcastController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public void SendSimpleBroadcast(@NonNull String broadcast) {
        _logger.Debug("Send Simple Broadcast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);

        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            int data) {
        _logger.Debug("Send Integer Broadcast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putInt(bundleName, data);
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntParcelableBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNamesInteger,
            int[] dataInteger,
            @NonNull String[] bundleNamesParcelable,
            @NonNull Object[] dataParcelables) {
        _logger.Debug("Send IntegerParcelable Broadcast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNamesInteger.length; index++) {
            broadcastData.putInt(bundleNamesInteger[index], dataInteger[index]);
        }
        for (int index = 0; index < bundleNamesParcelable.length; index++) {
            broadcastData.putParcelable(bundleNamesParcelable[index], (Parcelable) dataParcelables[index]);
        }
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntArrayBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNames,
            int[] data) {
        _logger.Debug("Send Integer Array Broadcast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNames.length; index++) {
            broadcastData.putInt(bundleNames[index], data[index]);
        }
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);

    }

    public void SendStringBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            @NonNull String data) {
        _logger.Debug("Send String Broadcast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putString(bundleName, data);
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);
    }

    public void SendSerializableBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            @NonNull Object model) {
        _logger.Debug("Send BroadCast: " + broadcast);

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putSerializable(bundleName, (Serializable) model);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendSerializableArrayBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNames,
            @NonNull Object[] models) {
        _logger.Debug("Send Serializable Broadcast: " + broadcast);

        if (bundleNames.length != models.length) {
            _logger.Warn("Cannot send broadcast! length are not equal!");
            return;
        }

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNames.length; index++) {
            broadcastData.putSerializable(bundleNames[index], (Serializable) models[index]);
        }
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);
    }
}
