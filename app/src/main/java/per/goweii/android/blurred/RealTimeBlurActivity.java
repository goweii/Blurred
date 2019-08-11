package per.goweii.android.blurred;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import per.goweii.burred.Blurred;

public class RealTimeBlurActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_fps;
    private TextView tv_mspf;
    private CheckBox cb_anti_alias;
    private CheckBox cb_fit_xy;
    private PhotoView iv_original;
    private ImageView iv_blurred;
    private SeekBar sb_radius;
    private TextView tv_radius;
    private SeekBar sb_scale;
    private TextView tv_scale;

    private PictureSelectorHelper mHelper;
    private Blurred mBlurred = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_blur);

        Blurred.init(RealTimeBlurActivity.this);

        tv_fps = findViewById(R.id.tv_fps);
        tv_mspf = findViewById(R.id.tv_mspf);
        cb_anti_alias = findViewById(R.id.cb_anti_alias);
        cb_fit_xy = findViewById(R.id.cb_fit_xy);
        iv_original = findViewById(R.id.iv_original);
        iv_blurred = findViewById(R.id.iv_blurred);
        sb_radius = findViewById(R.id.sb_radius);
        tv_radius = findViewById(R.id.tv_radius);
        sb_scale = findViewById(R.id.sb_scale);
        tv_scale = findViewById(R.id.tv_scale);
        tv_scale = findViewById(R.id.tv_scale);

        cb_anti_alias.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBlurred != null) {
                    mBlurred.antiAlias(isChecked);
                }
            }
        });
        cb_fit_xy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBlurred != null) {
                    mBlurred.fitIntoViewXY(isChecked);
                }
            }
        });
        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mBlurred != null) {
                    mBlurred.radius(progress);
                }
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
                if (mBlurred != null) {
                    mBlurred.scale(1F / (progress <= 0 ? 1 : progress));
                }
                tv_scale.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBlurred = Blurred.with(findViewById(R.id.sv))
                .fitIntoViewXY(cb_fit_xy.isChecked())
                .antiAlias(cb_anti_alias.isChecked())
                .scale(1F / (sb_scale.getProgress() <= 0 ? 1 : sb_scale.getProgress()))
                .radius(sb_radius.getProgress())
                .fpsListener(new Blurred.FpsListener() {
                    @Override
                    public void currFps(float fps) {
                        tv_fps.setText(String.format("fps%.1f", fps));
                    }
                })
                .listener(new Blurred.Listener() {
                    @Override
                    public void begin() {
                        start = System.currentTimeMillis();
                    }

                    @Override
                    public void end() {
                        long end = System.currentTimeMillis();
                        long off = end - start;
                        tv_mspf.setText(String.format("mspf%d", off));
                    }
                });
        mBlurred.blur(iv_blurred);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_real_time_blur, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose) {
            mHelper = PictureSelectorHelper.with(RealTimeBlurActivity.this, 1)
                    .singleMode(true)
                    .selectPhoto();
        }
        return true;
    }

    private long start = 0;

    @Override
    protected void onDestroy() {
        mBlurred.reset();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mHelper != null) {
            List<String> imgs = mHelper.selectResult(requestCode, resultCode, data);
            if (imgs != null && imgs.size() > 0) {
                Glide.with(RealTimeBlurActivity.this)
                        .load(imgs.get(0))
                        .into(iv_original);
            }
        }
    }
}
