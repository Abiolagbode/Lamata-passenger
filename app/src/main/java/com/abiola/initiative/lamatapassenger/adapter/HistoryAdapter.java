package com.abiola.initiative.lamatapassenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abiola.initiative.lamatapassenger.R;
import com.abiola.initiative.lamatapassenger.object.HistoryObject;

import java.util.ArrayList;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private HistoryAdapterListener listener;
    public static ArrayList<HistoryObject> historyList = new ArrayList<>();

    public interface HistoryAdapterListener {
        public void onHistoryClick(ArrayList<HistoryObject> resultList, int position);
    }

    public HistoryAdapter(Context context, HistoryAdapterListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.history_adapter, parent, false);
        ViewHolder mPredictionHolder = new ViewHolder(convertView);
        return mPredictionHolder;
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, final int position) {
        HistoryObject currentHistory = historyList.get(position);
        holder.pickup_txt_vw.setSelected(true);
        holder.dropoff_txt_vw.setSelected(true);
        holder.pickup_txt_vw.setText(currentHistory.getPickupName());
        holder.dropoff_txt_vw.setText(currentHistory.getDropoffName());
        holder.date_txt_vw.setText(currentHistory.getDate());
        holder.time_txt_vw.setText(currentHistory.getTime());
        holder.row_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onHistoryClick(historyList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public void clearHistory() {
        historyList.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView pickup_txt_vw;
        public TextView dropoff_txt_vw;
        public TextView date_txt_vw;
        public TextView time_txt_vw;
        public LinearLayout row_layout;
        public ViewHolder(View itemView) {
            super(itemView);
            pickup_txt_vw = itemView.findViewById(R.id.pickup_txt_vw);
            dropoff_txt_vw = itemView.findViewById(R.id.dropoff_txt_vw);
            date_txt_vw = itemView.findViewById(R.id.date_txt_vw);
            time_txt_vw = itemView.findViewById(R.id.time_txt_vw);
            row_layout = itemView.findViewById(R.id.row_layout);
        }
    }
}
