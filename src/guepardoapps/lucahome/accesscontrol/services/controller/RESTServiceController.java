package guepardoapps.lucahome.accesscontrol.services.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.library.toolset.common.Logger;

import guepardoapps.lucahome.accesscontrol.common.constants.Bundles;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.services.RESTService;

public class RESTServiceController {

	private static final String TAG = RESTServiceController.class.getSimpleName();
	private Logger _logger;

	private Context _context;

	public RESTServiceController(Context context) {
		_logger = new Logger(TAG, Enables.DEBUGGING);
		_logger.Debug("Created new " + TAG);
		_context = context;
	}

	public void SendRestAction(String url, int port, String action) {
		if (action == null) {
			_logger.Warn("Action is null!");
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
