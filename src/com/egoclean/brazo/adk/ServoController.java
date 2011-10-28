package com.egoclean.brazo.adk;

import com.egoclean.brazo.ui.ArmActivity;

public class ServoController {
	private final int mServoNumber;
	private final byte mCommandTarget;
	private ArmActivity mActivity;

	public ServoController(ArmActivity activity, int servoNumber) {
		mActivity = activity;
		mServoNumber = servoNumber;
		mCommandTarget = (byte) (servoNumber - 1 + 0x10);
	}

	public void changePosition(double value) {
		mActivity.sendCommand(ArmActivity.LED_SERVO_COMMAND, mCommandTarget, (int) value);
	}
}
