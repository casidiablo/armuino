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

package com.egoclean.arm.ui.widget;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.Tweenable;
import aurelienribon.tweenengine.equations.Linear;
import com.egoclean.arm.R;
import com.egoclean.arm.calc.ArmsAngles;
import com.egoclean.arm.calc.InverseCinematic;

import java.text.DecimalFormat;

/**
 * @author cristian
 */
public class ArmView extends View {
    private static final int ORIGIN_ARM_ANGLE = -90;
    private static final int PADDING = 10;

    private static final DecimalFormat FORMATTER = new DecimalFormat("#.#\u00ba");

    private final Paint mArmPaint;
    private final Paint mClickPaint;
    private final Paint mAreaPaint;
    private final Paint mEmptyPaint;
    private final Paint mBoxPaint;
    private final TextPaint mTextPaint;
    private boolean mDrawArea = true;
    private float mClickX;
    private float mClickY;

    private int mForearmAngle = 90;
    private int mArmAngle = 90;
    private final HandTween mHandTween;

    private AngleListener mAngleListener;

    private boolean mRaised = true;
    private boolean mRefresh = true;
    private final Bitmap mRaisedHand;
    private final Bitmap mDownHand;

    private String mPreviousForeArm;
    private String mPreviousArm;

    private float mPointX;
    private float mPointY;
    private final TweenManager mTweenManager;

    public ArmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mArmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArmPaint.setColor(Color.DKGRAY);
        mArmPaint.setAlpha(150);
        mArmPaint.setStrokeWidth(40);
        mArmPaint.setStrokeCap(Paint.Cap.ROUND);

        mClickPaint = new Paint(mArmPaint);
//        mClickPaint.setStrokeWidth(1);
        mClickPaint.setColor(Color.RED);

        mAreaPaint = new Paint(mArmPaint);
        mAreaPaint.setColor(Color.BLUE);
        mAreaPaint.setAlpha(50);

        mEmptyPaint = new Paint(mArmPaint);
        mEmptyPaint.setColor(Color.WHITE);

        mBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoxPaint.setColor(Color.BLACK);
        mBoxPaint.setAlpha(180);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mRaisedHand = BitmapFactory.decodeResource(getResources(), R.drawable.raised_hand);
        mDownHand = BitmapFactory.decodeResource(getResources(), R.drawable.down_hand);

