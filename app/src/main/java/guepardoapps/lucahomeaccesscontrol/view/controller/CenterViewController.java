package guepardoapps.lucahomeaccesscontrol.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class CenterViewController {

    private static final String TAG = CenterViewController.class.getSimpleName();
    private Logger _logger;

    private static final int MAX_CHAR_LENGTH = 10;
    private static final int LOGIN__SUCCESSFUL_TIMEOUT = 1500;

    private boolean _isInitialized;

    private Context _context;
    private ReceiverController _receiverController;
    private RESTServiceController _restServiceController;

    private Button _activateAlarmButton;

    private TextView _alarmTextView;

    private RelativeLayout _inputRelativeLayout;
    private TextView _codeTextView;
    private TextView _loginNotificationTextView;

    private String _code;

    private Runnable _loginSuccessfulRunnable = new Runnable() {
        public void run() {
            _logger.Debug("_loginSuccessfulRunnable run");
            setVisibilities(View.VISIBLE, View.GONE, View.GONE);
        }
    };

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_alarmStateReceiver onReceive");
            AlarmState currentState = (AlarmState) intent.getSerializableExtra(Bundles.ALARM_STATE);
            if (currentState != null) {
                switch (currentState) {
                    case ACCESS_CONTROL_ACTIVE:
                        setVisibilities(View.GONE, View.GONE, View.GONE);
                        break;
                    case REQUEST_CODE:
                        setVisibilities(View.GONE, View.GONE, View.VISIBLE);
                        _loginNotificationTextView.setText("");
                        _code = "";
                        setCodeText(_code.length());
                        break;
                    case ACCESS_SUCCESSFUL:
                        _loginNotificationTextView.setText(R.string.codeAccepted);
                        _loginNotificationTextView.setTextColor(0xFF00FF00);
                        _code = "";
                        setCodeText(_code.length());
                        Handler loginSuccessfulHandler = new Handler();
                        loginSuccessfulHandler.postDelayed(_loginSuccessfulRunnable, LOGIN__SUCCESSFUL_TIMEOUT);
                        break;
                    case ALARM_ACTIVE:
                        setVisibilities(View.GONE, View.VISIBLE, View.GONE);
                        break;
                    case ACCESS_FAILED:
                        setVisibilities(View.GONE, View.GONE, View.VISIBLE);
                        _loginNotificationTextView.setText(R.string.enteredInvalidCode);
                        _loginNotificationTextView.setTextColor(0xFFFF0000);
                        _code = "";
                        setCodeText(_code.length());
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

    public CenterViewController(@NonNull Context context) {
        _logger = new Logger(TAG, Enables.LOGGING);
        _context = context;
        _receiverController = new ReceiverController(_context);
        _restServiceController = new RESTServiceController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        _alarmTextView = (TextView) ((Activity) _context).findViewById(R.id.alarmText);
        _inputRelativeLayout = (RelativeLayout) ((Activity) _context).findViewById(R.id.buttonArrayLayout);
        _codeTextView = (TextView) ((Activity) _context).findViewById(R.id.codeTextView);
        _loginNotificationTextView = (TextView) ((Activity) _context).findViewById(R.id.loginNotificationTextView);
        initializeButtons();
        setVisibilities(View.GONE, View.GONE, View.GONE);
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
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void initializeButtons() {
        _activateAlarmButton = (Button) ((Activity) _context).findViewById(R.id.activateAlarmButton);
        _activateAlarmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String action = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME + "&password="
                        + Login.PASS_PHRASE + "&action=" + ServerSendAction.ACTION_ACTIVATE_ALARM.toString();
                _restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, action);
            }
        });

        Button buttonCode0 = (Button) ((Activity) _context).findViewById(R.id.code0Button);
        buttonCode0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("0");
            }
        });

        Button buttonCode1 = (Button) ((Activity) _context).findViewById(R.id.code1Button);
        buttonCode1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("1");
            }
        });

        Button buttonCode2 = (Button) ((Activity) _context).findViewById(R.id.code2Button);
        buttonCode2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("2");
            }
        });

        Button buttonCode3 = (Button) ((Activity) _context).findViewById(R.id.code3Button);
        buttonCode3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("3");
            }
        });

        Button buttonCode4 = (Button) ((Activity) _context).findViewById(R.id.code4Button);
        buttonCode4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("4");
            }
        });
        Button buttonCode5 = (Button) ((Activity) _context).findViewById(R.id.code5Button);
        buttonCode5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("5");
            }
        });

        Button buttonCode6 = (Button) ((Activity) _context).findViewById(R.id.code6Button);
        buttonCode6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("6");
            }
        });

        Button buttonCode7 = (Button) ((Activity) _context).findViewById(R.id.code7Button);
        buttonCode7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("7");
            }
        });

        Button buttonCode8 = (Button) ((Activity) _context).findViewById(R.id.code8Button);
        buttonCode8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("8");
            }
        });

        Button buttonCode9 = (Button) ((Activity) _context).findViewById(R.id.code9Button);
        buttonCode9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCharToCode("9");
            }
        });

        Button buttonCodeReset = (Button) ((Activity) _context).findViewById(R.id.codeResetButton);
        buttonCodeReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                _code = "";
                setCodeText(_code.length());
            }
        });

        Button buttonCodeOk = (Button) ((Activity) _context).findViewById(R.id.codeOkButton);
        buttonCodeOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (_code.length() > 0 && _code.length() <= MAX_CHAR_LENGTH) {
                    String action = ServerConstants.RASPBERRY_ACTION_PATH + Login.USER_NAME + "&password="
                            + Login.PASS_PHRASE + "&action=" + ServerSendAction.ACTION_SEND_CODE.toString() + "&code="
                            + _code;
                    _restServiceController.SendRestAction(ServerConstants.RASPBERRY_URL, -1, action);
                } else {
                    _logger.Warn("Code is not valid!");
                }
            }
        });
    }

    private void setCodeText(int length) {
        String codeHide = "";
        for (int count = 0; count < length; count++) {
            codeHide += "*";
        }
        _codeTextView.setText(codeHide);
    }

    private void addCharToCode(@NonNull String number) {
        if (_code.length() < MAX_CHAR_LENGTH) {
            _code += number;
            setCodeText(_code.length());
        } else {
            _logger.Warn("Code cannot be larger then " + String.valueOf(MAX_CHAR_LENGTH));
        }
    }

    private void setVisibilities(int alarmButtonVisibility, int alarmTextVisibility, int inputLayoutVisibility) {
        _activateAlarmButton.setVisibility(alarmButtonVisibility);
        _alarmTextView.setVisibility(alarmTextVisibility);
        _inputRelativeLayout.setVisibility(inputLayoutVisibility);
    }
}
