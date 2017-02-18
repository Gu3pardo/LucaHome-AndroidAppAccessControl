package guepardoapps.lucahome.accesscontrol.services;

import android.os.IBinder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahome.accesscontrol.common.constants.Timeouts;
import guepardoapps.lucahome.accesscontrol.server.ServerThread;
import guepardoapps.lucahome.accesscontrol.updater.*;

import guepardoapps.toolset.common.Logger;
import guepardoapps.toolset.controller.BroadcastController;

public class MainService extends Service {

	private static final String TAG = MainService.class.getName();
	private Logger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private boolean _isInitialized;

	private ServerThread _serverThread = null;

	private IpAdressViewUpdater _ipAdressViewUpdater;

	@Override
	public void onCreate() {
		super.onCreate();

		if (!_isInitialized) {
			_isInitialized = true;

			_logger = new Logger(TAG, Enables.DEBUGGING);

			_context = this;
			if (_broadcastController == null) {
				_broadcastController = new BroadcastController(_context);
			}

			if (_serverThread == null) {
				_serverThread = new ServerThread(ServerConstants.RASPBERRY_PORT, _context);
				_serverThread.Start();
			}

			if (_ipAdressViewUpdater == null) {
				_ipAdressViewUpdater = new IpAdressViewUpdater(_context);
				_ipAdressViewUpdater.Start(Timeouts.IP_ADRESS_UPDATE);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger != null) {
			_logger.Debug("onStartCommand");
		}
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (_logger != null) {
			_logger.Debug("onBind");
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_logger != null) {
			_logger.Debug("onDestroy");
		}

		_ipAdressViewUpdater.Dispose();
		_serverThread.Dispose();
	}
}