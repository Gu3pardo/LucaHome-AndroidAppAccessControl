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
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahomeaccesscontrol.R;
import guepardoapps.lucahomeaccesscontrol.common.constants.Broadcasts;
import guepardoapps.lucahomeaccesscontrol.common.constants.Bundles;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.constants.Login;
import guepardoapps.lucahomeaccesscontrol.common.constants.ServerConstants;
import guepardoapps.lucahomeaccesscontrol.common.controller.MediaMirrorController;
import guepardoapps.lucahomeaccesscontrol.common.controller.ReceiverController;
import guepardoapps.lucahomeaccesscontrol.common.enums.AlarmState;
import guepardoapps.lucahomeaccesscontrol.common.enums.MediaServerAction;
import guepardoapps.lucahomeaccesscontrol.common.enums.MediaServerSelection;
import guepardoapps.lucahomeaccesscontrol.common.enums.ServerSendAction;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;
import guepardoapps.lucahomeaccesscontrol.common.controller.RESTServiceController;

public class CountdownViewController {

    private static final String TAG = CountdownViewController.class.getSimpleName();
    private Logger _logger;

    private static final String TIME_FORMAT = "%02d:%02d:%02d";
    private static final int COUNTDOWN_TIME = 15 * 1000;
    private static final int COUNTDOWN_INTERVAL = 10;
    private CountDownTimer _countDownTimer;

    private boolean _isInitialized;
    private boolean _accessControlActive;
    private boolean _countdownActive;

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

                        _accessControlActive = true;
                        _countdownActive = false;
                        break;

                    case REQUEST_CODE:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.WHITE);
                        _countDownTimer.start();

                        _accessControlActive = true;
                        _countdownActive = true;
                        break;

                    case ACCESS_SUCCESSFUL:
                        _countdownTextView.setVisibility(View.GONE);
                        _countDownTimer.cancel();

                        _accessControlActive = false;
                        _countdownActive = false;
                        break;

                    case ALARM_ACTIVE:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.RED);
                        _countdownTextView.setText(R.string.countdownZero);

                        _accessControlActive = true;
                        _countdownActive = true;
                        break;

                    case ACCESS_FAILED:
                    case NULL:
                    default:
                        _logger.Warn("State not supported!");
                        _accessControlActive = true;
                        _countdownActive = true;
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
            _countdownTextView.setTextColor(Color.YELLOW);
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Info("_codeValidReceiver onReceive");
            _countdownTextView.setTextColor(Color.GREEN);
            _countDownTimer.cancel();
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

                sendAlarm();
            }
        };
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        _countdownTextView = ((Activity) _context).findViewById(R.id.countdownText);
        _countdownTextView.setVisibility(View.GONE);
    }

    public void onPause() {
        _logger.Debug("onPause");
        if (_accessControlActive || _countdownActive) {
            _logger.Warn("Do not pause me while access control or countdown is active!");
            Toasty.error(_context, "Alarm activated!", Toast.LENGTH_LONG).show();
            sendAlarm();
        }
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

        if (_accessControlActive || _countdownActive) {
            _logger.Warn("Do not pause me while access control or countdown is active!");
            Toasty.error(_context, "Alarm activated!", Toast.LENGTH_LONG).show();
            sendAlarm();
        }

        _mediaMirrorController.Dispose();
        _receiverController.Dispose();

        _isInitialized = false;
    }

    private void sendAlarm() {
        _logger.Debug("sendAlarm");

        String raspberryAction =
                ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME
                        + "&password=" + Login.PASS_PHRASE
                        + "&action=" + ServerSendAction.ACTION_PLAY_ALARM.toString();
        _restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, raspberryAction);

        for (MediaServerSelection server : MediaServerSelection.values()) {
            _mediaMirrorController.SendCommand(server.GetIp(), MediaServerAction.PLAY_ALARM.toString(), "");
        }
    }
}
