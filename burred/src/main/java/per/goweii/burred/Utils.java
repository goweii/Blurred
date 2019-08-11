package per.goweii.burred;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

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

    static Bitmap snapshot(View from, int bgColor, int fgColor, float scale, boolean antiAlias) {
        final float newScale = scale > 0 ? scale : 1;
        final int w = (int) (from.getWidth() * newScale);
        final int h = (int) (from.getHeight() * newScale);
        Bitmap output = Bitmap.createBitmap(w <= 0 ? 1 : w, h <= 0 ? 1 : h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        if (antiAlias) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        }
        canvas.save();
        canvas.scale(newScale, newScale);
        if (bgColor != 0) {
            canvas.drawColor(bgColor);
        }
        from.draw(canvas);
        if (fgColor != 0) {
            canvas.drawColor(fgColor);
        }
        canvas.restore();
        return output;
    }

    static Bitmap clip(Bitmap bitmap, View from, ImageView into, boolean antiAlias) {
        int[] lf = new int[2];
        from.getLocationOnScreen(lf);
        int[] lt = new int[2];
        into.getLocationOnScreen(lt);
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        float sx = (float) bw / (float) from.getWidth();
        float sh = (float) bh / (float) from.getHeight();
        Rect rf = new Rect(
                (int) ((lt[0] - lf[0]) * sx),
                (int) ((lt[1] - lf[1]) * sh),
                (int) ((lt[0] - lf[0]) * sx + into.getWidth() * sx),
                (int) ((lt[1] - lf[1]) * sh + into.getHeight() * sh)
        );
        Rect rt = new Rect(0, 0, into.getWidth(), into.getHeight());
        Paint paint = null;
        if (antiAlias) {
            paint = new Paint();
            paint.setXfermode(null);
            paint.setAntiAlias(true);
            float s = Math.max(
                    (float) into.getWidth() / (float) rf.width(),
                    (float) into.getHeight() / (float) rf.height()
            );
            if (s > 1) {
                rt.right = rf.width();
                rt.bottom = rf.height();
            }
        }
        Bitmap output = Bitmap.createBitmap(rt.width(), rt.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(bitmap, rf, rt, paint);
        return output;
    }

    private static class StopDrawException extends Exception {
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
