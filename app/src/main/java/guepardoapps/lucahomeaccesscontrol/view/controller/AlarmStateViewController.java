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

public class AlarmStateViewController {

    private static final String TAG = AlarmStateViewController.class.getSimpleName();
    private Logger _logger;

    private boolean _isInitialized;

    private Context _context;
    private ReceiverController _receiverController;

    private View _alarmStateIndicator;
    private TextView _alarmStateTextView;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_alarmStateReceiver onReceive");
            AlarmState currentState = (AlarmState) intent.getSerializableExtra(Bundles.ALARM_STATE);
            if (currentState != null) {
                switch (currentState) {
                    case ACCESS_CONTROL_ACTIVE:
                        setAlarmState(R.xml.circle_blue, R.string.accessControlActive);
                        break;
                    case REQUEST_CODE:
                        setAlarmState(R.xml.circle_yellow, R.string.enterAccessCode);
                        break;
                    case ACCESS_SUCCESSFUL:
                        setAlarmState(R.xml.circle_green, R.string.accessSuccessful);
                        break;
                    case ALARM_ACTIVE:
                        setAlarmState(R.xml.circle_red, R.string.alarmActive);
                        break;
                    case ACCESS_FAILED:
                        setAlarmState(R.xml.circle_red, R.string.accessFailed);
                        break;
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

    private BroadcastReceiver _codeInvalidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Warn("_codeInvalidReceiver onReceive");
            setAlarmState(R.xml.circle_red, R.string.enteredInvalidCode);
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Info("_codeValidReceiver onReceive");
            setAlarmState(R.xml.circle_red, R.string.codeAccepted);
        }
    };

    public AlarmStateViewController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _alarmStateIndicator = ((Activity) _context).findViewById(R.id.alarmStateIndicator);
        _alarmStateTextView = ((Activity) _context).findViewById(R.id.alarmStateText);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{Broadcasts.ALARM_STATE});
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

    private void setAlarmState(int alarmIndicator, int alarmMessage) {
        _alarmStateIndicator.setBackgroundResource(alarmIndicator);
        _alarmStateTextView.setText(alarmMessage);
    }
}
