package com.example.dong.odometer;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

//自定义仿小米运动圆环图
public class StepView extends View {

    //定义颜色
    private int background_color, outer_circle_color, outer_dot_color, line_color,
            ring_color, step_num_color, othet_text_color;

    //定义画笔
    private Paint paint;
    //圆弧 文本 点
    private Paint arcPaint, textPaint, pointPaint;

    //背景的坐标
    private int widthBg, heightBg;
    //最外层圆的半径 内层圆的半径 线的长度
    private int ra_out_circle, ra_inner_circle, line_length;

    //我的步数
    private int myFootNum;
    //当前步数
    private int curFootNum;
    //当前步数进度
    private int curFootNumPre;

    //角度
    private float angle;
    //重复次数
    private int count = 2;

    //动画的添加
    private AnimatorSet animatorSet;

    public int getMyFootNum() {
        return this.myFootNum;
    }

    public void setMyFootNum(int myFootNum) {
        this.myFootNum = myFootNum;
    }


    //三个构造函数
    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取atts.xml定义的属性值，存储在TypedArray中
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StepView, defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.StepView_backGroundColor://背景颜色 全部默认为白色
                    background_color = typedArray.getColor(attr, Color.WHITE);
                    break;

                case R.styleable.StepView_outerCircleColor: //最外侧圆
                    outer_circle_color = typedArray.getColor(attr, Color.WHITE);
                    break;

                case R.styleable.StepView_outerDotColor://最外侧圆上的小圆点
                    outer_dot_color = typedArray.getColor(attr, Color.WHITE);
                    break;

                case R.styleable.StepView_lineColor:  //最外侧线的颜色
                    line_color = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.StepView_ringColor: //圆环的颜色
                    ring_color = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.StepView_stepNumColor: //步数的颜色
                    step_num_color = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.StepView_othetTextColor: //其他文字颜色
                    othet_text_color = typedArray.getColor(attr, Color.WHITE);
                    break;
            }
        }
        typedArray.recycle();
        init();
    }

    //初始化画笔与动画集
    private void init() {
        //画笔 全部抗锯齿
        paint = new Paint();
        paint.setAntiAlias(true);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);

        animatorSet = new AnimatorSet();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//宽度测量模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//宽度测量值
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);//高度测量模式
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);//高度的测量值

        //如果布局里面设置的是固定值，这里取布局里面的固定值；如果设置的是match_parent,则取父布局的大小
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            //如果布局里面没有设置固定值，取布局的宽度的一半
            width = widthSize / 2;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = heightSize * 3 / 4;
        }

        widthBg = width;
        heightBg = height;
        ra_out_circle = heightBg * 3 / 9;
        ra_inner_circle = height * 3 / 10;
        line_length = 30;
        //设置当前view的大小
        setMeasuredDimension(width, height);
        startAnim();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制底层屏幕
        paint.setColor(background_color);
        paint.setStyle(Paint.Style.FILL);
        RectF rectF_back = new RectF(0, 0, widthBg, heightBg);
        canvas.drawRect(rectF_back, paint);

        //绘制最外层的圆
        paint.setColor(outer_circle_color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawCircle(widthBg / 2, heightBg / 2, ra_out_circle, paint);

        //绘制圆上的小圆点
        pointPaint.setColor(outer_dot_color);
        pointPaint.setStrokeWidth(10);
        canvas.drawCircle((float) (widthBg / 2 + ra_out_circle * Math.cos(angle * 3.14 / 180)), (float) (heightBg / 2 + ra_out_circle * Math.sin(angle * 3.14 / 180)), 10, pointPaint);

        //画竖线
        paint.setColor(line_color);
        paint.setStrokeWidth(4);
        for (int i = 0; i < 360; i++) {
            canvas.drawLine(widthBg / 2, (heightBg / 2 - ra_inner_circle), widthBg / 2, (heightBg / 2 - ra_inner_circle + line_length), paint);
            canvas.rotate(1, widthBg / 2, heightBg / 2);
        }

        //绘制刻度
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(30);
        arcPaint.setColor(ring_color);
        RectF arcRect = new RectF((widthBg / 2 - ra_inner_circle + line_length / 2), (heightBg / 2 - ra_inner_circle + line_length / 2), (widthBg / 2 + ra_inner_circle - line_length / 2), (heightBg / 2 + ra_inner_circle - line_length / 2));
        canvas.drawArc(arcRect, -90, curFootNumPre, false, arcPaint);


        //绘制步数
        textPaint.setColor(step_num_color);
        textPaint.setStrokeWidth(25);
        textPaint.setTextSize(widthBg / 6);
        // TODO: 2018/12/5 ??
        if (myFootNum < 10) {
            canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 + 120), heightBg / 2 + 50, textPaint);
        } else if (myFootNum < 100) {
            canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 + 90), heightBg / 2 + 50, textPaint);
        } else if (myFootNum < 1000) {
            canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 + 30), heightBg / 2 + 50, textPaint);
        } else if (myFootNum < 10000) {
            canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 - 50), heightBg / 2 + 50, textPaint);
        } else if (myFootNum < 100000) {
            textPaint.setTextSize(widthBg / 7);
            canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 - 70), heightBg / 2 + 50, textPaint);
        }
