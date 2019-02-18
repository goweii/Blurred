package per.goweii.burred;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

/**
 * @author Cuizhen
 * @date 2018/4/4
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public final class GaussianBlur implements IBlur {
    private static GaussianBlur INSTANCE = null;

    private final RenderScript renderScript;
    private final ScriptIntrinsicBlur gaussianBlur;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private GaussianBlur(Context context) {
        renderScript = RenderScript.create(context.getApplicationContext());
        gaussianBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static GaussianBlur get(Context context){
        if (INSTANCE == null) {
            synchronized (GaussianBlur.class){
                if (INSTANCE == null) {
                    INSTANCE = new GaussianBlur(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 模糊
     * 采用系统自带的RenderScript
     * 输出图与原图参数相同
     *
     * @param originalBitmap 原图
     * @param scale    缩放因子（>=1）
     * @param radius         模糊半径
     * @return 模糊Bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Bitmap process(@NonNull Bitmap originalBitmap,
                          @FloatRange(from = 0) float radius,
                          @FloatRange(from = 1) float scale,
                          boolean keepSize,
                          boolean recycleOriginal) {
        if (radius <= 0) {
            return originalBitmap;
        }
        float newScale = scale;
        float newRadius = radius;
        if (radius > 25) {
            newRadius = 25;
            newScale = scale * (radius / 25);
        }
        if (newScale == 1) {
            Bitmap output = blurIn25(originalBitmap, newRadius);
            if (recycleOriginal) {
                originalBitmap.recycle();
            }
            return output;
        }
        final int width = originalBitmap.getWidth();
        final int height = originalBitmap.getHeight();
        Bitmap input = Bitmap.createScaledBitmap(originalBitmap, (int) (width / newScale), (int) (height / newScale), true);
        if (recycleOriginal) {
            originalBitmap.recycle();
        }
        Bitmap output = blurIn25(input, newRadius);
        input.recycle();
        if (keepSize) {
            Bitmap outputScaled = Bitmap.createScaledBitmap(output, width, height, true);
            output.recycle();
            output = outputScaled;
        }
        return output;
    }

    @Override
    public void recycle() {
        if (INSTANCE != null) {
            INSTANCE.gaussianBlur.destroy();
            INSTANCE.renderScript.destroy();
            INSTANCE = null;
        }
    }

    /**
     * 高斯模糊
     * 采用系统自带的RenderScript
     * 图像越大耗时越长，测试时1280*680的图片耗时在30~60毫秒
     * 建议在子线程模糊通过Handler回调获取
     *
     * @param input 原图
     * @param radius         模糊半径
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Bitmap blurIn25(@NonNull Bitmap input, @FloatRange(fromInclusive = false, from = 0, to = 25) float radius) {
        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());
        Allocation aIn = Allocation.createFromBitmap(renderScript, input, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation aOut = Allocation.createTyped(renderScript, aIn.getType());
        gaussianBlur.setRadius(radius);
        gaussianBlur.setInput(aIn);
        gaussianBlur.forEach(aOut);
        aOut.copyTo(output);
        aIn.destroy();
        aOut.destroy();
        return output;
    }
}
