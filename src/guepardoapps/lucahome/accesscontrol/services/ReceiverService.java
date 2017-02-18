package guepardoapps.lucahome.accesscontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.lucahome.accesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.common.constants.Login;
import guepardoapps.lucahome.accesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahome.accesscontrol.common.enums.ServerSendAction;
import guepardoapps.lucahome.accesscontrol.services.controller.RESTServiceController;

import guepardoapps.mediamirror.client.ClientTask;
import guepardoapps.mediamirror.common.enums.MediaServerAction;

import guepardoapps.toolset.common.Logger;
import guepardoapps.toolset.controller.ReceiverController;

public class ReceiverService extends Service {

	private static final String TAG = ReceiverService.class.getName();
	private Logger _logger;

	private Context _context;

	private ClientTask _clientTask;
	private ReceiverController _receiverController;
	private RESTServiceController _restServiceController;

	private boolean _isInitialized;

	private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String raspberryAction = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME + "&password="
					+ Login.PASS_PHRASE + "&action=" + ServerSendAction.ACTION_STOP_ALARM.toString();
			_restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, ServerConstants.RASPBERRY_PORT,
					raspberryAction);

			String mediaserverAction = "ACTION:" + MediaServerAction.STOP_ALARM.toString() + "&DATA:" + "";
			_clientTask = new ClientTask(_context, ServerConstants.MEDIASERVER_URL, ServerConstants.MEDIASERVER_PORT);
			_clientTask.SetCommunication(mediaserverAction);
			_clientTask.execute();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new Logger(TAG, Enables.DEBUGGING);

			_context = this;

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
		_receiverController.UnregisterReceiver(_codeValidReceiver);
		_isInitialized = false;
	}
}
