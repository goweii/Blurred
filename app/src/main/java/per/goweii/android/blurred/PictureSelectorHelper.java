package per.goweii.android.blurred;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片选择和视频选择辅助类
 * 用类名调用对用的方法 设置回调监听 在最下面有回调监听的代码复制过去直接使用
 * <p>
 * .openGallery()//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
 * .theme()//主题样式(不设置为默认样式) 也可参考demo values/styles下 例如：R.style.picture.white.style
 * .maxSelectNum()// 最大图片选择数量 int
 * .minSelectNum()// 最小选择数量 int
 * .imageSpanCount(4)// 每行显示个数 int
 * .selectionMode()// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
 * .previewImage()// 是否可预览图片 true or false
 * .previewVideo()// 是否可预览视频 true or false
 * .enablePreviewAudio() // 是否可播放音频 true or false
 * .isCamera()// 是否显示拍照按钮 true or false
 * .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
 * .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
 * .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
 * .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
 * .enableCrop()// 是否裁剪 true or false
 * .compress()// 是否压缩 true or false
 * .glideOverride()// int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
 * .withAspectRatio()// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
 * .hideBottomControls()// 是否显示uCrop工具栏，默认不显示 true or false
 * .isGif()// 是否显示gif图片 true or false
 * .compressSavePath(getPath())//压缩图片保存地址
 * .freeStyleCropEnabled()// 裁剪框是否可拖拽 true or false
 * .circleDimmedLayer()// 是否圆形裁剪 true or false
 * .showCropFrame()// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
 * .showCropGrid()// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
 * .openClickSound()// 是否开启点击声音 true or false
 * .selectionMedia()// 是否传入已选图片 List<LocalMedia> list
 * .previewEggs()// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
 * .cropCompressQuality()// 裁剪压缩质量 默认90 int
 * .minimumCompressSize(100)// 小于100kb的图片不压缩
 * .synOrAsy(true)//同步true或异步false 压缩 默认同步
 * .cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效 int
 * .rotateEnabled() // 裁剪是否可旋转图片 true or false
 * .scaleEnabled()// 裁剪是否可放大缩小图片 true or false
 * .videoQuality()// 视频录制质量 0 or 1 int
 * .videoMaxSecond(15)// 显示多少秒以内的视频or音频也可适用 int
 * .videoMinSecond(10)// 显示多少秒以内的视频or音频也可适用 int
 * .recordVideoSecond()//视频秒数录制 默认60s int
 * .isDragFrame(false)// 是否可拖动裁剪框(固定)
 * .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
 *
 * @author Cuizhen
 * @date 2018/9/3
 */
public class PictureSelectorHelper {

    private final PictureSelector mSelector;
    private final int mRequestCode;
    private boolean mSingleMode;
    private boolean mEnableCrop;
    private int mMaxSelectNum;

    private PictureSelectorHelper(Activity activity, int requestCode) {
        mSelector = PictureSelector.create(activity);
        mRequestCode = requestCode;
    }

    @NonNull
    public static PictureSelectorHelper with(Activity activity, int requestCode) {
        return new PictureSelectorHelper(activity, requestCode);
    }

    private PictureSelectorHelper(Fragment fragment, int requestCode) {
        mSelector = PictureSelector.create(fragment);
        mRequestCode = requestCode;
    }

    public static PictureSelectorHelper with(Fragment fragment, int requestCode) {
        return new PictureSelectorHelper(fragment, requestCode);
    }

    public PictureSelectorHelper singleMode(boolean singleMode) {
        mSingleMode = singleMode;
        return this;
    }

    public PictureSelectorHelper enableCrop(boolean enableCrop) {
        mEnableCrop = enableCrop;
        return this;
    }

    public PictureSelectorHelper maxSelectNum(int maxSelectNum) {
        mMaxSelectNum = maxSelectNum;
        return this;
    }

    private boolean isSingleMode(){
        return mSingleMode || mMaxSelectNum == 1;
    }

    public PictureSelectorHelper selectPhoto() {
        mSelector.openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
//                .theme(R.style.PictureSelectorNumStyle)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(isSingleMode() ? 2 : mMaxSelectNum)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(isSingleMode() ? PictureConfig.SINGLE : PictureConfig.MULTIPLE)// 多选 or 单选
                .previewImage(true)// 是否可预览图片
                .enablePreviewAudio(true) // 是否可播放音频
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
//                .setOutputCameraPath(Config.DOWNLOAD_FILE_PATH)// 自定义拍照保存路径
                .enableCrop(mSingleMode ? mEnableCrop : false)// 是否裁剪
                .compress(false)// 是否压缩
                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
                .isGif(true)// 是否显示gif图片
                .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
                .circleDimmedLayer(false)// 是否圆形裁剪
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .openClickSound(false)// 是否开启点击声音
                .forResult(mRequestCode);//结果回调onActivityResult code
        return this;
    }

    public PictureSelectorHelper selectVideo() {
        // 进入相册 以下是例子：不需要的api可以不写
        mSelector.openGallery(PictureMimeType.ofVideo())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
//                .theme(R.style.PictureSelectorNumStyle)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                .previewVideo(true)// 是否可预览视频
                .enablePreviewAudio(true) // 是否可播放音频
                .isCamera(true)// 是否显示拍照按钮
//                .setOutputCameraPath(Config.DOWNLOAD_FILE_PATH)// 自定义拍照保存路径
                .compress(false)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
                .videoMaxSecond(60)
                .videoMinSecond(10)
                .videoQuality(1)// 视频录制质量 0 or 1
//                .recordVideoSecond(60)//录制视频秒数 默认60s
                .forResult(mRequestCode);//结果回调onActivityResult code
        return this;
    }

    public List<String> selectResult(int requestCode, int resultCode, Intent data) {
        List<String> selectPath = new ArrayList<>(0);
        if (resultCode == Activity.RESULT_OK && requestCode == mRequestCode) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            for (LocalMedia media : selectList) {
                if (media.isCut()) {
                    selectPath.add(media.getCutPath());
                } else {
                    selectPath.add(media.getPath());
                }
            }
        }
        return selectPath;
    }
}
