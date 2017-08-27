package org.zhangjie.onlab;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.util.Locale;

/**
 * Created by H151136 on 8/27/2017.
 */

public class HelpActivity extends Activity implements OnPageChangeListener
        , OnLoadCompleteListener, OnDrawListener {

    private PDFView mPDFView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        mPDFView = (PDFView) findViewById(R.id.pdfView);
        String locale = Locale.getDefault().getLanguage();
        Log.d("Onlab", "locale language: " + locale);
        if(locale.endsWith("zh")) {
            displayFromAssets("help_chn.pdf");
        } else {
            displayFromAssets("help_en.pdf");
        }
    }

    private void displayFromAssets(String assetFileName) {
        mPDFView.fromAsset(assetFileName)   //设置pdf文件地址
                .defaultPage(6)         //设置默认显示第1页
                .onPageChange(this)     //设置翻页监听
                .onLoad(this)           //设置加载监听
                .onDraw(this)            //绘图监听
                .showMinimap(false)     //pdf放大的时候，是否在屏幕的右上角生成小地图
                .swipeVertical(false)  //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .enableSwipe(true)   //是否允许翻页，默认是允许翻页
                // .pages( 2 , 3 , 4 , 5  )  //把2 , 3 , 4 , 5 过滤掉
                .load();
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }
}
