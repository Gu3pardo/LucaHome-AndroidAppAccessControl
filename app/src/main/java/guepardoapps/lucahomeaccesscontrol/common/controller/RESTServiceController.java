package guepardoapps.lucahomeaccesscontrol.common.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;

public class RESTServiceController {

    private static final String TAG = RESTServiceController.class.getSimpleName();
    private Logger _logger;

    private static final String CODE_INVALID = "codeInvalid";
    private static final String CODE_VALID = "codeValid";

    private static final String ACCESS_CONTROL_ACTIVE = "ACCESS_CONTROL_ACTIVE";
    private static final String REQUEST_CODE = "REQUEST_CODE";
    private static final String ACCESS_SUCCESSFUL = "ACCESS_SUCCESSFUL";
    private static final String ACCESS_FAILED = "ACCESS_FAILED";
    private static final String ALARM_ACTIVE = "ALARM_ACTIVE";

    private BroadcastController _broadcastController;

    public RESTServiceController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _logger.Debug("Created new " + TAG);

        _broadcastController = new BroadcastController(context);
    }

    public void SendRestAction(
            @NonNull String url,
            int port,
            @NonNull String action) {
        if (url.length() <= 12) {
            _logger.Error("Url is invalid!");
            _logger.Error("url: " + url);
            return;
        }
        _logger.Debug("url: " + url);

        if (action.length() <= 3) {
            _logger.Error("Action is null!");
            _logger.Error("action: " + action);
            return;
        }
        _logger.Debug("action: " + action);

        String performAction;
        if (port == -1) {
            performAction = url + action;
        } else {
            performAction = url + ":" + String.valueOf(port) + action;
        }

        RestCommunicationTask task = new RestCommunicationTask();
        task.execute(performAction);
    }

    private class RestCommunicationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... actions) {
            String response = "INVALID";

            for (String action : actions) {
                try {
                    URL url = new URL(action);
                    URLConnection connection = url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }
                    _logger.Debug(response);

                    reader.close();
                    inputStreamReader.close();
                    inputStream.close();

                } catch (IOException e) {
                    _logger.Error(e.getMessage());
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            _logger.Debug(result);

            switch (result) {
                case CODE_VALID:
                    _broadcastController.SendSimpleBroadcast(Broadcasts.ENTERED_CODE_VALID);
                    break;
                case CODE_INVALID:
                    _broadcastController.SendSimpleBroadcast(Broadcasts.ENTERED_CODE_INVALID);
                    break;
                case ACCESS_CONTROL_ACTIVE:
                    _broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE, AlarmState.ACCESS_CONTROL_ACTIVE);
                    break;
                case REQUEST_CODE:
                    _broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE, AlarmState.REQUEST_CODE);
                    break;
                case ACCESS_SUCCESSFUL:
                    _broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE, AlarmState.ACCESS_SUCCESSFUL);
                    break;
                case ACCESS_FAILED:
                    _broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE, AlarmState.ACCESS_FAILED);
                    break;
                case ALARM_ACTIVE:
                    _broadcastController.SendSerializableBroadcast(Broadcasts.ALARM_STATE, Bundles.ALARM_STATE, AlarmState.ALARM_ACTIVE);
                    break;
                default:
                    _logger.Error(String.format(Locale.getDefault(), "No support for result %s", result));
                    break;
            }
        }
    }
}
