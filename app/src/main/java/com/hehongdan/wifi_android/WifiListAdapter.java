package com.hehongdan.wifi_android;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：
 *
 * @author hehongdan
 * @version v2018/8/25
 * @date 2018/8/25
 */
public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {
    private static final String TAG = "HHDWifiListAdapter";
    /**
     * 上下文
     */
    private Context mContext;
    private List<ScanResult> mList;
    /**
     * 点击事件
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * 清空列表数据
     */
    public void clearListData(){
        if (null != mList){
            mList.clear();
        }
    }

    /**
     * 设置列表数据
     */
    public void setListData(List<ScanResult> data){
        mList = new ArrayList<>();
        mList.addAll(data);
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param list    适配数据集合
     */
    public WifiListAdapter(Context context, List<ScanResult> list) {
        this.mContext = context;
        this.mList = list;
    }

    /**
     * 创建ViewHolder
     *
     * @param parent   父视图
     * @param viewType 视图类型（根据类型判断并创建不同item的ViewHolder）
     * @return 此视图类型的ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_wifi_layout, parent, false));
        return holder;
    }

    /**
     * 绑定ViewHolder
     *
     * @param holder   ViewHolder
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int lPosition = holder.getLayoutPosition();
        Log.e(TAG, "数据长度=" + mList.size()+"，原始下标="+position+"，处理下标="+lPosition);
        if (null != mList && mList.size() > 0) {
            holder.ssid.setText(mList.get(lPosition).SSID);
            holder.bSsid.setText(mList.get(lPosition).BSSID);
        }

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.itemView, lPosition);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, lPosition);
                    return false;
                }
            });
        }
    }

    /**
     * 获取列表总数
     *
     * @return 总数
     */
    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    /**
     * 列表事件接口（外部类需要时调用）
     *
     * @param onItemClickListener 接口监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * 列表事件接口
     */
    public interface OnItemClickListener {
        /**
         * 点击事件
         *
         * @param view      点击视图
         * @param position  点击位置
         */
        void onItemClick(View view, int position);

        /**
         * 长按事件
         *
         * @param view      长按视图
         * @param position  长按位置
         */
        void onItemLongClick(View view, int position);
    }

    /**
     * ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ssid;
        private TextView bSsid;

        public ViewHolder(View view) {
            super(view);

            ssid = view.findViewById(R.id.tv_name);
            bSsid = view.findViewById(R.id.tv_desc);
        }
    }
}
