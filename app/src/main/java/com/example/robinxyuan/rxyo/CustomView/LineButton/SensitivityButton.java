package com.example.robinxyuan.rxyo.CustomView.LineButton;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.example.robinxyuan.rxyo.R;

/**
 * Created by robinxyuan on 2017/12/18.
 */

public class SensitivityButton extends ViewGroup implements View.OnClickListener{

        /**
         * TAG
         */
        private static final String TAG = "LineMenu";
        /**
         * 用户点击的按钮
         */
        private View mButton;
        /**
         * 用户点击的按钮的坐标
         */
        private int mT,mL;
        /**
         * 当前LineMenu的状态
         */
        private com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status mCurrentStatus = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.CLOSE;
        /**
         * 菜单的显示位置
         */
        private com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position mPosition = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position.LEFT_TOP;
        /**
         * 菜单的展开方向
         */
        private com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction mDirection = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_UP;
        /**
         * 子菜单之间的间隔，默认为25dp
         */
        private int mInterval = 25;

        /**
         * 设置菜单显示的位置，四选1，默认右下
         *
         */
        public enum Position {
            LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM;
        }

        /**
         * 设置菜单展开的方向，四选1，默认向上
         */
        public enum Direction {
            TURN_UP, TURN_DOWN, TURN_LEFT, TURN_RIGHT;
        }

        /**
         * 状态的枚举类,打开或者关闭
         *
         */
        public enum Status {
            OPEN, CLOSE
        }

        public SensitivityButton(Context context) {
            this(context, null);
        }


        public SensitivityButton(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        /**
         * 回调接口
         */
        private com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.OnMenuItemClickListener onMenuItemClickListener;

        public void setOnMenuItemClickListener(com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.OnMenuItemClickListener onMenuItemClickListener){
            this.onMenuItemClickListener =onMenuItemClickListener;
        }
        /**
         * 子菜单点击的监听器
         */
        public interface OnMenuItemClickListener {
            void onClick(View view, int pos);
        }


        /**
         * 初始化属性，构造方法
         *
         * @param context
         * @param attrs
         * @param defStyle
         */
        public SensitivityButton(Context context, AttributeSet attrs, int defStyle) {

            super(context, attrs, defStyle);
            // dp convert to px
            mInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mInterval, getResources().getDisplayMetrics());

            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SensitivityButton,
                    defStyle, 0);

