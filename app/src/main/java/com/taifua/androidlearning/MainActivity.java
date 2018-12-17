package com.taifua.androidlearning;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{

    private Button mBtnLinearlayout;
    private Button mBtnRelativelayout;
    private Button mBtnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnLinearlayout = findViewById(R.id.btn_linearlayout);
        mBtnRelativelayout = findViewById(R.id.btn_relativelayout);
        mBtnTextView = findViewById(R.id.btn_textview);

        setListeners();
    }

    // 监听器方法
    private void setListeners()
    {
        OnClick onClick = new OnClick();
        mBtnLinearlayout.setOnClickListener(onClick);
        mBtnRelativelayout.setOnClickListener(onClick);
        mBtnTextView.setOnClickListener(onClick);
    }

    // 实现监听器接口
    private class OnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            Intent intent = null;
            switch (view.getId())
            {
                // 跳转到LinearLayout演示页面
                case R.id.btn_linearlayout:
                    intent = new Intent(MainActivity.this, LinearlayoutActivity.class);
                    break;
                // 跳转到RelativeLayout演示页面
                case R.id.btn_relativelayout:
                    intent = new Intent(MainActivity.this, RelativelayoutActivity.class);
                    break;
                // 跳转到TextView演示页面
                case R.id.btn_textview:
                    intent = new Intent(MainActivity.this, TextViewActivity.class);
                    break;
                default:
                    break;
            }
            startActivity(intent);
        }
    }
}


