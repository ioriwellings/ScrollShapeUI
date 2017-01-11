package com.example.jingbin.scrollshapeui.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.jingbin.scrollshapeui.R;
import com.example.jingbin.scrollshapeui.adapter.ListAdapter;
import com.example.jingbin.scrollshapeui.databinding.ActivityMovieDetailBinding;
import com.example.jingbin.scrollshapeui.utils.CommonUtils;
import com.example.jingbin.scrollshapeui.utils.StatusBarUtil;
import com.example.jingbin.scrollshapeui.view.MyNestedScrollView;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.example.jingbin.scrollshapeui.utils.StatusBarUtil.getStatusBarHeight;

/**
 * Created by jingbin on 2017/1/9.
 * 高仿网易云音乐歌单详情页
 */

public class NeteasePlaylistActivity extends AppCompatActivity {

    public final static String IMAGE_URL_LARGE = "https://img5.doubanio.com/lpic/s4477716.jpg";
    public final static String IMAGE_URL_SMALL = "https://img5.doubanio.com/spic/s4477716.jpg";
    public final static String IMAGE_URL_MEDIUM = "https://img5.doubanio.com/mpic/s4477716.jpg";
    public final static String PARAM = "isRecyclerView";
    private ActivityMovieDetailBinding binding;
    // 这个是高斯图背景的高度
    private int imageBgHeight;
    // 在多大范围内变色
    private int slidingDistance;
    private boolean isRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);
        if (getIntent() != null) {
            isRecyclerView = getIntent().getBooleanExtra(PARAM, true);
        }

        setTitleBar();
        setPicture();
        initSlideShapeTheme();

        // RecyclerView列表显示
        if (isRecyclerView) {
            setAdapter();
        } else {// 显示一般文本
            setText();
        }
    }

    /**
     * 高斯背景图和一般图片
     */
    private void setPicture() {
        Glide.with(this)
                .load(IMAGE_URL_LARGE)
                .override((int) CommonUtils.getDimens(R.dimen.movie_detail_width), (int) CommonUtils.getDimens(R.dimen.movie_detail_height))
                .into(binding.include.ivOnePhoto);

        // "14":模糊度；"3":图片缩放4倍后再进行模糊
        Glide.with(this)
                .load(IMAGE_URL_MEDIUM)
                .error(R.drawable.stackblur_default)
                .placeholder(R.drawable.stackblur_default)
                .crossFade(500)
                .bitmapTransform(new BlurTransformation(this, 14, 3))
                .into(binding.include.imgItemBg);
    }

    /**
     * 显示文本
     */
    private void setText() {
        binding.tvTxt.setVisibility(View.VISIBLE);
        binding.xrvCast.setVisibility(View.GONE);
    }

    /**
     * 设置RecyclerView
     */
    private void setAdapter() {
        binding.tvTxt.setVisibility(View.GONE);
        binding.xrvCast.setVisibility(View.VISIBLE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.xrvCast.setLayoutManager(mLayoutManager);
        // 需加，不然滑动不流畅
        binding.xrvCast.setNestedScrollingEnabled(false);
        binding.xrvCast.setHasFixedSize(false);
        final ListAdapter adapter = new ListAdapter(this);
        adapter.notifyDataSetChanged();
        binding.xrvCast.setAdapter(adapter);
    }

    /**
     * toolbar设置
     */
    private void setTitleBar() {
        setSupportActionBar(binding.titleToolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
        }
        binding.titleToolBar.setTitle("1988：我想和这个世界谈谈");
        binding.titleToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    /**
     * 初始化滑动渐变
     */
    private void initSlideShapeTheme() {
        setImgHeaderBg();

        String TAG = "---NeteasePlaylist:";
        // toolbar 的高
        int toolbarHeight = binding.titleToolBar.getLayoutParams().height;
        Log.i(TAG, "toolbar height:" + toolbarHeight);
        final int headerBgHeight = toolbarHeight + getStatusBarHeight(this);
        Log.i(TAG, "headerBgHeight:" + headerBgHeight);

        // 使背景图向上移动到图片的最底端，保留（titlebar+statusbar）的高度
        binding.ivTitleHeadBg.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = binding.ivTitleHeadBg.getLayoutParams();
        ViewGroup.MarginLayoutParams ivTitleHeadBgParams = (ViewGroup.MarginLayoutParams) binding.ivTitleHeadBg.getLayoutParams();
        int marginTop = params.height - headerBgHeight;
        ivTitleHeadBgParams.setMargins(0, -marginTop, 0, 0);
        binding.ivTitleHeadBg.setImageAlpha(0);
        StatusBarUtil.setTranslucentImageHeader(this, 0, binding.titleToolBar);

        // 上移背景图片，使空白状态栏消失(这样下方就空了状态栏的高度)
        if (binding.include.imgItemBg != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.include.imgItemBg.getLayoutParams();
            layoutParams.setMargins(0, -getStatusBarHeight(this), 0, 0);
        }

        ViewGroup.LayoutParams imgItemBgparams = binding.include.imgItemBg.getLayoutParams();
        // 获得高斯图背景的高度
        imageBgHeight = imgItemBgparams.height;

        // 监听改变透明度
        initScrollViewListener();
    }


    /**
     * 加载titlebar背景
     */
    private void setImgHeaderBg() {
        // 高斯模糊背景
        Glide.with(this).load(NeteasePlaylistActivity.IMAGE_URL_MEDIUM)
                .error(R.drawable.stackblur_default)
                .bitmapTransform(new BlurTransformation(this, 14, 3)).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                binding.titleToolBar.setBackgroundColor(Color.TRANSPARENT);
                binding.ivTitleHeadBg.setImageAlpha(0);
                binding.ivTitleHeadBg.setVisibility(View.VISIBLE);
                return false;
            }
        }).into(binding.ivTitleHeadBg);
    }

    private void initScrollViewListener() {
        // 为了兼容23以下
        binding.nsvScrollview.setOnMyScrollChangeListener(new MyNestedScrollView.ScrollInterface() {
            @Override
            public void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollChangeHeader(scrollY);
            }
        });

        int titleBarAndStatusHeight = (int) (CommonUtils.getDimens(R.dimen.nav_bar_height) + getStatusBarHeight(this));
        slidingDistance = imageBgHeight - titleBarAndStatusHeight - (int) (CommonUtils.getDimens(R.dimen.nav_bar_height_more));
    }

    /**
     * 根据页面滑动距离改变Header方法
     */
    private void scrollChangeHeader(int scrolledY) {

//        DebugUtil.error("---scrolledY:  " + scrolledY);
//        DebugUtil.error("-----slidingDistance: " + slidingDistance);

        if (scrolledY < 0) {
            scrolledY = 0;
        }
        float alpha = Math.abs(scrolledY) * 1.0f / (slidingDistance);
//        DebugUtil.error("----alpha:  " + alpha);
        Drawable drawable = binding.ivTitleHeadBg.getDrawable();

        if (drawable != null) {
            if (scrolledY <= slidingDistance) {
                // title部分的渐变
                drawable.mutate().setAlpha((int) (alpha * 255));
                binding.ivTitleHeadBg.setImageDrawable(drawable);
            } else {
                drawable.mutate().setAlpha(255);
                binding.ivTitleHeadBg.setImageDrawable(drawable);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.xrvCast.setFocusable(false);
    }

    /**
     * @param context   activity
     * @param imageView imageView
     */
    public static void start(Activity context, ImageView imageView, boolean isRecyclerView) {
        Intent intent = new Intent(context, NeteasePlaylistActivity.class);
        intent.putExtra(PARAM, isRecyclerView);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                imageView, CommonUtils.getString(R.string.transition_movie_img));//与xml文件对应
        ActivityCompat.startActivity(context, intent, options.toBundle());
    }
}