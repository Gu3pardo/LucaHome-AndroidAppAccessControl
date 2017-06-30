package guepardoapps.lucahomeaccesscontrol.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahomeaccesscontrol.common.controller.NetworkController;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;

public class ServerThread {

    private static final String TAG = ServerThread.class.getSimpleName();
    private Logger _logger;

    private int _socketServerPort;
    private ServerSocket _serverSocket;

    private DataHandler _dataHandler;

    public ServerThread(
            int port,
            @NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);

        _socketServerPort = port;
        _dataHandler = new DataHandler(context);

        _logger.Debug("IpAddress: " + new NetworkController(context).GetIpAddress());
        _logger.Debug("SocketServerPort: " + String.valueOf(_socketServerPort));
    }

    public void Start() {
        _logger.Debug("Start");

        SocketServerThread socketServerThread = new SocketServerThread();
        Thread thread = new Thread(socketServerThread);
        thread.start();
    }

    public void Dispose() {
        _logger.Debug("Dispose");

        if (_serverSocket != null) {
            try {
                _serverSocket.close();
            } catch (IOException e) {
                _logger.Error(e.toString());
            }
        }
    }

    private class SocketServerThread extends Thread {

        @Override
        public void run() {
            try {
                _serverSocket = new ServerSocket(_socketServerPort);
                _logger.Debug("I'm waiting here: " + _serverSocket.getLocalPort());

                boolean running = true;
                while (running) {
                    Socket socket = _serverSocket.accept();

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(socket);
                    socketServerReplyThread.run();

                    running = socketServerReplyThread.isAlive();
                }
            } catch (IOException e) {
                _logger.Error(e.toString());
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket _hostThreadSocket;

        SocketServerReplyThread(@NonNull Socket socket) {
            _logger.Debug("New SocketServerReplyThread");
            _hostThreadSocket = socket;
        }

        @Override
        public void run() {
            _logger.Debug("New SocketServerReplyThread run");

            InputStreamReader inputStreamReader;
            BufferedReader inputReader;

            OutputStream outputStream;

            String response;

            try {
                inputStreamReader = new InputStreamReader(_hostThreadSocket.getInputStream());
                inputReader = new BufferedReader(inputStreamReader);

                String read = inputReader.readLine();
                _dataHandler.PerformAction(read);

                inputReader.close();
                inputStreamReader.close();

                response = "OK";
            } catch (IOException e) {
                _logger.Error(e.toString());
                response = "FAIL! " + e.toString();
            }

            try {
                outputStream = _hostThreadSocket.getOutputStream();

                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(response);
                printStream.close();

                outputStream.close();
            } catch (IOException e) {
                _logger.Error(e.toString());
            } finally {
                _logger.Debug("Response: " + response);
            }
        }
    }
}