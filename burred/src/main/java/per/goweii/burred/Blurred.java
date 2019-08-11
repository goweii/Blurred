package per.goweii.burred;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Cuizhen
 * @date 2018/4/27
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public final class Blurred {

    private static final Float MAX_FPS = 60F;

    private static IBlur sBlur;
    private static ExecutorService sExecutor;

    private long mLastFrameTime = 0L;

    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 1;
    private boolean mAntiAlias = false;
    private boolean mKeepSize = false;
    private boolean mFitIntoViewXY = false;
    private boolean mRecycleOriginal = false;
    private float mMaxFps = MAX_FPS;

    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = null;
    private ViewTreeObserver.OnDrawListener mOnDrawListener = null;

    private int mBackgroundColor = Color.TRANSPARENT;
    private int mForegroundColor = Color.TRANSPARENT;
    private Bitmap mOriginalBitmap = null;
    private View mViewFrom = null;
    private ImageView mViewInto = null;

    private SnapshotInterceptor mSnapshotInterceptor = null;
    private FpsListener mFpsListener = null;
    private Listener mListener = null;
    private Callback mCallback = null;
    private Handler mCallbackHandler = null;

    public static void init(Context context) {
        if (sBlur == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sBlur = GaussianBlur.get(context);
            } else {
                sBlur = FastBlur.get();
            }
        }
    }

    public static void recycle() {
        if (sBlur != null) {
            sBlur.recycle();
            sBlur = null;
        }
        BitmapProcessor.get().realTimeMode(false);
        if (sExecutor != null) {
            if (!sExecutor.isShutdown()) {
                sExecutor.shutdown();
            }
            sExecutor = null;
        }
    }

    public static void realTimeMode(boolean realTimeMode) {
        final IBlur iBlur = requireBlur();
        if (iBlur instanceof GaussianBlur) {
            GaussianBlur gaussianBlur = (GaussianBlur) sBlur;
            gaussianBlur.realTimeMode(realTimeMode);
        }
        BitmapProcessor.get().realTimeMode(realTimeMode);
    }

    private static IBlur requireBlur() {
        return Utils.requireNonNull(sBlur, "Blurred未初始化");
    }

    private static ExecutorService requireExecutor() {
        if (sExecutor == null || sExecutor.isShutdown()) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        return sExecutor;
    }

    public static Blurred with(Bitmap original) {
        return new Blurred().bitmap(original);
    }

    public static Blurred with(View view) {
        return new Blurred().view(view);
    }

    public void reset() {
        mMaxFps = MAX_FPS;
        mPercent = 0;
        mRadius = 0;
        mScale = 1;
        mKeepSize = false;
        mAntiAlias = false;
        mFitIntoViewXY = false;
        mRecycleOriginal = false;
        mOriginalBitmap = null;
        if (mViewFrom != null) {
            if (mOnPreDrawListener != null) {
                mViewFrom.getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
                mOnPreDrawListener = null;
            }
            mViewFrom = null;
        }
        mViewInto = null;
        mBackgroundColor = Color.TRANSPARENT;
        mForegroundColor = Color.TRANSPARENT;
    }

    public Blurred view(View view) {
        reset();
        mViewFrom = view;
        return this;
    }

    public Blurred bitmap(Bitmap original) {
        reset();
        mOriginalBitmap = original;
        return this;
    }

    public Blurred suggestConfig() {
        mMaxFps = 60F;
        mPercent = 0;
        mRadius = 10;
        mScale = 8;
        mKeepSize = false;
        mAntiAlias = false;
        mFitIntoViewXY = false;
        mRecycleOriginal = false;
        return this;
    }

    public Blurred backgroundColor(int color) {
        mBackgroundColor = color;
        return this;
    }

    public Blurred foregroundColor(int color) {
        mForegroundColor = color;
        return this;
    }

    public Blurred percent(float percent) {
        this.mPercent = percent;
        return this;
    }

    public Blurred radius(float radius) {
        this.mRadius = radius;
        return this;
    }

    public Blurred scale(float scale) {
        this.mScale = scale;
        return this;
    }

    public Blurred maxFps(float maxFps) {
        this.mMaxFps = maxFps;
        return this;
    }

    public Blurred keepSize(boolean keepSize) {
        this.mKeepSize = keepSize;
        return this;
    }

    public Blurred fitIntoViewXY(boolean fit) {
        this.mFitIntoViewXY = fit;
        return this;
    }

    public Blurred antiAlias(boolean antiAlias) {
        this.mAntiAlias = antiAlias;
        return this;
    }

    public Blurred recycleOriginal(boolean recycleOriginal) {
        this.mRecycleOriginal = recycleOriginal;
        return this;
    }

    public Blurred snapshotInterceptor(SnapshotInterceptor interceptor) {
        this.mSnapshotInterceptor = interceptor;
        return this;
    }

    public Blurred fpsListener(FpsListener listener) {
        this.mFpsListener = listener;
        return this;
    }

    public Blurred listener(Listener listener) {
        this.mListener = listener;
        return this;
    }

    public Bitmap blur() {
        if (mViewFrom == null && mOriginalBitmap == null) {
            throw new NullPointerException("待模糊View和Bitmap不能同时为空");
        }
        if (mListener != null) mListener.begin();
        float scale = mScale <= 0 ? 1 : mScale;
        float radius = mPercent <= 0 ? mRadius : Math.min(
                mViewFrom != null ? mViewFrom.getWidth() : mOriginalBitmap.getWidth(),
                mViewFrom != null ? mViewFrom.getHeight() : mOriginalBitmap.getHeight()
        ) * mPercent;
        final Bitmap blurredBitmap;
        if (mViewFrom == null) {
            blurredBitmap = requireBlur().process(mOriginalBitmap, radius, scale, mKeepSize, mRecycleOriginal);
        } else {
            if (radius > 25) {
                scale = scale / (radius / 25);
                radius = 25;
            }
            final SnapshotInterceptor snapshotInterceptor = checkSnapshotInterceptor();
            Bitmap bitmap = snapshotInterceptor.snapshot(mViewFrom, mBackgroundColor, mForegroundColor, scale, mAntiAlias);
            blurredBitmap = requireBlur().process(bitmap, radius, 1, mKeepSize, mRecycleOriginal);
        }
        if (mListener != null) mListener.end();
        return blurredBitmap;
    }

    public void blur(final Callback callback) {
        Utils.requireNonNull(callback, "Callback不能为空");
        mCallback = callback;
        mCallbackHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                mCallbackHandler = null;
                mCallback.down((Bitmap) msg.obj);
            }
        };
        requireExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = blur();
                Message msg = mCallbackHandler.obtainMessage();
                msg.obj = bitmap;
                mCallbackHandler.sendMessage(msg);
            }
        });
    }

    /**
     * 用于实现实时高斯模糊
     * viewFrom : 通过with()/view()传入的view
     * viewInto : 该处的ImageView
     * 将对viewFrom进行截图模糊处理，并对遮盖区域裁剪后设置到viewInto
     * viewFrom和viewInto不能有包含关系，及viewInto不能是viewFrom的子控件
     */
    public void blur(final ImageView into) {
        Utils.requireNonNull(mViewFrom, "实时高斯模糊时待模糊View不能为空");
        Utils.requireNonNull(into, "ImageView不能为空");
        mViewInto = into;
        if (mOnPreDrawListener == null) {
            mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mViewInto == null) return true;
                    long currFrameTime = System.currentTimeMillis();
                    final float fps = 1000F / (currFrameTime - mLastFrameTime);
                    if (fps > mMaxFps) return true;
                    mLastFrameTime = currFrameTime;
                    if (mFpsListener != null) mFpsListener.currFps(fps);
                    realTimeMode(true);
                    keepSize(false);
                    recycleOriginal(true);
                    Bitmap blur = blur();
                    Bitmap clip = BitmapProcessor.get().clip(blur, mViewFrom, mViewInto, mFitIntoViewXY, mAntiAlias);
                    blur.recycle();
                    mViewInto.setImageBitmap(clip);
                    return true;
                }
            };
            mViewFrom.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
        }
    }

    private SnapshotInterceptor checkSnapshotInterceptor() {
        if (mSnapshotInterceptor == null) {
            mSnapshotInterceptor = new DefaultSnapshotInterceptor();
        }
        return mSnapshotInterceptor;
    }

    public interface SnapshotInterceptor {
        Bitmap snapshot(View from,
                        int backgroundColor,
                        int foregroundColor,
                        float scale,
                        boolean antiAlias);
    }

    public interface Callback {
        void down(Bitmap bitmap);
    }

    public interface Listener {
        void begin();

        void end();
    }

    public interface FpsListener {
        void currFps(float fps);
    }
}
