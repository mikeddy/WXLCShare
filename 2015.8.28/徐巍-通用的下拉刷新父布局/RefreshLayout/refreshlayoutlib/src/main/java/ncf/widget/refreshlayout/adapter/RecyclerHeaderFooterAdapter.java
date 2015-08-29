package ncf.widget.refreshlayout.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XuWei on 15/8/21.
 */
public abstract class RecyclerHeaderFooterAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** item type header */
    public static final int ITEMTYPE_HEADER = 0x009001;
    /** item type footer */
    public static final int ITEMTYPE_FOOTER = 0x009002;

    /** HeaderView列表 */
    private List<View> mHeaderViewList;
    /** FooterView列表 */
    private List<View> mFooterViewList;

    public RecyclerHeaderFooterAdapter(RecyclerView recyclerView) {
        if (recyclerView == null) {
            throw new RuntimeException("RecyclerView不能为空");
        }

        if (recyclerView.getLayoutManager() == null) {
            throw new RuntimeException("需要先设置LayoutManager");
        }

        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == ITEMTYPE_HEADER || getItemViewType(position) == ITEMTYPE_FOOTER)
                        return manager.getSpanCount();
                    return 1;
                }
            });
        }

    }

    public void addHeaderView(View headerView) {
        if (headerView == null)
            return;

        if (mHeaderViewList == null)
            mHeaderViewList = new ArrayList<>();

        if (mHeaderViewList.contains(headerView))
            return;

        mHeaderViewList.add(headerView);
        notifyDataSetChanged();
    }

    public void addFooterView(View footerView) {
        if (footerView == null)
            return;

        if (mFooterViewList == null)
            mFooterViewList = new ArrayList<>();

        if (mFooterViewList.contains(footerView))
            return;

        mFooterViewList.add(footerView);
        notifyDataSetChanged();
    }

    /**
     * 获取Header数量
     * @return
     */
    public int getHeaderViewCount() {
        return mHeaderViewList == null ? 0 : mHeaderViewList.size();
    }

    /**
     * 获取Footer数量
     * @return
     */
    public int getFooterViewCount() {
        return mFooterViewList == null ? 0 : mFooterViewList.size();
    }

    @Override
    final public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEMTYPE_HEADER || viewType == ITEMTYPE_FOOTER) {
            HeaderFooterViewHolder holder = new HeaderFooterViewHolder(new LinearLayout(parent.getContext()));
            if (((RecyclerView)parent).getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setFullSpan(true);
                holder.itemView.setLayoutParams(layoutParams);
            } else {
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                holder.itemView.setLayoutParams(layoutParams);
            }

            return holder;
        }
        return onCreateContentViewHolder(parent, viewType);
    }

    @Override
    final public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == ITEMTYPE_HEADER) {
            HeaderFooterViewHolder holder = (HeaderFooterViewHolder) viewHolder;
            View headerView = mHeaderViewList.get(position);
            if (headerView.getParent() != null)
                ((ViewGroup)headerView.getParent()).removeView(headerView);

            holder.mContentViewContainer.removeAllViews();
            holder.mContentViewContainer.addView(headerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return;
        }

        if (getItemViewType(position) == ITEMTYPE_FOOTER) {
            HeaderFooterViewHolder holder = (HeaderFooterViewHolder) viewHolder;
            View footerView = mFooterViewList.get(position - getHeaderViewCount() - getContentItemCount());
            if (footerView.getParent() != null)
                ((ViewGroup)footerView.getParent()).removeView(footerView);

            holder.mContentViewContainer.removeAllViews();
            holder.mContentViewContainer.addView(footerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return;
        }

        onBindContentViewHolder((VH) viewHolder, position - getHeaderViewCount());
    }

    @Override
    final public int getItemCount() {
        //为Heaer数量＋Footer数量＋内容Item数量
        return getContentItemCount() + getHeaderViewCount() + getFooterViewCount();
    }

    @Override
    final public int getItemViewType(int position) {
        if (position < getHeaderViewCount())
            return ITEMTYPE_HEADER;
        if (position >= getHeaderViewCount() + getContentItemCount())
            return ITEMTYPE_FOOTER;

        return getContentItemViewType(position - getHeaderViewCount());
    }

    /**
     * 获取内容Item的数量，不包含Header及Footer
     * @return
     */
    public abstract int getContentItemCount();

    /**
     * 获取内容Item的ViewType
     * @param position
     * @return
     */
    public int getContentItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * 创建内容Holder
     * @param parent
     * @param viewType
     * @return
     */
    public abstract VH onCreateContentViewHolder(ViewGroup parent, int viewType);

    /**
     * 绑定内容Holder
     * @param viewHolder
     * @param position
     */
    public abstract void onBindContentViewHolder(VH viewHolder, int position);

    private class HeaderFooterViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mContentViewContainer;

        public HeaderFooterViewHolder(View itemView) {
            super(itemView);

            mContentViewContainer = (LinearLayout) itemView;
        }
    }
}
