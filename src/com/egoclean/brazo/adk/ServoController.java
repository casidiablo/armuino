package com.egoclean.brazo.adk;

import com.egoclean.brazo.ui.ArmActivity;

class ServoController {
	private final byte mCommandTarget;
	private final ArmActivity mActivity;

	public ServoController(ArmActivity activity, int servoNumber) {
		mActivity = activity;
		mCommandTarget = (byte) (servoNumber + 10);
	}

	public void changePosition(double value) {
		mActivity.sendCommand(mCommandTarget, (int) value);
	}
}
