package guepardoapps.lucahome.accesscontrol.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.lucahome.accesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahome.accesscontrol.common.constants.Bundles;
import guepardoapps.lucahome.accesscontrol.common.constants.Enables;

public class RESTService extends Service {

	private static final String TAG = RESTService.class.getSimpleName();
	private Logger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		_logger = new Logger(TAG, Enables.DEBUGGING);

		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			_logger.Warn("Bundle is null!");
			stopSelf();
			return -1;
		}

		String url = bundle.getString(Bundles.REST_URL);
		if (url == null) {
			_logger.Warn("URL is null!");
			stopSelf();
			return -1;
		}
		_logger.Debug("URL: " + url);

		String port = bundle.getString(Bundles.REST_PORT);
		if (port == null) {
			_logger.Warn("Port is null!");
			stopSelf();
			return -1;
		}
		_logger.Debug("Port: " + port);

		String action = bundle.getString(Bundles.REST_ACTION);
		if (action == null) {
			_logger.Warn("Action is null!");
			stopSelf();
			return -1;
		}
		_logger.Debug("Action: " + action);

		String performAction;
		if (port.contains("-1")) {
			performAction = url + action;
		} else {
			performAction = url + ":" + port + action;
		}

		_context = this;
		_broadcastController = new BroadcastController(_context);

		RestCommunicationTask task = new RestCommunicationTask();
		task.execute(new String[] { performAction });

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class RestCommunicationTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... actions) {
			String response = "";
			for (String action : actions) {
				try {
					response = "";

					URL url = new URL(action);
					URLConnection connection = url.openConnection();
					InputStream inputStream = connection.getInputStream();

					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

					String line;
					while ((line = reader.readLine()) != null) {
						response += line;
					}
					_logger.Debug(response);

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
