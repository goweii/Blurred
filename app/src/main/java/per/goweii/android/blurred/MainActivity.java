package per.goweii.android.blurred;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import per.goweii.burred.Blurred;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ImageView iv_original;
    private TextView tv_original;
    private ImageView iv_blurred;
    private TextView tv_blurred;
    private SeekBar sb_radius;
    private TextView tv_radius;
    private SeekBar sb_scale;
    private TextView tv_scale;
    private CheckBox cb_keep_size;
    private PictureSelectorHelper mHelper;
    private Bitmap mBitmapOriginal;
    private Bitmap mBitmapBlurred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_original = findViewById(R.id.iv_original);
        tv_original = findViewById(R.id.tv_original);
        iv_blurred = findViewById(R.id.iv_blurred);
        tv_blurred = findViewById(R.id.tv_blurred);
        sb_radius = findViewById(R.id.sb_radius);
        tv_radius = findViewById(R.id.tv_radius);
        sb_scale = findViewById(R.id.sb_scale);
        tv_scale = findViewById(R.id.tv_scale);
        cb_keep_size = findViewById(R.id.cb_keep_size);
        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_radius.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sb_scale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_scale.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        iv_original.setOnClickListener(this);
        iv_blurred.setOnClickListener(this);

        Blurred.init(MainActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.iv_original:
                mHelper = PictureSelectorHelper.with(MainActivity.this, 1)
                        .singleMode(true)
                        .selectPhoto();
                break;
            case R.id.iv_blurred:
                long start = System.currentTimeMillis();
                blur();
                long end = System.currentTimeMillis();
                Log.i(TAG, "process:" + (end - start));
                setInfo(true, end - start);
                if (mBitmapBlurred != null) {
                    Glide.with(MainActivity.this)
                            .load(mBitmapBlurred)
                            .into(iv_blurred);
                }
                break;
        }
    }

    private void blur() {
        mBitmapBlurred = Blurred.with(mBitmapOriginal)
                .keepSize(cb_keep_size.isChecked())
                .recycleOriginal(false)
                .scale(sb_scale.getProgress())
                .radius(sb_radius.getProgress())
                .blur();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mHelper != null) {
            List<String> imgs = mHelper.selectResult(requestCode, resultCode, data);
            if (imgs != null && imgs.size() > 0) {
                mBitmapOriginal = BitmapFactory.decodeFile(imgs.get(0));

                Glide.with(MainActivity.this)
                        .load(mBitmapOriginal)
                        .into(iv_original);

                setInfo(false, 0);
            }
        }
    }

    private void setInfo(boolean isBlurred, long time) {
        Bitmap bitmap;
        TextView textView;
        if (isBlurred) {
            bitmap = mBitmapBlurred;
            textView = tv_blurred;
        } else {
            bitmap = mBitmapOriginal;
            textView = tv_original;
        }
        String info = "";
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            info = w + "*" + h;
            if (isBlurred) {
                info = info + ", time:" + time + "ms";
            }
        }
        textView.setText(info);
    }
}
