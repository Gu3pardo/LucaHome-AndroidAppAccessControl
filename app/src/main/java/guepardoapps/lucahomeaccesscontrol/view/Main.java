package guepardoapps.lucahomeaccesscontrol.view;

import android.os.Bundle;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import guepardoapps.lucahomeaccesscontrol.R;
import guepardoapps.lucahomeaccesscontrol.common.constants.Enables;
import guepardoapps.lucahomeaccesscontrol.common.tools.Logger;
import guepardoapps.lucahomeaccesscontrol.services.*;
import guepardoapps.lucahomeaccesscontrol.view.controller.*;

public class Main extends Activity {

    private static final String TAG = Main.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private AlarmStateViewController _alarmStateViewController;
    private BatteryViewController _batteryViewController;
    private CenterViewController _centerViewController;
    private CountdownViewController _countdownViewController;
    private IpAddressViewController _ipAddressViewController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _logger = new Logger(TAG, Enables.LOGGING);
        _logger.Debug("onCreate");

        _context = this;

        initializeController();

        _alarmStateViewController.onCreate();
        _batteryViewController.onCreate();
        _centerViewController.onCreate();
        _countdownViewController.onCreate();
        _ipAddressViewController.onCreate();

        startServices();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        _alarmStateViewController.onResume();
        _batteryViewController.onResume();
        _centerViewController.onResume();
        _countdownViewController.onResume();
        _ipAddressViewController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _logger.Debug("onPause");

        _alarmStateViewController.onPause();
        _batteryViewController.onPause();
        _centerViewController.onPause();
        _countdownViewController.onPause();
        _ipAddressViewController.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");

        _alarmStateViewController.onDestroy();
        _batteryViewController.onDestroy();
        _centerViewController.onDestroy();
        _countdownViewController.onDestroy();
        _ipAddressViewController.onDestroy();
    }

    private void initializeController() {
        _alarmStateViewController = new AlarmStateViewController(_context);
        _batteryViewController = new BatteryViewController(_context);
        _centerViewController = new CenterViewController(_context);
        _countdownViewController = new CountdownViewController(_context);
        _ipAddressViewController = new IpAddressViewController(_context);
    }

    private void startServices() {
        startService(new Intent(_context, MainService.class));
        startService(new Intent(_context, ControlServiceStateService.class));
    }
}