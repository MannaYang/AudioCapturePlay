package com.manna.capture.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 每种类型的约束
 */
public interface ItemViewType {

    /**
     * item个数
     *
     * @return :count
     */
    int getItemCount();

    /**
     * 布局id
     *
     * @return :resId
     */
    int getLayoutId();

    /**
     * 继承RecyclerView.ViewHolder
     *
     * @param viewHolder ：每种类型的具体实现
     * @return ：RecyclerView.ViewHolder
     */
    RecyclerView.ViewHolder getViewHolder(View viewHolder);

    /**
     * 返回创建完成的view holder
     *
     * @param viewHolder :每种类型
     * @param index      ：每种类型在类型数组中的下标,方便点击事件处理等
     */
    void bindViewHolder(RecyclerView.ViewHolder viewHolder, int index);
}
