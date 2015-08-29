package ncf.widget.simple.view;

import android.content.Context;
import android.widget.TextView;

import ncf.widget.refreshlayout.RefreshViewLayout;

/**
 * Created by XuWei on 15/8/21.
 */
public class RefreshTextView extends TextView implements RefreshViewLayout.IRefreshView{

    public RefreshTextView(Context context) {
        super(context);

        setText("这是一只TextView");
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
