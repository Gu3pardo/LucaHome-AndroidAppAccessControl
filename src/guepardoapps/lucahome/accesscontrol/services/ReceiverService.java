package guepardoapps.lucahome.accesscontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.accesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.common.constants.Login;
import guepardoapps.lucahome.accesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahome.accesscontrol.common.enums.ServerSendAction;
import guepardoapps.lucahome.accesscontrol.services.controller.RESTServiceController;

public class ReceiverService extends Service {

	private static final String TAG = ReceiverService.class.getSimpleName();
	private Logger _logger;

	private Context _context;

	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;
	private RESTServiceController _restServiceController;

	private boolean _isInitialized;

	private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String raspberryAction = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME + "&password="
					+ Login.PASS_PHRASE + "&action=" + ServerSendAction.ACTION_STOP_ALARM.toString();
			_restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, raspberryAction);

			for (MediaMirrorSelection server : MediaMirrorSelection.values()) {
				_mediaMirrorController.SendCommand(server.GetIp(), ServerAction.STOP_ALARM.toString(), "");
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new Logger(TAG, Enables.DEBUGGING);

			_context = this;

			_mediaMirrorController = new MediaMirrorController(_context);
			_mediaMirrorController.Initialize();

			_receiverController = new ReceiverController(_context);
			_receiverController.RegisterReceiver(_codeValidReceiver, new String[] { Broadcasts.ENTERED_CODE_VALID });

			_restServiceController = new RESTServiceController(_context);

			_isInitialized = true;
		}

		_logger.Debug("onStartCommand");

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		_mediaMirrorController.Dispose();
		_receiverController.UnregisterReceiver(_codeValidReceiver);

		_isInitialized = false;
	}
}
