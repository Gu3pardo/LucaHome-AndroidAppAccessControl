package guepardoapps.lucahomeaccesscontrol.server;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.enums.ServerReceiveAction;

public class DataHandler {

    private static final String TAG = DataHandler.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;

    public DataHandler(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _broadcastController = new BroadcastController(context);
    }

    public void PerformAction(@NonNull String command) {
        if (command.length() <= 0) {
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
                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.ALARM_STATE,
                                Bundles.ALARM_STATE,
                                AlarmState.REQUEST_CODE);
                        break;

                    case LOGIN_FAILED:
                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.ALARM_STATE,
                                Bundles.ALARM_STATE,
                                AlarmState.ACCESS_FAILED);
                        break;

                    case LOGIN_SUCCESS:
                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.ALARM_STATE,
                                Bundles.ALARM_STATE,
                                AlarmState.ACCESS_SUCCESSFUL);
                        break;

                    case ALARM_ACTIVE:
                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.ALARM_STATE,
                                Bundles.ALARM_STATE,
                                AlarmState.ALARM_ACTIVE);
                        break;

                    case ACTIVATE_ACCESS_CONTROL:
                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.ALARM_STATE,
                                Bundles.ALARM_STATE,
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
