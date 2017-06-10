package guepardoapps.lucahomeaccesscontrol.view.controller;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahomeaccesscontrol.R;
import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.constants.Login;
import guepardoapps.lucahomeaccesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.enums.ServerSendAction;
import guepardoapps.lucahomeaccesscontrol.services.controller.RESTServiceController;

public class CountdownViewController {

    private static final String TAG = CountdownViewController.class.getSimpleName();
    private Logger _logger;

    private static final String TIME_FORMAT = "%02d:%02d:%02d";
    private static final int COUNTDOWN_TIME = 15 * 1000;
    private static final int COUNTDOWN_INTERVAL = 10;
    private CountDownTimer _countDownTimer;

    private boolean _isInitialized;

    private Context _context;
    private MediaMirrorController _mediaMirrorController;
    private ReceiverController _receiverController;
    private RESTServiceController _restServiceController;

    private TextView _countdownTextView;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_alarmStateReceiver onReceive");
            AlarmState currentState = (AlarmState) intent.getSerializableExtra(Bundles.ALARM_STATE);
            if (currentState != null) {
                switch (currentState) {
                    case ACCESS_CONTROL_ACTIVE:
                        _countdownTextView.setVisibility(View.GONE);
                        break;
                    case REQUEST_CODE:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.WHITE);
                        _countDownTimer.start();
                        break;
                    case ACCESS_SUCCESSFUL:
                        _countdownTextView.setVisibility(View.GONE);
                        _countDownTimer.cancel();
                        break;
                    case ALARM_ACTIVE:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.RED);
                        _countdownTextView.setText(R.string.countdownZero);
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

    public CountdownViewController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);

        _context = context;

        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();

        _receiverController = new ReceiverController(_context);
        _restServiceController = new RESTServiceController(_context);

        _countDownTimer = new CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                _countdownTextView.setText(String.format(Locale.getDefault(),
                        TIME_FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toMillis(millisUntilFinished) - TimeUnit.MILLISECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))));
            }

            public void onFinish() {
                _countdownTextView.setTextColor(Color.RED);
                _countdownTextView.setText(R.string.countdownZero);

                String raspberryAction = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME + "&password="
                        + Login.PASS_PHRASE + "&action=" + ServerSendAction.ACTION_PLAY_ALARM.toString();
                _restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, raspberryAction);

                for (MediaServerSelection server : MediaServerSelection.values()) {
                    _mediaMirrorController.SendCommand(server.GetIp(), MediaServerAction.PLAY_ALARM.toString(), "");
                }
            }
        };
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        _countdownTextView = (TextView) ((Activity) _context).findViewById(R.id.countdownText);
        _countdownTextView.setVisibility(View.GONE);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{Broadcasts.ALARM_STATE});
            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");

        _mediaMirrorController.Dispose();
        _receiverController.UnregisterReceiver(_alarmStateReceiver);

        _isInitialized = false;
    }
}