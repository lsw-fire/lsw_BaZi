package lsw.liuyao;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import lsw.library.DateExt;
import lsw.liuyao.common.DateTimePickerDialog;
import lsw.liuyao.common.IntentKeys;
import lsw.liuyao.common.LineDragListener;
import lsw.model.EnumLineSymbol;
import lsw.model.Line;


public class HexagramBuilder extends Activity implements LineDragListener.OnDropInteraction {

    private LinearLayout linearLayout1,linearLayout2,linearLayout3,linearLayout4,linearLayout5,linearLayout6;
    private ImageView ivYin,ivYang,ivLaoYin,ivLaoYang;

    private TextView tvDateSelector;

    private void initControls(){

        tvDateSelector = (TextView) findViewById(R.id.tvDateSelect);

        ivYin = (ImageView)findViewById(R.id.ivYin);
        ivLaoYin = (ImageView)findViewById(R.id.ivLaoYin);
        ivYang = (ImageView)findViewById(R.id.ivYang);
        ivLaoYang = (ImageView)findViewById(R.id.ivLaoYang);

        ivYang.setOnTouchListener(new MyTouchListener(EnumLineSymbol.Yang));
        ivYin.setOnTouchListener(new MyTouchListener(EnumLineSymbol.Yin));
        ivLaoYang.setOnTouchListener(new MyTouchListener(EnumLineSymbol.LaoYang));
        ivLaoYin.setOnTouchListener(new MyTouchListener(EnumLineSymbol.LaoYin));

        linearLayout1 = (LinearLayout)findViewById(R.id.llLine1);
        linearLayout2 = (LinearLayout)findViewById(R.id.llLine2);
        linearLayout3 = (LinearLayout)findViewById(R.id.llLine3);
        linearLayout4 = (LinearLayout)findViewById(R.id.llLine4);
        linearLayout5 = (LinearLayout)findViewById(R.id.llLine5);
        linearLayout6 = (LinearLayout)findViewById(R.id.llLine6);

        LineDragListener listener1 = new LineDragListener(1);
        listener1.setOnDropInteraction(this);
        LineDragListener listener2 = new LineDragListener(2);
        listener2.setOnDropInteraction(this);
        LineDragListener listener3 = new LineDragListener(3);
        listener3.setOnDropInteraction(this);
        LineDragListener listener4 = new LineDragListener(4);
        listener4.setOnDropInteraction(this);
        LineDragListener listener5 = new LineDragListener(5);
        listener5.setOnDropInteraction(this);
        LineDragListener listener6 = new LineDragListener(6);
        listener6.setOnDropInteraction(this);

        linearLayout1.setOnDragListener(listener1);
        linearLayout2.setOnDragListener(listener2);
        linearLayout3.setOnDragListener(listener3);
        linearLayout4.setOnDragListener(listener4);
        linearLayout5.setOnDragListener(listener5);
        linearLayout6.setOnDragListener(listener6);
    }

    DateExt initialDateExt;
    String formatDateTime = "yyyy年MM月dd日 HH:mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hexagram_maintain_activity);

        initControls();

        initialDateExt = new DateExt();
        tvDateSelector.setText(initialDateExt.getFormatDateTime(formatDateTime));

        tvDateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimePickerDialog dialog = new DateTimePickerDialog(initialDateExt,HexagramBuilder.this);
                dialog.setCallBack(new DateTimePickerDialog.ICallBack() {
                    @Override
                    public void invoke(DateExt dateExt) {
                        tvDateSelector.setText(dateExt.getFormatDateTime(formatDateTime));
                        initialDateExt = dateExt;
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private Point lastTouch;
    private ImageView moveImageView;
    private EnumLineSymbol enumLineSymbol;
    private HashMap<Integer,EnumLineSymbol> lineSymbolHashMap = new HashMap<Integer, EnumLineSymbol>();

    @Override
    public void OnDrop(View containerView, int position) {
        LinearLayout container = (LinearLayout) containerView;
        container.removeAllViews();
        container.addView(moveImageView);
        lineSymbolHashMap.put(position, enumLineSymbol);
    }

    @Override
    public void OnEntered(View containerView, int position)
    {
        LinearLayout container = (LinearLayout) containerView;
    }

    private final class MyTouchListener implements View.OnTouchListener {

        EnumLineSymbol enumYaoType;

        public MyTouchListener(EnumLineSymbol type)
        {
            this.enumYaoType = type;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                enumLineSymbol = enumYaoType;
                ImageView iv = (ImageView)view;
                Drawable drawable = iv.getDrawable();

                moveImageView = new ImageView(HexagramBuilder.this);
                moveImageView.setImageDrawable(drawable);

                lastTouch = new Point((int) motionEvent.getX(), (int) motionEvent.getY()) ;

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    private class MyDragShadowBuilder extends View.DragShadowBuilder {
        private View dragView;

        public MyDragShadowBuilder(View v) {
            super(v);
            this.dragView = v;
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            int width, height;
            width = getView().getWidth();
            height = getView().getHeight();
            size.set(width, height);

            if (lastTouch != null) {
                touch.set(lastTouch.x, lastTouch.y);
            }
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            dragView.draw(canvas);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reload(View view)
    {
        Intent intent = new Intent(HexagramBuilder.this, HexagramBuilder.class);
        startActivity(intent);
        finish();
    }

    public void zhuangGua(View view)
    {
        if(lineSymbolHashMap.size() != 6)
        {
            Toast.makeText(this,"爻位填充不全!", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Line> lines = new ArrayList<Line>();

        for(Integer key: lineSymbolHashMap.keySet())
        {
            Line line = new Line();
            line.setPosition(key);
            line.setLineSymbol(lineSymbolHashMap.get(key));
            lines.add(line);
        }

        Intent mIntent = new Intent(this,HexagramAnalyzer.class);
        Bundle mBundle = new Bundle();
        mBundle.putString(IntentKeys.FormatDate, initialDateExt.getFormatDateTime());
        mBundle.putSerializable(IntentKeys.LineModelList,lines);
        mIntent.putExtras(mBundle);

        startActivity(mIntent);
    }
}
