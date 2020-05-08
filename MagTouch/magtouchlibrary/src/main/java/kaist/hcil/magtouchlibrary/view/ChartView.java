package kaist.hcil.magtouchlibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ChartView extends View {
    public static int nDim = 3;
    public static int thickness = 10;
    public static int min = -75;
    public static int max = 75;

    private float startAngle = -90;
    private boolean isReady;
    private int width;
    private int height;
    private float[] data;
    private RectF[] ovals;
    private Paint[] paints;


    Paint centerLinePaint;

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        isReady = false;
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void init()
    {
        centerLinePaint = new Paint();
        centerLinePaint.setStrokeWidth(thickness);
        centerLinePaint.setColor(Color.WHITE);
        centerLinePaint.setStyle(Paint.Style.STROKE);

        setOvals();
        setPaints();

        data = new float[nDim];
        isReady = true;
    }

    public void setData(float x, float y, float z)
    {
        data[0] = dataToAngle(x);
        data[1] = dataToAngle(y);
        data[2] = dataToAngle(z);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(!isReady)
        {
            return;
        }

        //drawCenterLine(canvas, centerLinePaint);
        drawArch(canvas);
        invalidate();
    }

    private void drawCenterLine(Canvas canvas, Paint paint)
    {
        canvas.drawLine(0,height/2, width, height/2, paint);
    }

    private void drawArch(Canvas canvas)
    {
        for(int i=0; i< nDim; i++)
        {
            RectF oval = ovals[i];
            Paint paint = paints[i];
            float degrees = data[i];

            Path mPath = new Path();
            mPath.arcTo(oval, startAngle, (float)degrees);
            canvas.drawPath(mPath, paint);
        }

    }

    private void setOvals()
    {
        ovals = new RectF[nDim];
        float center = Math.min(width, height) / 2;
        float baseRadius = center;

        for(int i=0; i< nDim; i++)
        {
            float radius = baseRadius - (i+1)*thickness*2;
            RectF oval = new RectF();
            oval.set(center - radius, center - radius, center + radius, center + radius);
            ovals[i] = oval;
        }
    }

    private void setPaints()
    {
        paints = new Paint[nDim];
        centerLinePaint.setColor(Color.WHITE);
        for(int i=0; i< nDim; i++)
        {
            Paint p = new Paint();
            p.setStrokeWidth(thickness);
            p.setStyle(Paint.Style.STROKE);
            paints[i] = p;
        }
        paints[0].setColor(Color.CYAN);
        paints[1].setColor(Color.MAGENTA);
        paints[2].setColor(Color.YELLOW);
    }

    private float dataToAngle(float d)
    {
        return (d / max) * 180F;
    }
}