//        canvas.drawText(String.valueOf(myFootNum), (widthBg / 3 - 50), heightBg / 2 + 50, textPaint);
        textPaint.setStrokeWidth(10);
        textPaint.setColor(othet_text_color);
        textPaint.setTextSize(widthBg / 20);

        canvas.drawText("今日已走步数",(widthBg/2-160),heightBg/2+120,textPaint);
//        canvas.drawText("步", (widthBg / 2 + 200), heightBg / 2 + 50, textPaint);

    }



    //初始化的动画
    private void startAnim() {
        int step_count = myFootNum > 8000 ? 8000 : myFootNum;

        //小圆点动画
        final ValueAnimator dotAnimator = ValueAnimator.ofInt(-90 , (step_count * 360 / 8000 - 90));

        dotAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                angle = (int) dotAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        dotAnimator.setInterpolator(new LinearInterpolator());

        //步数动画
        final ValueAnimator walkAnimator = ValueAnimator.ofInt(0, (step_count * 360 / 8000));
        walkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                curFootNum = (int) walkAnimator.getAnimatedValue();
                postInvalidate();
            }
        });


        //圆弧动画的实现
        final ValueAnimator arcAnimator = ValueAnimator.ofInt(0, (step_count * 360 / 8000));
        arcAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                curFootNumPre = (int) arcAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        animatorSet.setDuration(1000);
        animatorSet.playTogether(walkAnimator, arcAnimator, dotAnimator);
        animatorSet.start();
    }

    //刷新动画
    private void resetAnim(int lastFootNum) {
        int last_step_count=lastFootNum>8000?8000:lastFootNum;
        int step_count = myFootNum > 8000 ? 8000 : myFootNum;
        //小圆点动画
        final ValueAnimator dotAnimator = ValueAnimator.ofInt((last_step_count*360/8000-90), (step_count * 360 / 8000 - 90));
        dotAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                angle = (int) dotAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        dotAnimator.setInterpolator(new LinearInterpolator());



        //步数动画
        final ValueAnimator walkAnimator = ValueAnimator.ofInt(last_step_count*360/8000, (step_count * 360 / 8000));
        walkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                curFootNum = (int) walkAnimator.getAnimatedValue();
                postInvalidate();
            }
        });


        //圆弧动画的实现
        final ValueAnimator arcAnimator = ValueAnimator.ofInt(last_step_count*360/8000, (step_count * 360 / 8000));
        arcAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                curFootNumPre = (int) arcAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        animatorSet.setDuration(1000);
        animatorSet.playTogether(walkAnimator, arcAnimator,dotAnimator);
        animatorSet.start();

    }



    public void reSet(int lastNum,int footNum) {
        this.myFootNum = footNum;
        resetAnim(lastNum);
    }
}
