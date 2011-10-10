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

    public float getForeArmAngleRadians() {
        return foreArmAngle;
    }

    public float getArmAngleRadians() {
        return armAngle;
    }

    public float getForeArmAngleDegrees() {
        return (float) Math.toDegrees(getForeArmAngleRadians());
    }

    public float getArmAngleDegrees() {
        return (float) Math.toDegrees(getArmAngleRadians());
    }
}
