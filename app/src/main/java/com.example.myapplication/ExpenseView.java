package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ExpenseView extends View {
    private Paint paint = new Paint();
    private List<Float> categoryPercentages = new ArrayList<>();
    private List<Integer> categoryColors = new ArrayList<>();


    public ExpenseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpenseView(Context context) {
        super(context);
    }

    public void setData(List<Float> percentages, List<Integer> colors) {
        this.categoryPercentages = percentages;
        this.categoryColors = colors;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float total = 0;
        for (float percentage : categoryPercentages) {
            total += percentage;
        }

        float startAngle = -90;
        for (int i = 0; i < categoryPercentages.size(); i++) {
            float sweepAngle = (categoryPercentages.get(i) / total) * 360;
            paint.setColor(categoryColors.get(i));
            canvas.drawArc(0, 0, getWidth(), getHeight(), startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }
}
