package com.yyp.musicplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class MxGroup extends View {

    public MxGroup(Context context) {
        super(context);
    }

    public MxGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MxGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.moveTo(0, getHeight());
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth(), getHeight());
        path.close();

        canvas.drawPath(path, paint);

    }
}