            //开始获取自定义属性
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    //菜单按钮所在位置
                    case R.styleable.SensitivityButton_tbtn_position:
                        int val = a.getInt(attr, 0);
                        switch (val) {
                            case 0:
                                mPosition = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position.LEFT_TOP;
                                break;
                            case 1:
                                mPosition = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position.RIGHT_TOP;
                                break;
                            case 2:
                                mPosition = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position.RIGHT_BOTTOM;
                                break;
                            case 3:
                                mPosition = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position.LEFT_BOTTOM;
                                break;
                        }
                        break;
                    //菜单展开方向
                    case R.styleable.SensitivityButton_direction:
                        int val2 = a.getInt(attr, 0);
                        switch(val2) {
                            case 0:
                                mDirection = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_UP;
                                break;
                            case 1:
                                mDirection = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_DOWN;
                                break;
                            case 2:
                                mDirection = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_LEFT;
                                break;
                            case 3:
                                mDirection = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_RIGHT;
                                break;
                        }
                        break;
                    //子菜单按钮之间的间隔
                    case R.styleable.SensitivityButton_interval:
                        // dp convert to px
                        mInterval = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, getResources().getDisplayMetrics()));
                        break;

                }
            }
            a.recycle();
        }

        /**
         * 计算子控件的大小，包括菜单按钮，和所有的子菜单按钮
         * @param widthMeasureSpec
         * @param heightMeasureSpec
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            int count = getChildCount();
            for (int i = 0; i < count; i++)
            {
                // mesure child
                getChildAt(i).measure(MeasureSpec.UNSPECIFIED,
                        MeasureSpec.UNSPECIFIED);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }


        /**
         * 计算所有子控件的位置
         * @param changed
         * @param l
         * @param t
         * @param r
         * @param b
         */
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b)
        {
            if (changed)
            {
                //第一个子控件为菜单按钮
                layoutButton();

                int count = getChildCount();

                for (int i = 0; i < count - 1; i++)
                {
                    View child = getChildAt(i + 1);

                    //子菜单按钮默认为隐藏
                    child.setVisibility(View.GONE);

                    int cl =  mL;
                    int ct =  mT;

                    // childview width
                    int cWidth = child.getMeasuredWidth();
                    // childview height
                    int cHeight = child.getMeasuredHeight();


                    if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_UP){
                        if(i == count - 2) {
                            ct = ct - (mInterval + cHeight) * (i + 3);
                        } else {
                            ct = ct - (mInterval + cHeight) * (i + 1);
                        }

                    }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_DOWN){
                        if(i == count - 2) {
                            ct = ct + (mInterval + cHeight) * (i + 3);
                        } else {
                            ct = ct + (mInterval + cHeight) * (i + 1);
                        }

                    }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_LEFT){
                        if(i == count - 2) {
                            cl = cl - (mInterval + cWidth) * (i + 3);
                        } else {
                            cl = cl - (mInterval + cWidth) * (i + 1);
                        }
                    }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_RIGHT){
                        if(i == count - 2) {
                            cl = cl + (mInterval + cWidth) * (i + 3);
                        } else {
                            cl = cl + (mInterval + cWidth) * (i + 1);
                        }
                    }

                    Log.e(TAG, cl + " , " + ct);
                    child.layout(cl, ct, cl + cWidth, ct + cHeight - 10);

                }
            }
        }

        /**
         * 第一个子元素为按钮，为按钮布局且初始化点击事件
         */
        private void layoutButton()
        {
            mButton = getChildAt(0);

            mButton.setOnClickListener(this);

            //默认是在左上角
            int l = 0;
            int t = 0;
            int width = mButton.getMeasuredWidth();
            int height = mButton.getMeasuredHeight();

            //根据自定义属性设置的位置重新设置菜单的位置坐标
            switch (mPosition)
            {
                case LEFT_TOP:
                    l = 0;
                    t = 0;
                    break;
                case LEFT_BOTTOM:
                    l = 0;
                    t = getMeasuredHeight() - height;
                    break;
                case RIGHT_TOP:
                    l = getMeasuredWidth() - width;
                    t = 0;
                    break;
                case RIGHT_BOTTOM:
                    l = getMeasuredWidth() - width;
                    t = getMeasuredHeight() - height;
                    break;
            }

            mT = t;
            mL = l;

            Log.e(TAG, l + " , " + t + " , " + (l + width) + " , " + (t + height));
            mButton.layout(l, t, l + width, t + height);
        }


        /**
         * 为按钮添加点击事件
         */
        @Override
        public void onClick(View v)
        {
            if (mButton == null)
            {
                mButton = getChildAt(0);
            }
            //旋转菜单按钮
//        rotateView(mButton, 0f, 270f, 300);
            //子菜单按钮变化
            toggleMenu(300);
        }

        /**
         * 改变菜单的状态
         */
        private void changeStatus()
        {
            mCurrentStatus = (mCurrentStatus == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.CLOSE ? com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.OPEN
                    : com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.CLOSE);
        }

        /**
         * 按钮的旋转动画
         *
         * @param view
         * @param fromDegrees
         * @param toDegrees
         * @param durationMillis
         */
        public static void rotateView(View view, float fromDegrees,
                                      float toDegrees, int durationMillis)
        {
            RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            rotate.setDuration(durationMillis);
            rotate.setFillBefore(true);
            view.startAnimation(rotate);
        }


        /**
         * 点击菜单按钮之后，子菜单按钮的反应
         * @param durationMillis
         */
        public void toggleMenu(int durationMillis)
        {
            int count = getChildCount();
            for (int i = 0; i < count - 1; i++)
            {
                final View childView = getChildAt(i + 1);
                childView.setVisibility(View.VISIBLE);  //设置子菜单按钮可见

                int xflag = 1;
                int yflag = 1;

                if (mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_UP) {
                    xflag = 0;
                    yflag = 1;
                }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_DOWN) {
                    xflag = 0;
                    yflag = -1;
                }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_LEFT) {
                    xflag = 1;
                    yflag = 0;
                }else if(mDirection == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Direction.TURN_RIGHT) {
                    xflag = -1;
                    yflag = 0;
                }

                int cl;
                int ct;

                if(i == count - 2) {
                    cl = (mInterval + childView.getWidth()) * (i + 3);
                    ct = (mInterval + childView.getHeight()) * (i + 3);
                } else {
                    cl = (mInterval + childView.getWidth()) * (i + 1);
                    ct = (mInterval + childView.getHeight()) * (i + 1);
                }


                AnimationSet animset = new AnimationSet(true);
                Animation animation = null;

                //如果当前状态为关闭，则打开菜单
                if (mCurrentStatus == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.CLOSE)
                {// to open
                    //位移动画
                    animset.setInterpolator(new OvershootInterpolator(2F));
                    animation = new TranslateAnimation(xflag * cl, 0, yflag * ct, 0);
                    childView.setClickable(true);
                    childView.setFocusable(true);
                } else
                {// to close
                    animation = new TranslateAnimation(0, xflag * cl, 0, yflag
                            * ct);
                    childView.setClickable(false);
                    childView.setFocusable(false);
                }
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationStart(Animation animation)
                    {
                    }

                    public void onAnimationRepeat(Animation animation)
                    {
                    }

                    //动画结束的时候，如果状态为关闭，则让子菜单按钮隐藏
                    public void onAnimationEnd(Animation animation)
                    {
                        if (mCurrentStatus == com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.CLOSE)
                            childView.setVisibility(View.GONE);

                    }
                });

                animation.setFillAfter(true);
                animation.setDuration(durationMillis);
                // 为动画设置一个开始延迟时间，纯属好看，可以不设
                animation.setStartOffset((i * 100) / (count - 1));
