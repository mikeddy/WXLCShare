package ncf.widget.simple.view;

import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ncf.widget.refreshlayout.RefreshViewLayout;

/**
 * Created by XuWei on 15/8/21.
 */
public class RefreshWebView extends WebView implements RefreshViewLayout.IRefreshView{

    public RefreshWebView(Context context) {
        super(context);

        setWebViewClient(new WebViewClient());
        setWebChromeClient(new WebChromeClient());

        getSettings().setJavaScriptEnabled(true);

        loadUrl("http://www.baidu.com");
    }

    @Override
    public boolean isReachTheTop() {
        return getScrollY() == 0;
    }

    @Override
    public boolean isReachTheBottom() {
        return getContentHeight() * getScale() == (getHeight() + getScrollY());
    }

    @Override
    public void setOnScrollToBottomListener(RefreshViewLayout.OnScrollToBottomListener listener) {

    }

    @Override
    public void scrollToTop(boolean isAnim) {
        scrollTo(0, 0);
    }

    @Override
    public void scrollToBottom(boolean isAnim) {
    }

}
