package cn.jimmie.learn.bufferknifedemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.jimmie.learn.butterknife.ButterKnife;
import cn.jimmie.learn.butterknife.Unbinder;
import cn.jimmie.learn.butterknife_annotations.BindView;
import cn.jimmie.learn.butterknife_annotations.OnClick;


public class MainActivity extends AppCompatActivity {
    // 解绑器
    private Unbinder unbinder;

    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.btn)
    Button button;

    int id = 0;

    @OnClick({R.id.btn, R.id.btn2})
    void click(View view) {
        Toast.makeText(this, "i am a toast !!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            button.setText("i'm button " + (++id));
            btn2.setText("xxx button " + (++id));
        });
        // 绑定
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑, 移除监听器等
        if (unbinder != null) unbinder.unbind();
    }
}
