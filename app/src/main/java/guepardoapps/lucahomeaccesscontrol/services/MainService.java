package guepardoapps.lucahomeaccesscontrol.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;

import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.common.Logger;


import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.constants.Login;
import guepardoapps.lucahomeaccesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahomeaccesscontrol.common.constants.Timeouts;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.enums.ServerSendAction;
import guepardoapps.lucahomeaccesscontrol.server.ServerThread;
import guepardoapps.lucahomeaccesscontrol.services.controller.RESTServiceController;
import guepardoapps.lucahomeaccesscontrol.updater.IpAddressViewUpdater;

public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();
    private Logger _logger;

    private MediaMirrorController _mediaMirrorController;
    private ReceiverController _receiverController;
    private RESTServiceController _restServiceController;

    private boolean _isInitialized;

    private ServerThread _serverThread = null;

    private IpAddressViewUpdater _ipAddressViewUpdater;

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String raspberryAction = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME
                    + "&password=" + Login.PASS_PHRASE
                    + "&action=" + ServerSendAction.ACTION_STOP_ALARM.toString();
            _restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, raspberryAction);

            for (MediaServerSelection server : MediaServerSelection.values()) {
                _mediaMirrorController.SendCommand(server.GetIp(), MediaServerAction.STOP_ALARM.toString(), "");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (!_isInitialized) {
            _logger = new Logger(TAG, Enables.LOGGING);

            _mediaMirrorController = new MediaMirrorController(this);
            _mediaMirrorController.Initialize();

            _receiverController = new ReceiverController(this);
            _receiverController.RegisterReceiver(_codeValidReceiver, new String[]{Broadcasts.ENTERED_CODE_VALID});

            _restServiceController = new RESTServiceController(this);

            _serverThread = new ServerThread(ServerConstants.LOCAL_PORT, this);
            _serverThread.Start();

            _ipAddressViewUpdater = new IpAddressViewUpdater(this);
            _ipAddressViewUpdater.Start(Timeouts.IP_ADDRESS_UPDATE);

            _isInitialized = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (_logger != null) {
            _logger.Debug("onStartCommand");
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (_logger != null) {
            _logger.Debug("onBind");
        }

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (_logger != null) {
            _logger.Debug("onDestroy");
        }

        _ipAddressViewUpdater.Dispose();
        _mediaMirrorController.Dispose();
        _receiverController.Dispose();
        _serverThread.Dispose();
    }
}