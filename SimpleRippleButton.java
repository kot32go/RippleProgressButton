package com.kot32.customviewdemo.widghts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kot32.customviewdemo.R;
import com.kot32.customviewdemo.util.Utils;

/**
 * Created by kot32 on 15/5/7.
 */
public class SimpleRippleButton extends CustomView {

    protected TextView textButton;
    protected int defaultTextColor = Color.WHITE;

    protected boolean settedRippleColor = false;

    protected int rippleSize = 3;//手指按在控件上，产生的圆形涟漪的大小
    protected Integer rippleColor = makePressColor(255);//默认按下颜色为灰色
    protected float rippleSpeed;//
    boolean start = false;
    protected OnClickListener onClickListener;

    public SimpleRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    @Override
    protected void onInitDefaultValues() {
        backgroundColor = Color.parseColor("#2196f3");// 默认的背景色，蓝色
        textButton = new TextView(getContext());
        defaultTextColor = Color.WHITE;
        rippleSpeed = 4f;
        minWidth = 80;
        minHeight = 36;
        backgroundResId = R.drawable.background_button_rectangle;
    }


    @Override
    protected void setAttributes(AttributeSet attrs) {
        super.setAttributes(attrs);
        setRippleButtonAttributes(attrs);
    }

    //设置自身的属性
    protected void setRippleButtonAttributes(AttributeSet attrs) {
        int c = attrs.getAttributeResourceValue(MATERIALDESIGNXML, "rippleColor", -1);
        //如果XML文件中颜色值是引用的资源文件里面的
        if (c != -1) {
            rippleColor = getResources().getColor(c);
            settedRippleColor = true;
        } else {

            int rColor = attrs.getAttributeIntValue(MATERIALDESIGNXML, "rippleColor", -1);// 16进制的颜色
            if (rColor != -1 && !isInEditMode()) {
                rippleColor = rColor;
                settedRippleColor = true;
            }
        }

        if (isInEditMode()) {
            // 为了在编译器中预览时不报空指针，在这里产生一个textView对象。实际中不会产生的。
            textButton = new TextView(getContext());
        }
        String text = null;
        /**
         * 设置按钮上的文字内容
         */
        int textResource = attrs.getAttributeResourceValue(ANDROIDXML, "text", -1);
        if (textResource != -1) {
            text = getResources().getString(textResource);
        } else {
            //如果没有文字资源，也就是@String/xx，那么就设置文字
            text = attrs.getAttributeValue(ANDROIDXML, "text");
        }

        /**
         * 当文字不为空的时候，TextView设置文字，否则不设置文字
         */
        if (text != null) {
            textButton.setText(text);
        }

        /**
         * 设置textSize
         */
        String textSize = attrs.getAttributeValue(ANDROIDXML, "textSize");
        if (text != null && textSize != null) {
            textSize = textSize.substring(0, textSize.length() - 2);//12sp->12
            textButton.setTextSize(Float.parseFloat(textSize));
        }

        /**
         * 设置textColor
         */
        int textColor = attrs.getAttributeResourceValue(ANDROIDXML, "textColor", -1);
        if (text != null && textColor != -1) {
            textButton.setTextColor(getResources().getColor(textColor));
        } else if (text != null) {
            // 16进制的color
            String color = attrs.getAttributeValue(ANDROIDXML, "textColor");
            if (color != null && !isInEditMode()) {
                textButton.setTextColor(Color.parseColor(color));
            } else {
                textButton.setTextColor(defaultTextColor);
            }
        }
        textButton.setTypeface(null, Typeface.BOLD);
        //textButton.setPadding(5, 5, 5, 5);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.setMargins(Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()), Utils.dpToPx(5, getResources()));
        textButton.setLayoutParams(params);
        addView(textButton);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //不再向下传递触摸事件了
        return true;
    }

    // 圆心坐标和绘制半径
    protected float x = -1;
    protected float y = -1;
    protected float radius = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                radius = getHeight() / rippleSize;
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println(event.getX() + ":" + event.getY());
                x = event.getX();
                y = event.getY();
                //如果当前的坐标值在控件范围之外了，那么显然不算是最后一次有效触摸，并且将x,y重置
                if (!((event.getX() <= getWidth() && event.getX() >= 0) &&
                        (event.getY() <= getHeight() && event.getY() >= 0))) {
                    isLastTouch = false;
                    x = -1;
                    y = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
                //如果在控件范围内，那么增加波纹半径表示即将开始扩散
                if ((event.getX() <= getWidth() && event.getX() >= 0)
                        && (event.getY() <= getHeight() && event.getY() >= 0)) {
                    start = true;//表示开始波纹
                    isLastTouch = true;
                    radius++;
                } else {
                    isLastTouch = false;
                    x = -1;
                    y = -1;
                }
                //执行点击事件
                if (onClickListener != null)
                    onClickListener.onClick(this);
                break;
        }
        return true;
    }

    public Bitmap makeCircleFromBitmap() {
        Bitmap output = Bitmap.createBitmap(
                getWidth() - Utils.dpToPx(1, getResources()),
                getHeight() - Utils.dpToPx(1, getResources()), Bitmap.Config.ARGB_8888);
        if (start) {
            // 如果开始波纹了，那么进行绘制
            // 画涟漪时要考虑到按钮的边界区域，不要把按钮的阴影边界也填满了
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0, 0, 0, 0);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            if (rippleColor == null) {
                //计算背景色是白色时按下的颜色以供波纹使用
                paint.setColor(makePressColor(255));
            } else {
                paint.setColor(rippleColor);
            }
            //关键点：以最后一次按下的地方为中心点画圆
            canvas.drawCircle(x, y, radius, paint);
            //如果半径大于一开始的半径，那么半径按半径增长速度逐次递增,增长速度也递增
            if (radius > getHeight() / rippleSize){
                radius += rippleSpeed;
                rippleSpeed+=0.3;
            }

            //如果半径超出宽度（注意是宽度，如果超过长度，不管，继续画），那么重置圆心，半径，执行点击事件
            if (radius >= getWidth()) {
                start=false;
                rippleSpeed=4f;
                x = -1;
                y = -1;
                radius = getHeight() / rippleSize;
                if (isEnabled() && onClickListener != null)
                    onClickListener.onClick(this);
            }
        }

        return output;
    }

    //如果焦点失效，重置圆心
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        if (!gainFocus) {
            x = -1;
            y = -1;
        }
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener = l;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (x != -1) {

            canvas.drawBitmap(makeCircleFromBitmap(), 1, 1, null);
        }
        invalidate();
    }

}
