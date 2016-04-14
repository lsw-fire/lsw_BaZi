package lsw.liuyao.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import lsw.Util;
import lsw.library.DateExt;
import lsw.library.LunarCalendar;
import lsw.library.StringHelper;
import lsw.liuyao.HexagramAnalyzerActivity;
import lsw.liuyao.R;
import lsw.liuyao.common.IntentKeys;
import lsw.liuyao.model.HexagramRow;
import lsw.liuyao.wxapi.WXEntryActivity;
import lsw.liuyao.wxapi.WeiXinSendMessageHelper;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by swli on 8/18/2015.
 */
public class HexagramListAdapter extends BaseSwipeAdapter {

    ArrayList<HexagramRow> rows;
    Activity context;
    Database database;

    public interface OnReload
    {
        void invoke(int index);
    }

    OnReload onReload;

    public void setOnReload(OnReload onReload)
    {
        this.onReload = onReload;
    }

    public void setRows(ArrayList<HexagramRow> rows)
    {
        this.rows = rows;
    }

    public ArrayList<HexagramRow> getRows()
    {
        return rows;
    }

    public HexagramListAdapter(ArrayList<HexagramRow> rows,  Activity context)
    {
        this.rows = rows;
        this.context = context;
        this.database = new Database(context);
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return rows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int i, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.hexagram_list_item, null);
    }

    @Override
    public void fillValues(int i, View view) {
        SwipeLayout swipeLayout = ((SwipeLayout) view);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.back));

        final HexagramRow item = rows.get(i);
        ViewHolder holder;
//       if (view == null) {
            holder = new ViewHolder();
            holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
            holder.tvOriginalName = (TextView) view.findViewById(R.id.tvOriginalName);
            holder.tvChangedName = (TextView) view.findViewById(R.id.tvChangedName);

            holder.btnAnalyze = (TextView) view.findViewById(R.id.btnAnalyze);
            holder.btnDelete = (TextView) view.findViewById(R.id.btnDelete);

            holder.tvNote = (TextView) view.findViewById(R.id.tvNote);
//            view.setTag(holder);
//        } else {
//            holder = (ViewHolder) view.getTag();
//        }

        DateExt tempDateExt = new DateExt(item.getDate());
        int indexOfWeek = tempDateExt.getIndexOfWeek();
        String weekDay = indexOfWeek == 0 ? "日" : LunarCalendar.toChineseDayInWeek(indexOfWeek);
        holder.tvDate.setText(tempDateExt.getFormatDateTime("yyyy年MM月dd日") + " (星期" + weekDay + ")");
        holder.tvOriginalName.setText("主卦: " + item.getOriginalName());
        String changedName = item.getChangedName();
        if (!StringHelper.isNullOrEmpty(changedName)) {
            holder.tvChangedName.setText("变卦: " + item.getChangedName());
        } else {
            holder.tvChangedName.setText("");
        }

        holder.tvNote.setText(item.getNote());
        holder.tvNote.setSelected(true);

        holder.btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mIntent = new Intent(context, HexagramAnalyzerActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString(IntentKeys.FormatDate, item.getDate());
                mBundle.putString(IntentKeys.OriginalName, item.getOriginalName());
                mBundle.putString(IntentKeys.ChangedName, item.getChangedName());
                mBundle.putInt(IntentKeys.HexagramRowId, item.getId());
                mIntent.putExtras(mBundle);

                context.startActivity(mIntent);
                //context.finish();
            }
        });

        final int index = i;
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.deleteHexagram(item.getId());
                if(onReload != null)
                    onReload.invoke(index);
            }
        });

    }

    static class ViewHolder {
        TextView tvDate;
        TextView tvOriginalName;
        TextView tvChangedName;
        TextView tvNote;
        TextView btnAnalyze;
        TextView btnDelete;
    }
}
