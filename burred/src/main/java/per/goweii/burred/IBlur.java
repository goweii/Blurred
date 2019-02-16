package per.goweii.burred;

import android.graphics.Bitmap;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

/**
 * 描述：
 *
 * @author Cuizhen
 * @date 2019/2/13
 */
public interface IBlur {
    /**
     * 高斯模糊图片
     *
     * @param originalBitmap  原图
     * @param radius          模糊半径
     * @param scale           缩小因子
     * @param keepSize        缩小后是否再次放大为原图尺寸
     * @param recycleOriginal 回收原图
     * @return 模糊图
     */
    Bitmap process(@NonNull Bitmap originalBitmap,
                   @FloatRange(from = 0) float radius,
                   @FloatRange(from = 1) float scale,
                   boolean keepSize,
                   boolean recycleOriginal);

    /**
     * 回收资源
     */
    void recycle();
}
