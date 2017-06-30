package guepardoapps.lucahomeaccesscontrol.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.controller.BroadcastController;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

public class ClientTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ClientTask.class.getSimpleName();
    private Logger _logger;

    private static final int DEFAULT_TIMEOUT_MS = 500;

    private String _address;
    private int _port;
    private String _communication;
    private int _timeoutMSec;

    private String _response;
    private boolean _responseError;

    private BroadcastController _broadcastController;

    public ClientTask(
            @NonNull Context context,
            @NonNull String address,
            int port,
            @NonNull String communication,
            int timeoutMSec) {
        _logger = new Logger(TAG);

        _address = address;
        _port = port;

        _logger.Info("Address is " + _address + " with port " + String.valueOf(_port));

        _communication = communication + "\n";
        _logger.Info("Communication is " + _communication);

        if (timeoutMSec <= 0) {
            _logger.Warn("TimeOut was set lower then 1ms! Setting to default!");
            timeoutMSec = DEFAULT_TIMEOUT_MS;
        }
        _timeoutMSec = timeoutMSec;

        _response = "";

        _broadcastController = new BroadcastController(context);
    }

    public ClientTask(
            @NonNull Context context,
            @NonNull String address,
            int port,
            @NonNull String communication) {
        this(context, address, port, communication, DEFAULT_TIMEOUT_MS);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        _logger.Debug("executing Task");
        Socket socket = null;
        try {
            _logger.Debug("New communication is set");
            socket = new Socket();
            socket.connect(new InetSocketAddress(_address, _port), _timeoutMSec);

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
            _responseError = false;

            inputReader.close();
            inputStreamReader.close();
            printWriter.close();
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (UnknownHostException e) {
            _logger.Error(e.toString());
            _response = "UnknownHostException: " + e.toString();
            _responseError = true;
        } catch (IOException e) {
            _logger.Error(e.toString());
            _response = "IOException: " + e.toString();
            _responseError = true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    _logger.Error(e.toString());
                }
            }
            _communication = "";
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

        if (_responseError) {
            _logger.Error("An response error appeared!");
            return;
        }

        _broadcastController.SendStringBroadcast(Broadcasts.CLIENT_TASK_RESPONSE, Bundles.CLIENT_TASK_RESPONSE,
                _response);

        super.onPostExecute(result);
    }
}