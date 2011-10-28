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

package com.egoclean.brazo.calc;

/**
* // TODO write description
*
* @author cristian
*/
public class ArmsAngles {
    private final float foreArmAngle;
    private final float armAngle;

    public ArmsAngles(float foreArmAngle, float armAngle) {
        this.foreArmAngle = foreArmAngle;
        this.armAngle = armAngle;
    }

    float getForeArmAngleRadians() {
        return foreArmAngle;
    }

    float getArmAngleRadians() {
        return armAngle;
    }

    public float getForeArmAngleDegrees() {
        return (float) Math.toDegrees(getForeArmAngleRadians());
    }

    public float getArmAngleDegrees() {
        return (float) Math.toDegrees(getArmAngleRadians());
    }
}
