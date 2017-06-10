package guepardoapps.lucahomeaccesscontrol.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.UserInformationController;

import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;

public class IpAddressViewUpdater {

    private static final String TAG = IpAddressViewUpdater.class.getSimpleName();
    private Logger _logger;

    private Handler _updater;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private int _updateTime;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            _logger.Debug("_updateRunnable run");
            getCurrentLocalIpAddress();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            getCurrentLocalIpAddress();
            _updater.removeCallbacks(_updateRunnable);
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    public IpAddressViewUpdater(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _updater = new Handler();
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        _userInformationController = new UserInformationController(context);
    }

    public void Start(int updateTime) {
        _logger.Debug("Initialize");
        _updateTime = updateTime;
        _logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_UPDATE_IP_ADDRESS});
        _updateRunnable.run();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
    }

    private void getCurrentLocalIpAddress() {
        _logger.Debug("getCurrentLocalIpAddress");

        String ip = _userInformationController.GetIp();
        _logger.Debug("IP address is: " + ip);

        _broadcastController.SendStringBroadcast(Broadcasts.UPDATE_IP_ADDRESS, Bundles.IP_ADDRESS, ip);
    }
}
