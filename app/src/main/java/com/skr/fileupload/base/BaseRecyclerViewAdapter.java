package com.skr.fileupload.base;

import android.support.annotation.AnimRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

/**
 * @author hyw
 * @since 2016/11/18
 */
public class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;
    protected int mLastPosition = -1;
    protected boolean mIsShowFooter;
    protected List<T> mData;
    protected OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public BaseRecyclerViewAdapter(List<T> list) {
        mData = list;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (getItemViewType(position) == TYPE_FOOTER) {
            if (layoutParams != null) {
                if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
                            .getLayoutParams();
                    params.setFullSpan(true);
                }
            }
        }
    }

    protected View getView(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        int itemSize = mData.size();
        if (mIsShowFooter) {
            itemSize += 1;
        }
        return itemSize;
    }

    protected void setItemAppearAnimation(RecyclerView.ViewHolder holder, int position, @AnimRes int type) {
        if (position > mLastPosition/* && !isFooterPosition(position)*/) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), type);
            holder.itemView.startAnimation(animation);
            mLastPosition = position;
        }
    }

    protected boolean isFooterPosition(int position) {
        return (getItemCount() - 1) == position;
    }

    public void add(int position, T item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void addMore(List<T> data) {
        int startPosition = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(startPosition, mData.size());
    }

    public void delete(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> items) {
        mData = items;
    }

    public void showFooter() {
        mIsShowFooter = true;
        notifyItemInserted(getItemCount());
    }

    public void hideFooter() {
        mIsShowFooter = false;
        notifyItemRemoved(getItemCount());
    }

    protected class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
