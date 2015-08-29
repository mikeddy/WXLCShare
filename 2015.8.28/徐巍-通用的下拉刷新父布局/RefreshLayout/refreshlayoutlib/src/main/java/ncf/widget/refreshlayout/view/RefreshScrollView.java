package ncf.widget.refreshlayout.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import ncf.widget.refreshlayout.RefreshViewLayout;
import ncf.widget.refreshlayout.util.LogUtil;
import ncf.widget.refreshlayout.util.Utils;


/**
 * 实现了{@link RefreshViewLayout.IRefreshScrollView} 的接口，若需实现下拉刷新、上拉加载、滚动加载等功能则在
 * {@link RefreshViewLayout#setContentView(RefreshViewLayout.IRefreshView)}
 * 方法中传入本类实例即可
 * 
 * @author xuwei3-pd
 * 
 */
public class RefreshScrollView extends ScrollView implements RefreshViewLayout.IRefreshScrollView {

	/** Tag */
	protected final String TAG = "RefreshScrollView";

	/** 滚动监听回调 */
	private RefreshViewLayout.OnScrollToBottomListener mOnScrollToBottomListener;

	/**
	 * 构造
	 * 
	 * @param context
	 */
	public RefreshScrollView(Context context) {
		super(context);
		init();
	}

	/**
	 * 构造
	 * 
	 * @param context
	 * @param attrs
	 */
	public RefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化
	 */
	@TargetApi(9)
	private void init() {
		if (Utils.hasGingerbread()) {
			setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (mOnScrollToBottomListener != null) {
			try {
				// 默认为小余50dip的高度条件下触发事件
				if (getChildAt(0).getMeasuredHeight() - getScrollY()
						- getMeasuredHeight() < Utils.getDensity(getContext()) * 50) {
					mOnScrollToBottomListener.onScrollToBottom();
				}
			} catch (Exception e) {
				LogUtil.w(TAG, "caught unknow exception");
				LogUtil.w(TAG, e);
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			return super.onTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
			return super.dispatchTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * RefreshViewLayout部分接口配置
	 * 
	 * @see com.qihoopp.framework.ui.view.RefreshViewLayout.IRefreshView
	 */

	@Override
	public boolean isReachTheTop() {
		return getScrollY() == 0;
	}

	@Override
	public boolean isReachTheBottom() {
		try {
			return getScrollY() + getMeasuredHeight() == getChildAt(0)
					.getMeasuredHeight();
		} catch (Exception e) {
			LogUtil.w(TAG, "caught unknow exception");
			LogUtil.w(TAG, e);
		}
		return false;
	}

	@Override
	public void setOnScrollToBottomListener(RefreshViewLayout.OnScrollToBottomListener listener) {
		mOnScrollToBottomListener = listener;
	}

	@Override
	public void scrollToTop(boolean isAnim) {
		scrollTo(0, 1);
		scrollTo(0, 0);
	}

	@Override
	public void scrollToBottom(boolean isAnim) {
		scrollTo(0, getChildAt(0).getMeasuredHeight());
	}

}
