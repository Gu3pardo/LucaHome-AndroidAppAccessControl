package guepardoapps.lucahome.accesscontrol.common.enums;

import java.io.Serializable;

public enum ServerSendAction implements Serializable {

	NULL(0, "", ""), 
	ACTION_ACTIVATE_ALARM(1, "ACTION_ACTIVATE_ALARM", "activatealarm"), 
	ACTION_SEND_CODE(2, "ACTION_SEND_CODE", "sendcode"), 
	ACTION_PLAY_ALARM(3, "ACTION_PLAY_ALARM", "playAlarm"), 
	ACTION_STOP_ALARM(4, "ACTION_STOP_ALARM", "stopAlarm");

	private int _id;
	private String _description;
	private String _raspberryAction;

	private ServerSendAction(int id, String description, String raspberryAction) {
		_id = id;
		_description = description;
		_raspberryAction = raspberryAction;
	}

	public int GetId() {
		return _id;
	}

	public String GetRaspberryAction() {
		return _raspberryAction;
	}

	@Override
	public String toString() {
		return _description;
	}

	public static ServerSendAction GetById(int id) {
		for (ServerSendAction e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}
}
