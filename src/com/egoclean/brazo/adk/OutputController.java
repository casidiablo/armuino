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
        // 180 - 0 -> 180
        // 180 - 45 -> 135
        // 180 - 90 -> 90
        // 180 - 135 -> 45
        // 180 - 180 -> 0
        mArm.changePosition(180 - arm);
        mHand.changePosition(hand);
    }
}
