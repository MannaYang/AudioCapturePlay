package com.manna.capture.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础RecyclerView adapter
 */
public class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ItemViewType> itemViewTypeList;

    public BaseAdapter(Context context, List<ItemViewType> viewTypeList) {
        this.context = context;
        itemViewTypeList = new ArrayList<>();
        if (viewTypeList.size() > 0) {
            itemViewTypeList.addAll(viewTypeList);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(viewType, parent, false);
        ItemViewType itemViewType = getItemViewTypeByViewType(viewType);
        return itemViewType != null ? itemViewType.getViewHolder(itemView) : null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewType itemViewType = getItemViewTypeByPosition(position);
        int index = getItemViewTypeIndexByPosition(position);
        if (itemViewType != null) {
            itemViewType.bindViewHolder(holder, index);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (ItemViewType viewType : itemViewTypeList) {
            count = count + viewType.getItemCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        ItemViewType itemViewType = getItemViewTypeByPosition(position);
        return itemViewType != null ? itemViewType.getLayoutId() : 0;
    }

    private ItemViewType getItemViewTypeByPosition(int position) {
        int count = 0;
        for (ItemViewType itemViewType : itemViewTypeList) {
            count = count + itemViewType.getItemCount();
            if (position < count) {
                return itemViewType;
            }
        }
        return null;
    }

    private ItemViewType getItemViewTypeByViewType(int viewType) {
        for (ItemViewType itemViewType : itemViewTypeList) {
            if (itemViewType.getLayoutId() == viewType) {
                return itemViewType;
            }
        }
        return null;
    }

    /**
     * 返回itemViewTypeList所在index
     *
     * @param position:当前position
     * @return :int
     */
    private int getItemViewTypeIndexByPosition(int position) {
        int count = 0;
        for (ItemViewType itemType : itemViewTypeList) {
            count = count + itemType.getItemCount();
            if (position < count) {
                int preItemCount = (count - itemType.getItemCount());
                return position - preItemCount;
            }
        }
        return -1;
    }

    public void setRefreshData(List<ItemViewType> data) {
        if (itemViewTypeList.size() == 0) {
            itemViewTypeList.addAll(data);
            notifyItemInserted(0);
        } else {
            DiffUtil.DiffResult result =
                    DiffUtil.calculateDiff(new AdapterDiffCallback(itemViewTypeList, data), false);
            result.dispatchUpdatesTo(this);
            itemViewTypeList.clear();
            itemViewTypeList.addAll(data);
        }
    }

    public void setLoadMoreData(List<ItemViewType> data) {
        itemViewTypeList.addAll(data);
        notifyItemInserted(itemViewTypeList.size() - 1);
    }

    public void setEmptyData() {
        this.itemViewTypeList.clear();
    }
}