//            RotateAnimation rotate = new RotateAnimation(0, 720,
//                    Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            rotate.setDuration(durationMillis);
//            rotate.setFillAfter(true);
//            animset.addAnimation(rotate);
                animset.addAnimation(animation);
                childView.startAnimation(animset);
                final int index = i + 1;

                //子菜单按钮的点击事件
                childView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //返回被点击的子菜单按钮的ID
                        if (onMenuItemClickListener != null)
                            onMenuItemClickListener.onClick(childView, index - 1);
                        //被点击的子菜单按钮的动画效果
                        menuItemAnin(index - 1);
                        //点击之后，重新设置状态
//                    changeStatus();
                        mCurrentStatus = com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status.OPEN;
                    }
                });

            }
            changeStatus();
            Log.e(TAG, mCurrentStatus.name() + "");
        }

        /**
         * 开始菜单动画，点击的MenuItem放大消失，其他的缩小消失
         * @param item
         */
        private void menuItemAnin(int item)
        {
            for (int i = 0; i < getChildCount() - 1; i++)
            {
                View childView = getChildAt(i + 1);
                if (i == item)
                {
                    childView.startAnimation(scaleBigAnim(150));
                    childView.startAnimation(scaleLittleAnim(150));
                } //else
//            {
//                childView.startAnimation(scaleSmallAnim(300));
//            }
                childView.setClickable(true);
                childView.setFocusable(true);

            }

        }

        /**
         * 缩小消失
         * @param durationMillis
         * @return
         */
        private Animation scaleSmallAnim(int durationMillis)
        {
            Animation anim = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            anim.setDuration(durationMillis);
            anim.setFillAfter(true);
            return anim;
        }

        /**
         * 放大，透明度降低
         * @param durationMillis
         * @return
         */
        private Animation scaleBigAnim(int durationMillis)
        {
            AnimationSet animationset = new AnimationSet(true);

            Animation anim = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
//        Animation alphaAnimation = new AlphaAnimation(1, 0);
            animationset.addAnimation(anim);
//        animationset.addAnimation(alphaAnimation);
            animationset.setDuration(durationMillis);
            animationset.setFillAfter(true);
            return animationset;
        }

        /**
         * 放大，透明度降低
         * @param durationMillis
         * @return
         */
        private Animation scaleLittleAnim(int durationMillis)
        {
            AnimationSet animationset = new AnimationSet(true);

            Animation anim = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
//        Animation alphaAnimation = new AlphaAnimation(1, 0);
            animationset.addAnimation(anim);
//        animationset.addAnimation(alphaAnimation);
            animationset.setDuration(durationMillis);
            animationset.setFillAfter(true);
            return animationset;
        }

        public com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position getmPosition() {
            return mPosition;
        }

        public void setmPosition(com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Position mPosition) {
            this.mPosition = mPosition;
        }

        public com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status getmCurrentStatus() {
            return mCurrentStatus;
        }

        public void setmCurrentStatus(com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.Status mCurrentStatus) {
            this.mCurrentStatus = mCurrentStatus;
        }

        public com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton.OnMenuItemClickListener getOnMenuItemClickListener() {
            return onMenuItemClickListener;
        }
}
