package guepardoapps.lucahomeaccesscontrol.services.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import guepardoapps.library.toolset.common.Logger;

import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.services.RESTService;

public class RESTServiceController {

    private static final String TAG = RESTServiceController.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    public RESTServiceController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _logger.Debug("Created new " + TAG);
        _context = context;
    }

    public void SendRestAction(
            @NonNull String url,
            int port,
            @NonNull String action) {
        if (action.length() <= 0) {
            _logger.Error("Action is null!");
            return;
        }
        _logger.Debug("action: " + action);

        Intent serviceIntent = new Intent(_context, RESTService.class);

        Bundle serviceData = new Bundle();
        serviceData.putString(Bundles.REST_URL, url);
        serviceData.putString(Bundles.REST_PORT, String.valueOf(port));
        serviceData.putString(Bundles.REST_ACTION, action);

        serviceIntent.putExtras(serviceData);

        _context.startService(serviceIntent);
    }
}
