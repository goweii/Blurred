package per.goweii.burred;

import android.graphics.Bitmap;
import android.view.View;

/**
 * @author CuiZhen
 * @date 2019/8/11
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public class DefaultSnapshotInterceptor implements Blurred.SnapshotInterceptor {
    @Override
    public Bitmap snapshot(View from, int backgroundColor, int foregroundColor, float scale, boolean antiAlias) {
        return BitmapProcessor.get().snapshot(from, backgroundColor, foregroundColor, scale, antiAlias);
    }
}
