package com.example.dong.odometer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<StepRecord> records;

    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;

    public StepAdapter(Context context, List<StepRecord> records) {
        this.context = context;
        this.records = records;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //列表为空
        if (viewType==VIEW_TYPE_EMPTY){
            View emptyView = LayoutInflater.from(context).inflate(R.layout.empty_view,viewGroup,false);
            return new RecyclerView.ViewHolder(emptyView){};
        }
        //正常
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder){
            ViewHolder holder = (ViewHolder) viewHolder;
            Log.e("List", "list size" + records.size());
            StepRecord record = records.get(position);
            holder.step_tv.setText(record.getStep() + "");
            holder.date_tv.setText(record.getDate() + "");
        }
    }

    @Override
    public int getItemCount() {
        if (records != null) {
            return records.size();
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (records==null){
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView date_tv;
        TextView step_tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date_tv = itemView.findViewById(R.id.item_date);
            step_tv = itemView.findViewById(R.id.item_step);

        }
    }
}
