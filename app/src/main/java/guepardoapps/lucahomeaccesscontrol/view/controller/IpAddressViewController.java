package guepardoapps.lucahomeaccesscontrol.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.lucahomeaccesscontrol.R;
import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.controller.ReceiverController;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;

public class IpAddressViewController {

    private static final String TAG = IpAddressViewController.class.getSimpleName();
    private Logger _logger;

    private boolean _isInitialized;

    private Context _context;
    private ReceiverController _receiverController;

    private TextView _ipAddressTextView;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_alarmStateReceiver onReceive");
            AlarmState currentState = (AlarmState) intent.getSerializableExtra(Bundles.ALARM_STATE);
            if (currentState != null) {
                switch (currentState) {
                    case ACCESS_SUCCESSFUL:
                        _ipAddressTextView.setVisibility(View.VISIBLE);
                        break;
                    case ALARM_ACTIVE:
                    case ACCESS_CONTROL_ACTIVE:
                    case REQUEST_CODE:
                        _ipAddressTextView.setVisibility(View.INVISIBLE);
                        break;
                    case ACCESS_FAILED:
                    case NULL:
                    default:
                        _logger.Warn("State not supported!");
                        break;
                }
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateViewReceiver onReceive");
            String ipAddress = intent.getStringExtra(Bundles.IP_ADDRESS);
            if (ipAddress != null) {
                _logger.Debug("ipAddress: " + ipAddress);
                _ipAddressTextView.setText(ipAddress);
            } else {
                _logger.Warn("ipAddress is null!");
            }
        }
    };

    private BroadcastReceiver _codeInvalidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Warn("_codeInvalidReceiver onReceive");
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Info("_codeValidReceiver onReceive");
        }
    };

    public IpAddressViewController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        _ipAddressTextView = ((Activity) _context).findViewById(R.id.ipAddressTextView);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{Broadcasts.ALARM_STATE});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.UPDATE_IP_ADDRESS});
            _receiverController.RegisterReceiver(_codeInvalidReceiver, new String[]{Broadcasts.ENTERED_CODE_INVALID});
            _receiverController.RegisterReceiver(_codeValidReceiver, new String[]{Broadcasts.ENTERED_CODE_VALID});
            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
