package com.jerry.studio.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * 点赞View Demo
 * @author Jerry
 * @since 2016/07/20
 */
public class HeartView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "HeartView";
    private Queue<HeartItemView> heartViewArrayList = new ConcurrentLinkedQueue<>();
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private int ScreenW, screenH; //屏幕宽高
    private boolean flag;  //线程标识位
    private Thread thread;
    private Timer timer;
    private TimerTask timerTask;

    public HeartView(Context context) {
        super(context);
        init(context);
    }

    public HeartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    Bitmap bitmapXin1, bitmapXin2, bitmapXin3;

    private void init(Context context) {
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        paint = new Paint();
        paint.setTextSize(40);
        paint.setColor(Color.WHITE);
        setFocusable(true);
        heartViewArrayList.clear();
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        timer = new Timer();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        heartViewArrayList.clear();
        ScreenW = this.getWidth();
        screenH = this.getHeight();
        flag = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        heartViewArrayList.clear();
        ScreenW = this.getWidth();
        screenH = this.getHeight();
        flag = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

    //最小程序要30毫秒执行刷新一次
    private long refreshIntervalTime = 16;

    @Override
    public void run() {

        while (flag) {
            long startTime = System.currentTimeMillis();
            myDraw();
            long endTime = System.currentTimeMillis();

            if ((endTime - startTime) < refreshIntervalTime) {
                try {
                    Thread.sleep(refreshIntervalTime - (endTime - startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void refreshBackground(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void myDraw() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (surfaceHolder == null || canvas == null) {
            return;
        }
        try {
            if (canvas != null) {
                refreshBackground(canvas);
                Iterator<HeartItemView> iter = heartViewArrayList.iterator();
                while (iter.hasNext()) {
                    HeartItemView heartView = iter.next();
                    heartView.drawHeart(canvas);
                    heartView.drawHeartLogic();
                    if (heartView.isTop()) {
                        iter.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void addHeart(boolean isSelf) {

        Bitmap bitmap;
        if (isSelf) {
            bitmap = BitmapPool.selfBitmap;

        } else {
            bitmap = BitmapPool.otherBitmaps[new Random().nextInt(BitmapPool.otherBitmaps.length)];
        }

        HeartItemView heartView = new HeartItemView(ScreenW, screenH, bitmap);
        heartViewArrayList.add(heartView);
    }

    public static class HeartItemView {
        private  Bitmap bitmap;
        private Paint paint;

        private float initX = 0, initY = 0;  // 初始心形的位置
        private Random random = new Random();//用于实现随机功能
        private int screenW, screenH;
        private int alpha = 255;
        /***
         * 贝塞尔曲线中间两个点
         */
        private PointF pointA;
        private PointF pointB;
        private float bezierDuration = 4000f;
        private float scaleDuation = 300f;

        /**
         * 贝塞尔曲线起点和终点
         */
        private PointF startPointf;
        private PointF endPointf;

        /***
         * 开始的时间
         */
        private long startTime = 0;
        private float scaleX = 0;
        private float scaleY = 0;

        public HeartItemView(int screenW, int screenH, Bitmap bitmap) {

            this.screenW = screenW;
            this.screenH = screenH;
            this.bitmap = bitmap;
            pointA = getPointF(2);
            pointB = getPointF(1);

            startPointf = new PointF((screenW - bitmap.getWidth()) / 2, screenH - bitmap.getHeight());
            endPointf = new PointF(random.nextInt(screenW / 2) + screenW / 4, 0);

            paint = new Paint();
            paint.setAlpha(alpha);
            paint.setAntiAlias(true);
            initY = screenH - bitmap.getHeight();
            initX = screenW / 2 - bitmap.getWidth() / 2;

        }

        /****
         * 绘制心形
         */
        public void drawHeart(Canvas canvas) {
            if (startTime == 0)
                startTime = SystemClock.uptimeMillis();
            canvas.save();
            canvas.scale(scaleX, scaleY, screenW / 2, screenH);
            canvas.drawBitmap(bitmap, initX, initY, paint);
            canvas.restore();
        }

        /****
         * 绘制心形位置逻辑
         */

        public void drawHeartLogic() {

            /***
             * 1.根据bezierDuration计算0-1之间的时间
             */

            long currentTime = SystemClock.uptimeMillis();
            float bezierTime = (currentTime - startTime) / bezierDuration;

            PointF pointF = getBezierPointF(bezierTime, startPointf, endPointf, pointA, pointB);


            /***
             * 设置滚动路线
             */
            initX = pointF.x;
            long middleTime = SystemClock.uptimeMillis() - startTime;
            if (middleTime > bezierDuration) {
                initY = 0;
            } else {
                initY = (screenH - bitmap.getHeight()) - (screenH - bitmap.getHeight()) * middleTime / bezierDuration;
            }

            paint.setAlpha((int) (255 - (255f * bezierTime)));
            if (bezierTime >= 1) {
                paint.setAlpha(0);
            }

            float scaleTime = (currentTime - startTime) / scaleDuation;

            float tempScaleTime = 0.4f + scaleTime;

            if (tempScaleTime <= 1) {
                scaleX = scaleY = +tempScaleTime;
            } else {
                scaleX = scaleY = 1;
            }

        }

        /***
         * 是否达到顶部
         */
        public boolean isTop() {
            return initY <= 0;
        }

        private PointF getPointF(int scale) {

            PointF pointF = new PointF();
            pointF.x = random.nextInt((screenW - 100));
            pointF.y = random.nextInt((screenH - 100)) / scale;

            return pointF;
        }

        public PointF getBezierPointF(float time, PointF startValue, PointF endValue, PointF middleA, PointF middleB) {

            float timeLeft = 1.0f - time;
            PointF point = new PointF();//结果

            PointF point0 = startValue;//起点

            PointF point3 = endValue;//终点
            //代入公式
            point.x = timeLeft * timeLeft * timeLeft * (point0.x)
                    + 3 * timeLeft * timeLeft * time * (middleA.x)
                    + 3 * timeLeft * time * time * (middleB.x)
                    + time * time * time * (point3.x);

            point.y = timeLeft * timeLeft * timeLeft * (point0.y)
                + 3 * timeLeft * timeLeft * time * (middleA.y)
                + 3 * timeLeft * time * time * (middleB.y)
                + time * time * time * (point3.y);
            return point;

        }
    }
}
