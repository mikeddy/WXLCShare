package ncf.widget.refreshlayout.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ncf.widget.refreshlayout.RefreshViewLayout;
import ncf.widget.refreshlayout.adapter.RecyclerHeaderFooterAdapter;
import ncf.widget.refreshlayout.util.RecyclerViewPositionHelper;
import ncf.widget.refreshlayout.util.Utils;

/**
 * Created by XuWei on 15/8/22.
 */
public class RefreshRecyclerView extends RecyclerView implements RefreshViewLayout.IRefreshListView{

    /** 当前位置计算器 */
    private RecyclerViewPositionHelper mPositionHelper;
    /** 滑动到页面底部监听 */
    private RefreshViewLayout.OnScrollToBottomListener mScrollToBottomListener;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public RefreshRecyclerView(Context context) {
        super(context);

        if (Utils.hasGingerbread()) {
            setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        mPositionHelper = RecyclerViewPositionHelper.createHelper(this);
        mPositionHelper.setIsIgnoreInvisibleItem(true);
    }

    @Override
    public void addHeaderRefreshView(View headerView) {
        if (getAdapter() instanceof RecyclerHeaderFooterAdapter) {
            headerView.setMinimumHeight(1);
            ((RecyclerHeaderFooterAdapter) getAdapter()).addHeaderView(headerView);
        }
    }

    @Override
    public void addFooterRefreshView(View footerView) {
        if (getAdapter() instanceof RecyclerHeaderFooterAdapter) {
            footerView.setMinimumHeight(1);
            ((RecyclerHeaderFooterAdapter) getAdapter()).addFooterView(footerView);
        }
    }

    @Override
    public boolean isReachTheTop() {
        return mPositionHelper.findFirstCompletelyVisibleItemPosition() == 0;
    }

    @Override
    public boolean isReachTheBottom() {
        return mPositionHelper.findLastCompletelyVisibleItemPosition() == getAdapter().getItemCount() - 1;
    }

    @Override
    public void setOnScrollToBottomListener(RefreshViewLayout.OnScrollToBottomListener listener) {
        mScrollToBottomListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (getAdapter() != null && mScrollToBottomListener != null) {
            if (getAdapter().getItemCount() - mPositionHelper.findLastVisibleItemPosition() < 5) {
                mScrollToBottomListener.onScrollToBottom();
            }
        }
    }

    @Override
    public void scrollToTop(boolean isAnim) {
        scrollToPosition(0);
    }

    @Override
    public void scrollToBottom(boolean isAnim) {
        scrollToPosition(getAdapter().getItemCount() - 1);
    }
}
