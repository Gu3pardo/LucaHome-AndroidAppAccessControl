package guepardoapps.lucahome.accesscontrol.test;

import android.content.Context;
import android.os.Handler;

import guepardoapps.library.toolset.common.Logger;

import guepardoapps.lucahome.accesscontrol.common.constants.Enables;
import guepardoapps.lucahome.accesscontrol.common.enums.ServerReceiveAction;
import guepardoapps.lucahome.accesscontrol.server.DataHandler;

public class Test {

	private static final String TAG = Test.class.getSimpleName();
	private Logger _logger;

	private Context _context;

	private boolean _testAll;
	private int _testAllIndex;

	private ServerReceiveAction _action;

	public Test(Context context) {
		_logger = new Logger(TAG, Enables.DEBUGGING);

		_context = context;
	}

	public void PerformTests() {
		_logger.Info("PerformTests");
		simulateStateLoginSuccess();
	}

	public void simulateStateAlarmActive() {
		_logger.Info("SimulateStateAlarmActive");
		handlerPerformAction(ServerReceiveAction.ALARM_ACTIVE, 5000);
	}

	public void simulateStateActivateAccessControl() {
		_logger.Info("SimulateStateActivateAccessControl");
		handlerPerformAction(ServerReceiveAction.ACTIVATE_ACCESS_CONTROL, 5000);
	}

	public void simulateStateLoginFailed() {
		_logger.Info("SimulateStateLoginFailed");
		handlerPerformAction(ServerReceiveAction.LOGIN_FAILED, 5000);
	}

	public void simulateStateLoginSuccess() {
		_logger.Info("SimulateStateLoginSuccess");
		handlerPerformAction(ServerReceiveAction.LOGIN_SUCCESS, 5000);
	}

	public void simulateStateRequestCode() {
		_logger.Info("SimulateStateRequestCode");
		handlerPerformAction(ServerReceiveAction.REQUEST_CODE, 5000);
	}

	public void simulateAllStates() {
		_logger.Info("SimulateAllStates");
		_testAll = true;
		_testAllIndex = 0;
		for (ServerReceiveAction action : ServerReceiveAction.values()) {
			handlerPerformAction(action, action.GetId() * 7500);
		}
	}

	private void handlerPerformAction(ServerReceiveAction action, int delayInMillis) {
		_action = action;

		Handler handler = new Handler();
		handler.postDelayed(_performTest, delayInMillis);
	}

	private Runnable _performTest = new Runnable() {
		@Override
		public void run() {
			if (_testAll) {
				_testAllIndex++;
				_action = ServerReceiveAction.GetById(_testAllIndex);
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
