package guepardoapps.lucahome.accesscontrol.services.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.lucahome.accesscontrol.common.Constants;
import guepardoapps.lucahome.accesscontrol.services.RESTService;

import guepardoapps.toolset.common.Logger;

public class RESTServiceController {

	private static final String TAG = RESTServiceController.class.getName();
	private Logger _logger;

	private Context _context;

	public RESTServiceController(Context context) {
		_logger = new Logger(TAG, Constants.DEBUGGING_ENABLED);
		_logger.Debug("Created new " + TAG);
		_context = context;
	}

	public void SendRestAction(String action) {
		if (action == null) {
			_logger.Warn("Action is null!");
			return;
		}
		_logger.Debug("action: " + action);

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();
		serviceData.putString(Constants.BUNDLE_REST_ACTION, action);
		serviceIntent.putExtras(serviceData);
		
		_context.startService(serviceIntent);
	}
}
