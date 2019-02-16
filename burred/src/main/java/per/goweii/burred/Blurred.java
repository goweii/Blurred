package per.goweii.burred;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    private static Blurred INSTANCE = null;

    private final IBlur mBlur;

    private Bitmap mOriginalBitmap = null;

    private int mMode = MODE_NONE;

    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 0;
    private boolean mKeepSize = false;
    private boolean mRecycleOriginal = false;

    private Blurred(@Nullable Context context) {
        if (context == null) {
            mBlur = FastBlur.get();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mBlur = GaussianBlur.get(context);
            } else {
                mBlur = FastBlur.get();
            }
        }
    }

    public static void init(@NonNull Context context) {
        INSTANCE = new Blurred(context);
    }

    public static void recycle() {
        if (INSTANCE != null) {
            INSTANCE.mBlur.recycle();
            INSTANCE = null;
        }
    }

    public static Blurred with(@NonNull Bitmap original) {
        Blurred instance;
        if (INSTANCE == null) {
            instance = new Blurred(null);
        } else {
            instance = INSTANCE;
        }
        instance.mOriginalBitmap = original;
        return instance;
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
        if (mRadius < 0) {
            mRadius = 0;
        }
        if (mScale < 1) {
            mScale = 1;
        }
        return mBlur.process(mOriginalBitmap, radius, mScale, mKeepSize, mRecycleOriginal);
    }

    private Callback mCallback = null;
    private Handler mCallbackHandler = null;

    @SuppressLint("HandlerLeak")
    public void blur(Callback callback) {
        mCallback = callback;
        mCallbackHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mCallbackHandler = null;
                Bitmap blur = (Bitmap) msg.obj;
                if (mCallback != null) {
                    mCallback.down(blur);
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = mCallbackHandler.obtainMessage();
                msg.obj = blur();
                mCallbackHandler.sendMessage(msg);
            }
        }).start();
    }

    public interface Callback {
        void down(Bitmap bitmap);
    }
}