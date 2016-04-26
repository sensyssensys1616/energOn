package com.nairan.batman_launcher.viewsandadapters;

import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by nrzhang on 8/8/2015.
 */
public abstract class BaseCompatableAdapter<T> extends BaseAdapter {

    private List<T> data;

    public void setData(List<T> data) {
        this.data = data;
    };

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (this.data == null)
            return 0;
        return this.data.size();
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}