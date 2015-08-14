package lsw.liuyao;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import lsw.hexagram.Analyzer;
import lsw.hexagram.Builder;
import lsw.library.DateExt;
import lsw.library.LunarCalendarWrapper;
import lsw.liuyao.common.IntentKeys;
import lsw.model.Hexagram;
import lsw.model.Line;

/**
 * Created by swli on 8/7/2015.
 */
public class HexagramAnalyzer extends Activity implements View.OnTouchListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏,全屏显示
        // 第一种：
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 第二种：（两种方法效果一样）
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.hexagram_analyze_activity);

        Bundle bundle = getIntent().getExtras();
        String formatDate = getIntent().getStringExtra(IntentKeys.FormatDate);

        ArrayList<Line> models = (ArrayList<Line>)bundle.getSerializable(IntentKeys.LineModelList);

        Builder builder = Builder.getInstance(this);
        Analyzer analyzer = new Analyzer(this);
        try {
            Hexagram hexagram = builder.getHexagramByLines(models, true);
            Hexagram changed = builder.getChangedHexagramByOriginal(hexagram, false);

            LunarCalendarWrapper lunarCalendarWrapper = new LunarCalendarWrapper(new DateExt(formatDate));
            String eraMonth = lunarCalendarWrapper.toStringWithSexagenary(lunarCalendarWrapper.getChineseEraOfMonth());
            String eraDay = lunarCalendarWrapper.toStringWithSexagenary(lunarCalendarWrapper.getChineseEraOfDay());

            StringBuilder stringBuilder = analyzer.analyzeHexagramResult(eraMonth, eraDay, hexagram, changed);
            ArrayList<String> list = analyzer.orderedStringResult(stringBuilder.toString());

            HexagramBuilderFragment analyzerFragment = HexagramBuilderFragment.newInstance(hexagram, changed, formatDate);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fl_Hexagram_Analyzer, analyzerFragment, null);

            HexagramAutoAnalyzerFragment autoAnalyzerFragment = HexagramAutoAnalyzerFragment.newInstance(list);
            ft.replace(R.id.fl_Hexagram_Auto_Analyzer, autoAnalyzerFragment,null);

            ft.commit();
        }
        catch (Exception ex)
        {
            Log.e("Hexagram Builder", ex.getMessage());
        }

        initValues();

//        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                // ---Call it directly---
//                scrollToContent();
//                return false;
//            }
//        });
    }

    //GestureDetector gestureDetector;

    public static final int SNAP_VELOCITY = 200;
    private int screenWidth;
    private int leftEdge;
    private int rightEdge = 0;
    private int menuPadding = 900;
    private View content;
    private View menu;
    private LinearLayout.LayoutParams menuParams;
    private float xDown;
    private float xMove;
    private float xUp;
    private boolean isMenuVisible;
    private VelocityTracker mVelocityTracker;

    private void initValues() {
        //WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

        //screenWidth = window.getDefaultDisplay().getWidth();

        content = findViewById(R.id.content);
        menu = findViewById(R.id.menu);

        menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
        //menuParams.width = screenWidth - menuPadding;
        menuParams.width = screenWidth/5*2;
        leftEdge = -menuParams.width + 90;
        menuParams.leftMargin = leftEdge;
        content.getLayoutParams().width = screenWidth-90;

        scrollToMenu();

        content.setOnTouchListener(this);
        //menu.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

//        if(isMenuVisible) {
//            scrollToContent();
//            return false;
//        }

        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown);
                if (isMenuVisible) {
                    menuParams.leftMargin = distanceX;
                } else {
                    menuParams.leftMargin = leftEdge + distanceX;
                }
                if (menuParams.leftMargin < leftEdge) {
                    menuParams.leftMargin = leftEdge;
                } else if (menuParams.leftMargin > rightEdge) {
                    menuParams.leftMargin = rightEdge;
                }
                menu.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                if (wantToShowMenu()) {
                    if (shouldScrollToMenu()) {
                        scrollToMenu();
                    } else {
                        scrollToContent();
                    }
                } else if (wantToShowContent()) {
                    if (shouldScrollToContent()) {
                        scrollToContent();
                    } else {
                        scrollToMenu();
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    private boolean wantToShowContent() {
        return xUp - xDown < 0 && isMenuVisible;
    }


    private boolean wantToShowMenu() {
        return xUp - xDown > 0 && !isMenuVisible;
    }


    private boolean shouldScrollToMenu() {
        return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }


    private boolean shouldScrollToContent() {
        return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }


    private void scrollToMenu() {
        new ScrollTask().execute(30);
    }


    private void scrollToContent() {
        new ScrollTask().execute(-30);
    }

    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }


    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }


    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }


    class ScrollTask extends AsyncTask<Integer,Integer,Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = menuParams.leftMargin;
            while (true) {
                leftMargin = leftMargin + speed[0];
                if (leftMargin > rightEdge) {
                    leftMargin = rightEdge;
                    break;
                }
                if (leftMargin < leftEdge) {
                    leftMargin = leftEdge;
                    break;
                }
                publishProgress(leftMargin);
                sleep(5);
            }
            if (speed[0] > 0) {
                isMenuVisible = true;
            } else {
                isMenuVisible = false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            menuParams.leftMargin = leftMargin[0];
            menu.setLayoutParams(menuParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            menuParams.leftMargin = leftMargin;
            menu.setLayoutParams(menuParams);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}