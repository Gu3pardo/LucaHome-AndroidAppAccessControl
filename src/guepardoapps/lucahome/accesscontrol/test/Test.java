package guepardoapps.lucahome.accesscontrol.test;

import android.content.Context;
import android.os.Handler;

import guepardoapps.lucahome.accesscontrol.common.Constants;
import guepardoapps.lucahome.accesscontrol.common.enums.ServerAction;
import guepardoapps.lucahome.accesscontrol.server.DataHandler;

import guepardoapps.toolset.common.Logger;

public class Test {

	private static final String TAG = Test.class.getName();
	private Logger _logger;

	private Context _context;

	private boolean _testAll;
	private int _testAllIndex;

	private ServerAction _action;

	public Test(Context context) {
		_logger = new Logger(TAG, Constants.DEBUGGING_ENABLED);

		_context = context;
	}

	public void PerformTests() {
		_logger.Info("PerformTests");
		simulateStateLoginSuccess();
	}

	private void simulateStateAlarmActive() {
		_logger.Info("SimulateStateAlarmActive");
		handlerPerformAction(ServerAction.ALARM_ACTIVE, 5000);
	}

	private void simulateStateActivateAccessControl() {
		_logger.Info("SimulateStateActivateAccessControl");
		handlerPerformAction(ServerAction.ACTIVATE_ACCESS_CONTROL, 5000);
	}

	private void simulateStateLoginFailed() {
		_logger.Info("SimulateStateLoginFailed");
		handlerPerformAction(ServerAction.LOGIN_FAILED, 5000);
	}

	private void simulateStateLoginSuccess() {
		_logger.Info("SimulateStateLoginSuccess");
		handlerPerformAction(ServerAction.LOGIN_SUCCESS, 5000);
	}

	private void simulateStateRequestCode() {
		_logger.Info("SimulateStateRequestCode");
		handlerPerformAction(ServerAction.REQUEST_CODE, 5000);
	}

	private void simulateAllStates() {
		_logger.Info("SimulateAllStates");
		_testAll = true;
		_testAllIndex = 0;
		for (ServerAction action : ServerAction.values()) {
			handlerPerformAction(action, action.GetId() * 7500);
		}
	}

	private void handlerPerformAction(ServerAction action, int delayInMillis) {
		_action = action;

		Handler handler = new Handler();
		handler.postDelayed(_performTest, delayInMillis);
	}

	private Runnable _performTest = new Runnable() {
		@Override
		public void run() {
			if (_testAll) {
				_testAllIndex++;
				_action = ServerAction.GetById(_testAllIndex);
			}

			if (_action == null) {
				_logger.Error("_action is null!");
				return;
			}
			_logger.Info("Performing test for action: " + _action.toString());
			DataHandler dataHandler = new DataHandler(_context);
			dataHandler.PerformAction("ACTION:" + _action.toString());
		}
	};
}
