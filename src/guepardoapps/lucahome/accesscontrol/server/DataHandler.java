package guepardoapps.lucahome.accesscontrol.server;

import android.content.Context;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.lucahome.accesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahome.accesscontrol.common.constants.Bundles;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.common.enums.AlarmState;
import guepardoapps.lucahome.accesscontrol.common.enums.ServerReceiveAction;

public class DataHandler {

	private static final String TAG = DataHandler.class.getSimpleName();
	private Logger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	public DataHandler(Context context) {
		_logger = new Logger(TAG, Enables.DEBUGGING);
		_context = context;
		_broadcastController = new BroadcastController(_context);
	}

	public void PerformAction(String command) {
		if (command == null) {
			_logger.Warn("Command is null!");
			return;
		}

		_logger.Debug("PerformAction with data: " + command);
		if (command.startsWith("ACTION:")) {
			ServerReceiveAction action = ServerReceiveAction.GetByString(command.replace("ACTION:", ""));

			if (action != null) {
				_logger.Debug("action: " + action.toString());

				switch (action) {
				case REQUEST_CODE:
					_broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE,
							AlarmState.REQUEST_CODE);
					break;
				case LOGIN_FAILED:
					_broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE,
							AlarmState.ACCESS_FAILED);
					break;
				case LOGIN_SUCCESS:
					_broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE,
							AlarmState.ACCESS_SUCCESSFUL);
					break;
				case ALARM_ACTIVE:
					_broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE,
							AlarmState.ALARM_ACTIVE);
					break;
				case ACTIVATE_ACCESS_CONTROL:
					_broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE,
							AlarmState.ACCESS_CONTROL_ACTIVE);
					break;
				case NULL:
				default:
					_logger.Warn("Action not handled!\n" + action.toString());
					break;
				}
			} else {
				_logger.Warn("Action failed to be converted! Is null!\n" + command);
			}
		} else {
			_logger.Warn("Command has wrong format!\n" + command);
		}
	}
}
