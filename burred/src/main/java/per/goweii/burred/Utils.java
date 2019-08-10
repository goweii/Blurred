package per.goweii.burred;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * @author CuiZhen
 * @date 2019/8/10
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
class Utils {

    static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    static <T> T requireNonNull(T obj) {
        return requireNonNull(obj, "");
    }

    static Bitmap snapshot(View view, float scale) {
        final float newScale = scale > 0 ? scale : 1;
        final int w = view.getWidth();
        final int h = view.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.save();
        canvas.scale(newScale, newScale);
        view.draw(canvas);
        canvas.restore();
        return output;
    }

    static Bitmap snapshot(View view, int bgColor) {
        return snapshot(view, bgColor, 1);
    }

    static Bitmap snapshot(View view, int bgColor, float scale) {
        final float newScale = scale > 0 ? scale : 1;
        final int w = view.getWidth();
        final int h = view.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.save();
        canvas.scale(newScale, newScale);
        canvas.drawColor(bgColor);
        view.draw(canvas);
        canvas.restore();
        return output;
    }

    static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        final int iw = bitmap.getWidth();
        final int ih = bitmap.getHeight();
        final int ow = (int) (iw * scale);
        final int oh = (int) (ih * scale);
        return scaleBitmap(bitmap, ow, oh);
    }

    static Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        final Bitmap.Config config = bitmap.getConfig();
        final Bitmap.Config newConfig;
        switch (config) {
            case RGB_565:
                newConfig = Bitmap.Config.RGB_565;
                break;
            case ALPHA_8:
                newConfig = Bitmap.Config.ALPHA_8;
                break;
            case ARGB_4444:
            case ARGB_8888:
            default:
                newConfig = Bitmap.Config.ARGB_8888;
                break;
        }
        Bitmap output = Bitmap.createBitmap(w, h, newConfig);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, w, h),
                null);
        return output;
    }
}
