package com.nairan.batman_launcher.viewsandadapters;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nrzhang on 10/8/2015.
 */
public abstract class BaseDynamicGridAdapter<T> extends AbstractDynamicGridAdapter {

    private Context mContext;
    private ArrayList<T> mItems = new ArrayList<T>();
    private int mColumnCount;

    protected BaseDynamicGridAdapter(Context context, int columnCount) {
        this.mContext = context;
        this.mColumnCount = columnCount;
    }

    public BaseDynamicGridAdapter(Context context, List<?> items, int columnCount) {
        mContext = context;
        mColumnCount = columnCount;
        init(items);
    }

    private void init(List<?> items) {
        clearStableIdMap();
        this.mItems.clear();

        addAllStableId(items);
        this.mItems.addAll((ArrayList) items);
    }

    public void set(List<?> items) {
        if(items == null || items.isEmpty())
            return;

        clear();
        init(items);
        notifyDataSetChanged();
    }

    public void clear() {
        clearStableIdMap();
        mItems.clear();
        notifyDataSetChanged();
    }

    public void add(T item) {
        addStableId(item);
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void add(int position, T item) {
        addStableId(item);
        mItems.add(position, item);
        notifyDataSetChanged();
    }

    public void add(List<?> items) {
        addAllStableId(items);
        this.mItems.addAll((ArrayList) items);
        notifyDataSetChanged();
    }


    public void remove(T item) {
        mItems.remove(item);
        removeStableID(item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getColumnCount() {
        return mColumnCount;
    }

    public void setColumnCount(int columnCount) {
        this.mColumnCount = columnCount;
        notifyDataSetChanged();
    }

    @Override
    public void reorderItems(int originalPosition, int newPosition) {
        if (newPosition < getCount()) {
            DynamicGridUtils.reorder(mItems, originalPosition, newPosition);
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean canReorder(int position) {
        return true;
    }

    public List<T> getItems() {
        return mItems;
    }

    protected Context getContext() {
        return mContext;
    }
}
