package per.goweii.burred;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

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

    private static final int MODE_NONE = 0;
    private static final int MODE_PERCENT = 1;
    private static final int MODE_RADIUS = 2;

    private static IBlur sBlur;
    private static ExecutorService sExecutor;

    private int mMode = MODE_NONE;
    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 0;
    private boolean mKeepSize = false;
    private boolean mRecycleOriginal = false;

    private Bitmap mOriginalBitmap = null;

    private Callback mCallback = null;
    private Handler mCallbackHandler = null;

    private Blurred() {
    }

    public static void init(Context context) {
        if (sBlur == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sBlur = GaussianBlur.get(context);
            } else {
                sBlur = FastBlur.get();
            }
        }
    }

    public static void realTimeMode(boolean realTimeMode) {
        IBlur iBlur = requireBlur();
        if (iBlur instanceof GaussianBlur) {
            GaussianBlur gaussianBlur = (GaussianBlur) sBlur;
            gaussianBlur.realTimeMode(realTimeMode);
        }
    }

    public static void recycle() {
        if (sBlur != null) {
            sBlur.recycle();
            sBlur = null;
        }
        if (sExecutor != null) {
            if (!sExecutor.isShutdown()) {
                sExecutor.shutdown();
            }
            sExecutor = null;
        }
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
        Utils.requireNonNull(view, "待模糊View不能为空");
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        view.destroyDrawingCache();
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        return with(view.getDrawingCache());
    }

    private Blurred bitmap(Bitmap original) {
        Utils.requireNonNull(original, "待模糊Bitmap不能为空");
        reset();
        mOriginalBitmap = original;
        return this;
    }

    private void reset() {
        if (mOriginalBitmap != null) {
            if (!mOriginalBitmap.isRecycled()) {
                mOriginalBitmap.recycle();
            }
            mOriginalBitmap = null;
        }
        mMode = MODE_NONE;
        mPercent = 0;
        mRadius = 0;
        mScale = 0;
        mKeepSize = false;
        mRecycleOriginal = false;
    }

    public Blurred percent(float percent) {
        this.mMode = MODE_PERCENT;
        this.mPercent = percent;
        return this;
    }

    public Blurred radius(float radius) {
        this.mMode = MODE_RADIUS;
        this.mRadius = radius;
        return this;
    }

    public Blurred scale(float scale) {
        this.mScale = scale;
        return this;
    }

    public Blurred keepSize(boolean keepSize) {
        this.mKeepSize = keepSize;
        return this;
    }

    public Blurred recycleOriginal(boolean recycleOriginal) {
        this.mRecycleOriginal = recycleOriginal;
        return this;
    }

    public Bitmap blur() {
        float radius = 0;
        switch (mMode) {
            default:
                break;
            case MODE_PERCENT:
                int w = mOriginalBitmap.getWidth();
                int h = mOriginalBitmap.getHeight();
                int min = Math.min(w, h);
                radius = min * mPercent;
                break;
            case MODE_RADIUS:
                radius = mRadius;
                break;
        }
        return requireBlur().process(mOriginalBitmap, radius, mScale, mKeepSize, mRecycleOriginal);
    }

    public void blur(Callback callback) {
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

    public interface Callback {
        void down(Bitmap bitmap);
    }
}
