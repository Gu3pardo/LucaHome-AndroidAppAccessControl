package guepardoapps.lucahome.accesscontrol.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.UserInformationController;

import guepardoapps.lucahome.accesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahome.accesscontrol.common.constants.Bundles;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;

public class IpAdressViewUpdater {

	private static final String TAG = IpAdressViewUpdater.class.getSimpleName();
	private Logger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;
	private UserInformationController _userInformationController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			getCurrentLocalIpAddress();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			getCurrentLocalIpAddress();
			_updater.removeCallbacks(_updateRunnable);
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	public IpAdressViewUpdater(Context context) {
		_logger = new Logger(TAG, Enables.DEBUGGING);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
		_userInformationController = new UserInformationController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_UPDATE_IP_ADRESS });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	private void getCurrentLocalIpAddress() {
		_logger.Debug("getCurrentLocalIpAddress");

		String ip = _userInformationController.GetIp();
		_logger.Debug("IP adress is: " + ip);

		_broadcastController.SendStringBroadcast(Broadcasts.UPDATE_IP_ADRESS, Bundles.IP_ADRESS, ip);
	}
}
