package guepardoapps.lucahomeaccesscontrol.common.controller;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Constants;
import guepardoapps.lucahomeaccesscontrol.common.enums.MediaServerAction;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;
import guepardoapps.lucahomeaccesscontrol.tasks.ClientTask;

public class MediaMirrorController {

    private static final String TAG = MediaMirrorController.class.getSimpleName();
    private Logger _logger;

    private boolean _initialized;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _clientTaskResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_clientTaskResponseReceiver onReceive");

            String response = intent.getStringExtra(Bundles.CLIENT_TASK_RESPONSE);
            if (response != null) {
                try {
                    handleResponse(response);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }
            } else {
                _logger.Error("Received null response!");
            }
        }
    };

    public MediaMirrorController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void Initialize() {
        _logger.Debug("Initialize");
        _receiverController.RegisterReceiver(_clientTaskResponseReceiver, new String[]{Broadcasts.CLIENT_TASK_RESPONSE});
        _initialized = true;
    }

    public boolean SendCommand(
            @NonNull String serverIp,
            @NonNull String command,
            @NonNull String data) {
        _logger.Debug("SendServerCommand: " + command + " with data " + data);

        if (!_initialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        String communication = "ACTION:" + command + "&DATA:" + data;
        _logger.Debug("Communication is: " + communication);

        ClientTask clientTask = new ClientTask(
                _context,
                serverIp,
                Constants.MEDIAMIRROR_SERVERPORT,
                communication);
        clientTask.execute();

        return true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _initialized = false;
    }

    private void handleResponse(@NonNull String response) {
        _logger.Debug("handleResponse");

        String[] responseData = response.split("\\:");

        if (responseData.length > 1) {
            MediaServerAction responseAction = MediaServerAction.GetByString(responseData[0]);
            if (responseAction != null) {
                _logger.Info("ResponseAction: " + responseAction.toString());

                switch (responseAction) {

                    case INCREASE_VOLUME:
                    case DECREASE_VOLUME:
                    case UNMUTE_VOLUME:
                    case GET_CURRENT_VOLUME:
                        String currentVolume = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_VOLUME,
                                Bundles.CURRENT_RECEIVED_VOLUME,
                                currentVolume);
                        break;

                    case INCREASE_SCREEN_BRIGHTNESS:
                    case DECREASE_SCREEN_BRIGHTNESS:
                    case GET_SCREEN_BRIGHTNESS:
                        String currentBrightness = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_BRIGHTNESS,
                                Bundles.CURRENT_RECEIVED_BRIGHTNESS,
                                currentBrightness);
                        break;

                    case GET_BATTERY_LEVEL:
                        String currentBatteryLevel = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_BATTERY_LEVEL,
                                Bundles.CURRENT_BATTERY_LEVEL,
                                currentBatteryLevel);
                        break;

                    case GET_SERVER_VERSION:
                        String currentServerVersion = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_SERVER_VERSION,
                                Bundles.CURRENT_SERVER_VERSION,
                                currentServerVersion);
                        break;

                    default:
                        _logger.Debug(String.format(Locale.getDefault(), "ResponseAction is %s", responseAction));
                        break;
                }
            } else {
                _logger.Warn("responseAction is null!");
            }
        }
    }
}
