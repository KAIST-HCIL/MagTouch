package kaist.hcil.magtouchlibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import kaist.hcil.magtouchlibrary.datamodel.TapData;

public class TargetView extends View {
    int textSize = 45;
    int textOffset = 10;
    float demoEnlargeRatio = 1.5f;

    Paint paint = new Paint();
    Paint indexPaint = new Paint();
    Paint middlePaint = new Paint();
    Paint ringPaint = new Paint();
    Paint dontKnowPaint = new Paint();
    Paint textPaint = new Paint();

    boolean isDemoMode = false;

    private TapData curTap;

    public TargetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);

        indexPaint.setStyle(Paint.Style.FILL);
        indexPaint.setColor(Color.RED);

        middlePaint.setStyle(Paint.Style.FILL);
        middlePaint.setColor(Color.BLUE);

        ringPaint.setStyle(Paint.Style.FILL);
        ringPaint.setColor(Color.GREEN);

        dontKnowPaint.setStyle(Paint.Style.FILL);
        dontKnowPaint.setColor(Color.WHITE);

        if(isDemoMode)
        {
            textPaint.setColor(Color.WHITE);
        }
        else
        {
            textPaint.setColor(Color.BLACK);
        }

        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setIsDemo(boolean isDemo)
    {
        isDemoMode = isDemo;
        if(isDemoMode)
        {
            textPaint.setColor(Color.WHITE);

        }
        else
        {
            textPaint.setColor(Color.BLACK);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (curTap == null) {
            invalidate();
            return;
        }

        Paint circlePaint;

        if (curTap.getFinger().equals(TapData.Finger.INDEX)) {
            circlePaint = indexPaint;
        } else if (curTap.getFinger().equals(TapData.Finger.MIDDLE)) {
            circlePaint = middlePaint;
        } else if (curTap.getFinger().equals(TapData.Finger.RING)) {
            circlePaint = ringPaint;
        } else {
            circlePaint = dontKnowPaint;
        }


        if(isDemoMode)
        {
            float newRadius = curTap.getRadius() * demoEnlargeRatio;
            int offset = (int)newRadius+ textOffset;

            canvas.drawText(curTap.getFingerFullName(), curTap.getTargetX(), curTap.getTargetY() - offset , textPaint);
            canvas.drawCircle(curTap.getTargetX(), curTap.getTargetY(), newRadius, circlePaint);
        }
        else
        {
            canvas.drawCircle(curTap.getTargetX(), curTap.getTargetY(), curTap.getRadius(), circlePaint);
            canvas.drawText(curTap.getFingerDisplay(), curTap.getTargetX(), curTap.getTargetY(), textPaint);
        }


        invalidate();
        return;
    }

    public void setCurTap(TapData tap) { curTap = tap; }
    public TapData getCurTap() {return curTap;}
}
