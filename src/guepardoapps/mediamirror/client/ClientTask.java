package guepardoapps.mediamirror.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;

import guepardoapps.lucahome.accesscontrol.common.constants.Enables;

import guepardoapps.mediamirror.common.enums.MediaServerAction;

import guepardoapps.toolset.common.Logger;

public class ClientTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = ClientTask.class.getName();
	private Logger _logger;

	private String _address;
	private int _port;

	private String _communication;
	private boolean _setNewCommunication;

	private String _response = "";

	public ClientTask(Context context, String address, int port) {
		_logger = new Logger(TAG, Enables.DEBUGGING);

		_address = address;
		_port = port;

		_logger.Info("Address is " + _address + " with port " + String.valueOf(_port));
	}

	public void SetCommunication(String communication) {
		_communication = communication + "\n";
		_logger.Info("Communication is " + _communication);
		_setNewCommunication = true;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		_logger.Debug("executing Task");
		Socket socket = null;
		try {
			if (_setNewCommunication) {
				_logger.Debug("New communication is set");
				socket = new Socket(_address, _port);

				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
				_logger.Info("outputStreamWriter is " + outputStreamWriter.toString());

				BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
				_logger.Info("bufferedWriter is " + bufferedWriter.toString());

				PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
				_logger.Info("printWriter is " + printWriter.toString());

				printWriter.println(_communication);
				_logger.Info("printWriter println");
				printWriter.flush();
				_logger.Info("printWriter flush");

				InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
				_logger.Debug("inputStreamReader: " + inputStreamReader.toString());
				BufferedReader inputReader = new BufferedReader(inputStreamReader);
				_logger.Debug("inputReader: " + inputReader.toString());

				_response = inputReader.readLine();

				inputReader.close();
				inputStreamReader.close();
				printWriter.close();
				bufferedWriter.close();
				outputStreamWriter.close();
			} else {
				_logger.Warn("Communication not set!");
				_response += "Communication not set!";
			}
		} catch (UnknownHostException e) {
			_logger.Error(e.toString());
			_response = "UnknownHostException: " + e.toString();
		} catch (IOException e) {
			_logger.Error(e.toString());
			_response = "IOException: " + e.toString();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					_logger.Error(e.toString());
				}
			}
			_communication = "";
			_setNewCommunication = false;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		_logger.Info("Response: " + _response);

		if (_response == null) {
			_logger.Error("Response is null!");
			return;
		}

		String[] responseData = _response.split("\\:");
		if (responseData != null) {
			if (responseData.length > 1) {
				MediaServerAction responseAction = MediaServerAction.GetByString(responseData[0]);
				if (responseAction != null) {
					_logger.Info("ResponseAction: " + responseAction.toString());

					switch (responseAction) {
					default:
						break;
					}
				} else {
					_logger.Warn("responseAction is null!");
				}
			}
		}

		super.onPostExecute(result);
	}
}