        mTweenManager = new TweenManager();
        mHandTween = new HandTween();
        mTickHandler.sendMessage(Message.obtain());
    }

    private final Handler mTickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTweenManager.update();
            mTickHandler.sendMessageDelayed(Message.obtain(), 10);
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        int armSize = calcArmSize();
        int originX = getWidth() / 2;
        int originY = calcOriginY();

        drawArea(canvas);

        boolean insideWorkingArea = insideWorkingArea(mClickX, mClickY);
        if (mRefresh) {
            mRefresh = insideWorkingArea;
            if (!mRefresh) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }
        ArmsAngles angles = InverseCinematic.calculateAngles(mClickX - getWidth() / 2, originY - mClickY, armSize);
        if (angles != null) {
            if (mRefresh) {
                int foreArmAngleDegrees = (int) angles.getForeArmAngleDegrees();
                if (foreArmAngleDegrees > 0 && foreArmAngleDegrees <= 180) {
                    mForearmAngle = foreArmAngleDegrees;
                }
                int armAngleDegrees = (int) angles.getArmAngleDegrees();
                if (armAngleDegrees > 0 && armAngleDegrees <= 180) {
                    mArmAngle = armAngleDegrees;
                }
            }
        } else {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }

        if (mForearmAngle > 180) {
            mForearmAngle = 180;
        }
        if (mForearmAngle < 0) {
            mForearmAngle = 0;
        }

        if (mArmAngle > 180) {
            mArmAngle = 180;
        }
        if (mArmAngle < 0) {
            mArmAngle = 0;
        }

        // draw arms
        drawLine(canvas, originX, originY, armSize, mForearmAngle);

        int elbowX = (int) (Math.cos(Math.toRadians(mForearmAngle)) * armSize);
        int elbowY = (int) (Math.sin(Math.toRadians(mForearmAngle)) * armSize);
        drawLine(canvas, originX + elbowX, originY - elbowY, armSize, mForearmAngle + mArmAngle + ORIGIN_ARM_ANGLE);

        // draw area again :P
        int auxiliarAreaY = originY + armSize + PADDING;
        canvas.translate(0, auxiliarAreaY);
        boolean previousDrawArea = mDrawArea;
        mDrawArea = true;
        drawArea(canvas);
        mDrawArea = previousDrawArea;
        canvas.translate(0, -auxiliarAreaY);

        // draw point
        if (mClickY < auxiliarAreaY && mClickX > 0 && mClickY > 0) {
            if ((mRefresh && angles != null) || (mPointX == 0 && mPointY == 0)) {
                mPointX = mClickX;
                mPointY = mClickY;
            }
            canvas.drawPoint(mPointX, mPointY, mClickPaint);
        }

        // draw text container
        RectF rect = new RectF(0, 0, getWidth() / 2, getWidth() / 6);
        float boxX = getWidth() / 2 - rect.right / 2;
        float boxY = originY + PADDING;
        canvas.translate(boxX, boxY);
        canvas.drawRoundRect(rect, 15, 15, mBoxPaint);
        if (mRefresh && angles != null) {
            mPreviousForeArm = FORMATTER.format(mForearmAngle);
            mPreviousArm = FORMATTER.format(mArmAngle);
        }

        String servo1 = "Antebrazo: " + mPreviousForeArm;
        String servo2 = "Brazo: " + mPreviousArm;
        if (angles != null && mAngleListener != null && mRefresh) {
            mAngleListener.onAnglesChanged(mForearmAngle, mArmAngle, mHandTween.currentAngle);
        }
        canvas.drawText(servo1, PADDING, mTextPaint.measureText("Xy"), mTextPaint);
        canvas.drawText(servo2, PADDING, mTextPaint.measureText("Xy") * 2, mTextPaint);
        canvas.translate(-boxX, -boxY);

        // draw hand switcher
        if (mRaised) {
            canvas.drawBitmap(mRaisedHand, getMeasuredWidth() / 2 - mRaisedHand.getWidth() / 2,
                    getMeasuredHeight() - mRaisedHand.getHeight(), null);
        } else {
            canvas.drawBitmap(mDownHand, getMeasuredWidth() / 2 - mDownHand.getWidth() / 2,
                    getMeasuredHeight() - mDownHand.getHeight(), null);
        }
    }

    private boolean insideWorkingArea(float x, float y) {
        // check whether it is inside the inner circle
        float centerX = getWidth() / 2;
        int armSize = calcArmSize();
        float centerY = PADDING + armSize * 2;
        double innerRadius = Math.sqrt(2 * Math.pow(armSize, 2));
        boolean insideInnerCircle = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) < Math.pow(innerRadius, 2);

        // now check whether it is outside the arcs
        int rightAreaX = getWidth() - PADDING - armSize;
        int rightAreaY = PADDING + armSize * 2;
        boolean insideRightArea = true;
        if (x > rightAreaX && y > rightAreaY) {
            insideRightArea = Math.pow(x - rightAreaX, 2) + Math.pow(y - rightAreaY, 2) < Math.pow(armSize, 2);
        }

        int leftAreaX = PADDING + armSize;
        int leftAreaY = PADDING + armSize * 2;
        boolean insideLeftArea = true;
        if (x < leftAreaX && y > leftAreaY) {
            insideLeftArea = Math.pow(x - leftAreaX, 2) + Math.pow(y - leftAreaY, 2) < Math.pow(armSize, 2);
        }

        // now check whether it is inside big arc
        int mainAreaX = PADDING + armSize * 2;
        int mainAreaY = PADDING + armSize * 2;
        boolean insideMainArea = true;
        if (y < mainAreaY) {
            insideMainArea = Math.pow(x - mainAreaX, 2) + Math.pow(y - mainAreaY, 2) < Math.pow(armSize * armSize, 2);
        }

        return !insideInnerCircle && insideRightArea && insideLeftArea && insideMainArea;
    }

    private class HandTween implements Tweenable {

        private float currentAngle = 0;

        @Override
        public int getTweenValues(int i, float[] floats) {
            floats[1] = currentAngle;
            return 2;
        }

        @Override
        public void onTweenUpdated(int i, float[] floats) {
            currentAngle = floats[1];
            if (mAngleListener != null) {
                mAngleListener.onAnglesChanged(mForearmAngle, mArmAngle, mHandTween.currentAngle);
            }
        }
    }

    private int calcOriginY() {
        return PADDING + calcArmSize() * 2;
    }

    private void drawArea(Canvas canvas) {
        if (!mDrawArea) {
            return;
        }
        int armSize = calcArmSize();
        RectF mainArea = new RectF(0, 0, calcWidth(), armSize * 4);
        canvas.translate(PADDING, PADDING);
        canvas.drawArc(mainArea, 180, 180, true, mAreaPaint);
        canvas.translate(-PADDING, -PADDING);

        RectF leftArea = new RectF(0, 0, armSize * 2, armSize * 2);
        int leftAreaX = PADDING;
        int leftAreaY = PADDING + armSize;
        canvas.translate(leftAreaX, leftAreaY);
        canvas.drawArc(leftArea, 90, 90, true, mAreaPaint);
        canvas.translate(-leftAreaX, -leftAreaY);

        RectF rightArea = new RectF(0, 0, armSize * 2, armSize * 2);
        int rightAreaX = getWidth() - PADDING - armSize * 2;
        int rightAreaY = PADDING + armSize;
        canvas.translate(rightAreaX, rightAreaY);
        canvas.drawArc(rightArea, 0, 90, true, mAreaPaint);
        canvas.translate(-rightAreaX, -rightAreaY);

        double innerRadius = Math.sqrt(2 * Math.pow(armSize, 2));
        canvas.drawCircle(getWidth() / 2, PADDING + armSize * 2, (float) innerRadius, mEmptyPaint);
    }

    private int calcArmSize() {
        return calcWidth() / 4;
    }

    private int calcWidth() {
        return getWidth() - PADDING * 2;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (clickedHand(event.getX(), event.getY())) {
                    return false;
                }
                mClickX = event.getX();
                mClickY = event.getY();
                int auxiliarDrawAreaY = calcOriginY() + calcArmSize() + PADDING;
                if (mClickY > auxiliarDrawAreaY) {
                    mClickY -= auxiliarDrawAreaY;
                }
                mRefresh = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (clickedHand(event.getX(), event.getY())) {
                    mRaised = !mRaised;
                    mTweenManager.clear();
                    int to = mRaised ? 0 : 180;
                    float diff = Math.abs(mHandTween.currentAngle - to);
                    Tween.to(mHandTween, 0, (int) (diff * 2000 / 180), Linear.INOUT)
                            .target(0, to)
                            .addToManager(mTweenManager);
                    mRefresh = false;
                    invalidate();
                }
                return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean clickedHand(float x, float y) {
        int xPos = getMeasuredWidth() / 2 - mRaisedHand.getWidth() / 2;
        int yPos = getMeasuredHeight() - mRaisedHand.getHeight();
        return x > xPos && x < xPos + mRaisedHand.getWidth() && y > yPos;
    }

    private void drawLine(Canvas canvas, int x, int y, int length, float angle) {
        canvas.translate(x, y);
        canvas.rotate(-angle);
        canvas.drawLine(0, 0, length, 0, mArmPaint);
        canvas.rotate(angle);
        canvas.translate(-x, -y);
    }

    public void setAngleListener(AngleListener angleListener) {
        mAngleListener = angleListener;
    }

    public interface AngleListener {
        void onAnglesChanged(float foreArm, float arm, float hand);
    }
}
