package guepardoapps.lucahomeaccesscontrol.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;

public class RESTService extends Service {

    private static final String TAG = RESTService.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _logger = new Logger(TAG, Enables.LOGGING);

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            _logger.Warn("Bundle is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        String url = bundle.getString(Bundles.REST_URL);
        if (url == null) {
            _logger.Warn("URL is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        _logger.Debug("URL: " + url);

        String port = bundle.getString(Bundles.REST_PORT);
        if (port == null) {
            _logger.Warn("Port is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        _logger.Debug("Port: " + port);

        String action = bundle.getString(Bundles.REST_ACTION);
        if (action == null) {
            _logger.Warn("Action is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        _logger.Debug("Action: " + action);

        String performAction;
        if (port.contains("-1")) {
            performAction = url + action;
        } else {
            performAction = url + ":" + port + action;
        }

        _broadcastController = new BroadcastController(this);

        RestCommunicationTask task = new RestCommunicationTask();
        task.execute(performAction);

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private class RestCommunicationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... actions) {
            String response;
            for (String action : actions) {
                try {
                    response = "";

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
            return "FINISHED";
        }

        @Override
        protected void onPostExecute(String result) {
            _logger.Debug(result);

            if (result.contains("codeValid")) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.ENTERED_CODE_VALID);
            }

            stopSelf();
        }
    }
}
