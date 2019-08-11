package per.goweii.android.blurred;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private CheckBox cb_real_time;
    private PictureSelectorHelper mHelper;
    private Bitmap mBitmapOriginal;
    private Bitmap mBitmapBlurred;

    private int color1 = 0;
    private int color2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Blurred.init(MainActivity.this);

        color1 = Color.parseColor("#33000000");
        color2 = Color.parseColor("#33ff0000");

        iv_original = findViewById(R.id.iv_original);
        tv_original = findViewById(R.id.tv_original);
        iv_blurred = findViewById(R.id.iv_blurred);
        tv_blurred = findViewById(R.id.tv_blurred);
        sb_radius = findViewById(R.id.sb_radius);
        tv_radius = findViewById(R.id.tv_radius);
        sb_scale = findViewById(R.id.sb_scale);
        tv_scale = findViewById(R.id.tv_scale);
        cb_keep_size = findViewById(R.id.cb_keep_size);
        cb_real_time = findViewById(R.id.cb_real_time);
        cb_real_time.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Blurred.realTimeMode(isChecked);
            }
        });
        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_radius.setText("" + progress);
                if (cb_real_time.isChecked()) {
                    blurAndUpdateView();
                }
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
                if (cb_real_time.isChecked()) {
                    blurAndUpdateView();
                }
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_real_time_blur) {
            startActivity(new Intent(this, RealTimeBlurActivity.class));
        }
        return true;
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
                if (mBitmapOriginal != null) {
                    blurAndUpdateView();
                }
                break;
        }
    }

    private void blurAndUpdateView() {
        long start = System.currentTimeMillis();
        blur();
        long end = System.currentTimeMillis();
        long off = end - start;
        Log.i(TAG, "process:" + off);
        setInfo(true, end - start);
        if (off <= 16) {
            tv_blurred.setBackgroundColor(color1);
        } else {
            tv_blurred.setBackgroundColor(color2);
        }
        if (mBitmapBlurred != null) {
            iv_blurred.setImageBitmap(mBitmapBlurred);
        }
    }

    private Blurred mBlurred = null;

    private void blur() {
        if (mBlurred == null) {
            mBlurred = new Blurred();
        }
        mBitmapBlurred = mBlurred.bitmap(mBitmapOriginal)
                .keepSize(cb_keep_size.isChecked())
                .recycleOriginal(false)
                .scale(1F / sb_scale.getProgress())
                .radius(sb_radius.getProgress())
                .blur();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mHelper != null) {
            List<String> imgs = mHelper.selectResult(requestCode, resultCode, data);
            if (imgs != null && imgs.size() > 0) {
                Glide.with(MainActivity.this)
                        .load(imgs.get(0))
                        .into(iv_original);
                iv_blurred.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

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
