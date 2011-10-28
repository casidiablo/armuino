package com.egoclean.brazo.adk;

import com.egoclean.brazo.ui.ArmActivity;
import com.egoclean.brazo.ui.widget.ArmView;

/**
 * @author cristian
 */
public class OutputController implements ArmView.AngleListener {

    private ServoController mForeArm;
    private ServoController mArm;
    private ServoController mHand;

    public OutputController(ArmActivity armActivity) {
        mForeArm = new ServoController(armActivity, 1);
        mArm = new ServoController(armActivity, 2);
        mHand = new ServoController(armActivity, 3);
    }

    @Override
    public void onAnglesChanged(float foreArm, float arm, float hand) {
        mForeArm.changePosition(foreArm);
        mArm.changePosition(180 - arm);
        mHand.changePosition(hand);
    }
}
