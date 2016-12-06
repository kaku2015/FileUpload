package com.skr.fileupload.wigets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.skr.fileupload.App;
import com.skr.fileupload.R;

/**
 * @author hyw
 * @since 2016/12/2
 */
public class DividerItemDecoration2 extends RecyclerView.ItemDecoration {

    private int mOrientation = LinearLayoutManager.VERTICAL;

    private int mItemDividerSize = 1;

    private Paint mDividerPaint;

    /**
     * @param orientation LinearLayoutManager.VERTICAL ||
     *                    <p>
     *                    LinearLayoutManager.HORIZONTAL
     */
    public DividerItemDecoration2(Context context, int orientation) {
        this.mOrientation = orientation;
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("Please set the correct parameters!");
        }
        mItemDividerSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemDividerSize, context.getResources().getDisplayMetrics());
        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //noinspection deprecation
        mDividerPaint.setColor(App.getAppContext().getResources().getColor(R.color.light_gray));
        mDividerPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mItemDividerSize;
            canvas.drawRect(left, top, right, bottom, mDividerPaint);
        }
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mItemDividerSize;
            canvas.drawRect(left, top, right, bottom, mDividerPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mItemDividerSize);
        } else {
            outRect.set(0, 0, mItemDividerSize, 0);
        }
    }
}
