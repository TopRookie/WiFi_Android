package com.wanjie.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class WifiListAdapter extends RecyclerView.Adapter {
    private WifiController mWifiController;
    private Context mContext;

    private List<ScanResult> mData;

    public WifiListAdapter(Context context){
        this.mContext = context;
        mWifiController = WifiController.getInstant(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_wifi_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ScanResult scanResult = mData.get(position);
        ((ItemViewHolder)holder).setItemData(scanResult);
    }

    @Override
    public int getItemCount() {
        return mData!=null?mData.size():0;
    }

    public void setData(List<ScanResult> data){
        mData = new ArrayList<>();
        mData.addAll(data);
    }

    public void clearList(){
        mData.clear();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name;
        TextView tv_desc;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_desc = (TextView) itemView.findViewById(R.id.tv_desc);
        }

        public void setItemData(ScanResult scanResult){
            tv_name.setText(scanResult.SSID);
            tv_desc.setText(scanResult.BSSID);
        }
    }
}
