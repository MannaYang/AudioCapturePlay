package com.manna.capture.adapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * adapter数据差异对比
 */
public class AdapterDiffCallback extends DiffUtil.Callback {

    private List<ItemViewType> oldList, newList;

    AdapterDiffCallback(List<ItemViewType> oldList, List<ItemViewType> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getClass().equals(newList.get(newItemPosition).getClass());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ItemViewType oldBean = oldList.get(oldItemPosition);
        ItemViewType newBean = newList.get(newItemPosition);
        return oldBean.equals(newBean);
    }
}
