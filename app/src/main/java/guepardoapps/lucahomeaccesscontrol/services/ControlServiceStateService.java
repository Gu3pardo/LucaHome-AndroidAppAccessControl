package guepardoapps.lucahomeaccesscontrol.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahomeaccesscontrol.common.controller.AndroidSystemController;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.constants.Timeouts;
import guepardoapps.lucahomeaccesscontrol.view.Main;

public class ControlServiceStateService extends Service {

    private static final String TAG = ControlServiceStateService.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private AndroidSystemController _androidSystemController;

    private boolean _isInitialized;
    private Handler _checkApplicationHandler = new Handler();

    private Runnable _checkApplication = new Runnable() {
        public void run() {
            _logger.Debug("_checkApplication");

            if (!_androidSystemController.IsServiceRunning(MainService.class)) {
                String message = String.format("%s not running! Restarting!", MainService.class.getSimpleName());
                _logger.Warn(message);
                Toasty.warning(_context, message, Toast.LENGTH_LONG).show();

                Intent serviceIntent = new Intent(_context, MainService.class);
                startService(serviceIntent);
            }

            if (!_androidSystemController.IsBaseActivityRunning()) {
                String message = String.format("%s not running! Restarting!", Main.class.getSimpleName());
                _logger.Error(message);
                Toasty.error(_context, message, Toast.LENGTH_LONG).show();

                Intent activityIntent = new Intent(_context, Main.class);
                startActivity(activityIntent);
            }

            _checkApplicationHandler.postDelayed(_checkApplication, Timeouts.CHECK_MAIN_SERVICE);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _logger = new Logger(TAG, Enables.LOGGING);

            _context = this;
            _androidSystemController = new AndroidSystemController(_context);

            _isInitialized = true;
            _checkApplication.run();
        }

        _logger.Debug("onStartCommand");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _isInitialized = false;
    }
}
