/*
 * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2011 Cristian Castiblanco <cristian@elhacker.net>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 */

package com.egoclean.brazo.adk;

import com.egoclean.brazo.ui.ArmActivity;
import com.egoclean.brazo.ui.widget.ArmView;

/**
 * @author cristian
 */
public class OutputController implements ArmView.AngleListener {

    private final ServoController mForeArm;
    private final ServoController mArm;
    private final ServoController mHand;

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
