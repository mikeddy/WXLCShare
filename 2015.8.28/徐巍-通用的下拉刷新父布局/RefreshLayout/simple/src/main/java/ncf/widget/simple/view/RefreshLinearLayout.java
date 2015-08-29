package ncf.widget.simple.view;

import android.content.Context;
import android.widget.LinearLayout;

import ncf.widget.refreshlayout.RefreshViewLayout;
import ncf.widget.simple.R;

/**
 * Created by XuWei on 15/8/21.
 */
public class RefreshLinearLayout extends LinearLayout implements RefreshViewLayout.IRefreshView {

    public RefreshLinearLayout(Context context) {
        super(context);

        setOrientation(VERTICAL);
        inflate(getContext(), R.layout.linearlayout, this);
    }

    @Override
    public boolean isReachTheTop() {
        return true;
    }

    @Override
    public boolean isReachTheBottom() {
        return false;
    }

    @Override
    public void setOnScrollToBottomListener(RefreshViewLayout.OnScrollToBottomListener listener) {

    }

    @Override
    public void scrollToTop(boolean isAnim) {

    }

    @Override
    public void scrollToBottom(boolean isAnim) {

    }
}
