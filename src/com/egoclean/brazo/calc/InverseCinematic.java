package com.egoclean.brazo.calc;

/**
 * @author cristian
 */
public class InverseCinematic {
    public static ArmsAngles calculateAngles(float x, float y, float armSize) {
        float distance = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        float mainAngle;
        if (x >= 0) {// in the right side, calculate it with the height
            mainAngle = (float) Math.asin(y / distance);
        } else {// in the left side, calculate it with the X
            mainAngle = (float) Math.acos(x / distance);
        }

        if (x >= 0) {// in the right side, calculate it with the height
            mainAngle = (float) Math.asin(y / distance);
        } else {
            if (y > 0) {
                mainAngle = (float) Math.acos(x / distance);
            } else {
                mainAngle = (float) (Math.PI - Math.asin(y / distance));
            }
        }
        if (Float.isNaN(mainAngle)) {
            return null;
        }

        float internalAngle = (float) Math.acos(distance / (2 * armSize));
        if (Float.isNaN(internalAngle)) {
            return null;
        }
        float foreArmAngleRightElbow = mainAngle - internalAngle;
        float foreArmAngleLeftElbow = mainAngle + internalAngle;

        float foreArmAngle = x < 0 ? foreArmAngleRightElbow : foreArmAngleLeftElbow;

        // calculate arm angle
        float elbowY = (float) (Math.sin(foreArmAngle) * armSize);
        float initialAngle = (float) Math.asin((y - elbowY) / armSize);
        if (x < 0) {
            initialAngle = (float) (Math.PI - initialAngle);
        }

        float armAngle = (float) ((Math.PI / 2 - foreArmAngle) + initialAngle);
        return new ArmsAngles(foreArmAngle, armAngle);
    }

}
