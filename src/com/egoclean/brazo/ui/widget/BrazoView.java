package com.egoclean.brazo.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.egoclean.brazo.calc.ArmsAngles;
import com.egoclean.brazo.calc.InverseCinematic;

/**
 * @author cristian
 */
public class BrazoView extends View {
    private final int mOriginArmAngle = -90;
    private static final int PADDING = 10;

    private final Paint mArmPaint;
    private final Paint mClickPaint;
    private final Paint mAreaPaint;
    private final Paint mEmptyPaint;
    private boolean mDrawArea = true;
    private float mClickX;
    private float mClickY;
    private int mForearmAngle;
    private int mArmAngle;

    public BrazoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mArmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArmPaint.setColor(Color.DKGRAY);
        mArmPaint.setAlpha(150);
        mArmPaint.setStrokeWidth(10);
        mArmPaint.setStrokeCap(Paint.Cap.ROUND);

        mClickPaint = new Paint(mArmPaint);
        mClickPaint.setColor(Color.RED);

        mAreaPaint = new Paint(mArmPaint);
        mAreaPaint.setColor(Color.BLUE);
        mAreaPaint.setAlpha(50);

        mEmptyPaint = new Paint(mArmPaint);
        mEmptyPaint.setColor(Color.parseColor("#cccccc"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int armSize = calcArmSize();
        int originX = getWidth() / 2;
        int originY = calcOriginY();

        drawArea(canvas);

//        mClickX = getWidth() / 2 + 90;
//        mClickY = originY + 80;// 170
        ArmsAngles angles = InverseCinematic.calculateAngles(mClickX- getWidth() / 2, originY - mClickY, armSize);
        if (angles != null) {
            mForearmAngle = (int) angles.getForeArmAngleDegrees();
            mArmAngle = (int) angles.getArmAngleDegrees();
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
        drawLine(canvas, originX + elbowX, originY - elbowY, armSize, mForearmAngle + mArmAngle + mOriginArmAngle);

        // draw area again :P
        int auxiliarAreaY = originY + armSize + PADDING;
        canvas.translate(0, auxiliarAreaY);
        boolean previousDrawArea = mDrawArea;
        mDrawArea = true;
        drawArea(canvas);
        mDrawArea = previousDrawArea;
        canvas.translate(0, -auxiliarAreaY);

        // draw point
        if (mClickY < auxiliarAreaY) {
            canvas.drawPoint(mClickX,  mClickY, mClickPaint);
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
        int leftAreaY = PADDING + armSize * 2 - armSize;
        canvas.translate(leftAreaX, leftAreaY);
        canvas.drawArc(leftArea, 90, 90, true, mAreaPaint);
        canvas.translate(-leftAreaX, -leftAreaY);

        RectF rightArea = new RectF(0, 0, armSize * 2, armSize * 2);
        int rightAreaX = getWidth() - PADDING - armSize * 2;
        int rightAreaY = PADDING + armSize * 2 - armSize;
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
                mClickX = event.getX();
                mClickY = event.getY();
                int auxiliarDrawAreaY = calcOriginY() + calcArmSize() + PADDING;
                if (mClickY > auxiliarDrawAreaY) {
                    mClickY -= auxiliarDrawAreaY;
                }
                invalidate();
                return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void drawLine(Canvas canvas, int x, int y, int length, float angle) {
        canvas.translate(x, y);
        canvas.rotate(-angle);
        canvas.drawLine(0, 0, length, 0, mArmPaint);
        canvas.rotate(angle);
        canvas.translate(-x, -y);
    }
}
