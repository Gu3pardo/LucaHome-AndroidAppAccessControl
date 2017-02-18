package guepardoapps.lucahome.accesscontrol.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;

import guepardoapps.toolset.common.Logger;

public class ControlServiceStateService extends Service {

	private static final String TAG = ControlServiceStateService.class.getName();
	private Logger _logger;

	private Context _context;

	private boolean _isInitialized;
	private Handler _checkServicesHandler;

	private Runnable _checkServices = new Runnable() {
		public void run() {
			_logger.Debug("_checkServices");

			if (!isServiceRunning(MainService.class)) {
				_logger.Warn("MainService not running! Restarting!");
				Toasty.warning(_context, "MainService not running! Restarting!", Toast.LENGTH_LONG).show();
				Intent serviceIntent = new Intent(_context, MainService.class);
				startService(serviceIntent);
			}

			if (!isServiceRunning(ReceiverService.class)) {
				_logger.Warn("ReceiverService not running! Restarting!");
				Toasty.warning(_context, "ReceiverService not running! Restarting!", Toast.LENGTH_LONG).show();
				Intent serviceIntent = new Intent(_context, ReceiverService.class);
				startService(serviceIntent);
			}

			_checkServicesHandler.postDelayed(_checkServices, 60 * 1000);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			if (_logger == null) {
				_logger = new Logger(TAG, Enables.DEBUGGING);
			}

			_context = this;

			_isInitialized = true;
			_checkServicesHandler = new Handler();
			_checkServices.run();
		}

		_logger.Debug("onStartCommand");

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
