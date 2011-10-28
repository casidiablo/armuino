package com.egoclean.brazo.adk;

import com.egoclean.brazo.ui.ArmActivity;

public class ServoController {
	private final byte mCommandTarget;
	private ArmActivity mActivity;

	public ServoController(ArmActivity activity, int servoNumber) {
		mActivity = activity;
		mCommandTarget = (byte) (servoNumber + 10);
	}

	public void changePosition(double value) {
		mActivity.sendCommand(ArmActivity.LED_SERVO_COMMAND, mCommandTarget, (int) value);
	}
}
