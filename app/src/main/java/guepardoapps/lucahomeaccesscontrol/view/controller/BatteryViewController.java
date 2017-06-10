package guepardoapps.lucahomeaccesscontrol.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahomeaccesscontrol.R;
import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;

public class BatteryViewController {

    private static final String TAG = BatteryViewController.class.getSimpleName();
    private Logger _logger;

    private static final int BATTERY_LEVEL_WARNING = 15;
    private static final int BATTERY_LEVEL_CRITICAL = 5;

    private boolean _isInitialized;

    private Context _context;
    private ReceiverController _receiverController;

    private View _batteryAlarmView;
    private TextView _batteryValueTextView;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_alarmStateReceiver onReceive");
            AlarmState currentState = (AlarmState) intent.getSerializableExtra(Bundles.ALARM_STATE);
            if (currentState != null) {
                switch (currentState) {
                    case ACCESS_SUCCESSFUL:
                        _batteryValueTextView.setVisibility(View.VISIBLE);
                        _batteryAlarmView.setVisibility(View.VISIBLE);
                        break;
                    case ALARM_ACTIVE:
                    case ACCESS_CONTROL_ACTIVE:
                    case REQUEST_CODE:
                        _batteryValueTextView.setVisibility(View.INVISIBLE);
                        _batteryAlarmView.setVisibility(View.INVISIBLE);
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

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            _batteryValueTextView.setText(String.format("%s%%", level));
            if (level > BATTERY_LEVEL_WARNING) {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_green);
            } else if (level <= BATTERY_LEVEL_WARNING && level > BATTERY_LEVEL_CRITICAL) {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_yellow);
            } else {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_red);
            }
        }
    };

    public BatteryViewController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _batteryAlarmView = ((Activity) _context).findViewById(R.id.batteryAlarm);
        _batteryValueTextView = (TextView) ((Activity) _context).findViewById(R.id.batteryTextView);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _logger.Debug("Initializing!");
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{Broadcasts.ALARM_STATE});
            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _isInitialized = true;
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
