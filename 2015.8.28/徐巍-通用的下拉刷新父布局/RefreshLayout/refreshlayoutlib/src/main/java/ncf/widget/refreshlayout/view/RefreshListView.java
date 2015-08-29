package ncf.widget.refreshlayout.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import ncf.widget.refreshlayout.RefreshViewLayout;
import ncf.widget.refreshlayout.adapter.BaseSectionAdapter;
import ncf.widget.refreshlayout.util.LogUtil;
import ncf.widget.refreshlayout.util.Utils;

/**
 * 实现了{@link RefreshViewLayout.IRefreshListView} 的接口，若需实现下拉刷新、上拉加载、滚动加载等功能则在
 * {@link RefreshViewLayout#setContentView(RefreshViewLayout.IRefreshView)}
 * 方法中传入本类实例即可。<br>
 * 若需悬停Section栏目功能则需在{@link #setAdapter(BaseSectionAdapter)}或
 * {@link #setAdapter(BaseSectionAdapter, boolean)}中传入{@link BaseSectionAdapter}
 * 即可。默认开启悬停功能，如不需要该功能则在{@link #setAdapter(BaseSectionAdapter, boolean)}控制即可。
 * 
 * @author xuwei3-pd
 * 
 */
public class RefreshListView extends ListView implements OnScrollListener,
		RefreshViewLayout.IRefreshListView {

	/** Tag */
	private final String TAG = "RefreshListView";

	/*
	 * IRefreshListView配置
	 */
	/** 滑动到底部的监听 */
	private RefreshViewLayout.OnScrollToBottomListener mScrollToBottomListener;
	/** 设定额外的OnScrollListener */
	private OnScrollListener mExtraOnScrollListener;
	/** 滑动监听队列 */
	private List<OnScrollListener> mScrollListeners = new ArrayList<>();

	/*
	 * PinnedHeader配置
	 */
	/** 是否开启悬浮section */
	private boolean mIsPinnedSectionOn;
	/** section adapter */
	private BaseSectionAdapter mSectionAdapter;
	/** 悬浮在顶部的SectionView */
	private View mPinnedSectionView;
	/** 是否显示PinedView */
	private boolean mIsPinnedSectionVisible;
	/** PinnedView可视区域 */
	private Rect mPinnedSectionVisibleRect;
	/** 是否为PinnedView区域内点击事件 */
	private boolean mIsPinnedSectionAction;
	/** section展现状态监听 */
	private PinnedSectionListener mSectionListener;
	/** 当前所选section */
	private int mCurrentSection;

	/**
	 * 构造
	 * 
	 * @param context
	 */
	public RefreshListView(Context context) {
		super(context);
		init();
	}

	/**
	 * 构造
	 * 
	 * @param context
	 * @param attrs
	 */
	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * 运行中改变悬浮显示状态 
	 * @param isEnable
	 */
	public void enablePinnedSection(boolean isEnable) {
		mIsPinnedSectionOn = isEnable;
	}

	/**
	 * 初始化下拉刷新和滑动加载的View
	 */
	@TargetApi(9)
	private void init() {
		setCacheColorHint(Color.TRANSPARENT);
		setDivider(null);
		setVerticalFadingEdgeEnabled(false);
		setOnScrollListener(this);
		if (Utils.hasHoneycomb()) {
			setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
	}

	/**
	 * 监听滑动事件，记录当前第一条可见列表项的索引值，捕捉页面滚动至最下方的事件
	 */
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mScrollToBottomListener != null
				&& (totalItemCount - getHeaderViewsCount() - getFooterViewsCount()) != 0
				&& firstVisibleItem + visibleItemCount == totalItemCount) {
			mScrollToBottomListener.onScrollToBottom();
		}

		if (mSectionListener != null && mSectionAdapter != null) {
			int section = mSectionAdapter.getSectionId(Math.min(
					mSectionAdapter.getCount(),
					Math.max(0, firstVisibleItem - getHeaderViewsCount())));
			if (section != mCurrentSection) {
				mSectionListener.onSectionChanged(section);
				mCurrentSection = section;
			}
		}

		if (mSectionAdapter != null && mSectionAdapter.getCount() > 0) {
			configurePinnedSectionView(firstVisibleItem - getHeaderViewsCount());
		}

		if (mExtraOnScrollListener != null) {
			mExtraOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

		for (OnScrollListener scrollListener : mScrollListeners)
			scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mExtraOnScrollListener != null) {
			mExtraOnScrollListener.onScrollStateChanged(view, scrollState);
		}

		for (OnScrollListener scrollListener : mScrollListeners)
			scrollListener.onScrollStateChanged(view, scrollState);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		super.setOnScrollListener(this);
		if (l != this) {
			mExtraOnScrollListener = l;
		}
	}

	/**
	 * 设置{@link BaseSectionAdapter},默认开启悬停SectionView
	 * 
	 * @param sectionAdapter
	 */
	public void setAdapter(BaseSectionAdapter sectionAdapter) {
		setAdapter(sectionAdapter, true);
	}

	/**
	 * 设置{@link BaseSectionAdapter}
	 * 
	 * @param sectionAdapter
	 * @param needPinned
	 *            是否需要悬停的SectionView
	 */
	public void setAdapter(BaseSectionAdapter sectionAdapter, boolean needPinned) {
		// 配置PinnedAdapter
		if (sectionAdapter != null
				&& sectionAdapter instanceof BaseSectionAdapter) {
			mSectionAdapter = sectionAdapter;
			mIsPinnedSectionOn = needPinned;
		}
		super.setAdapter(sectionAdapter);
	}

	@Override
	protected void layoutChildren() {
		try {
			super.layoutChildren();
		} catch (IllegalStateException e) {
			LogUtil.d(TAG, "This is not realy dangerous problem");
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void smoothScrollToPosition(int position) {
		if (getFirstVisiblePosition() <= position
				&& getLastVisiblePosition() >= position) {
			if (Utils.hasHoneycomb() && !isReachTheTop()) {
				smoothScrollToPositionFromTop(position, 0);
			} else {
				setSelection(position);
			}
		} else {
			super.smoothScrollToPosition(position);
		}
	}

	/*
	 * RefreshViewLayout部分接口配置
	 * 
	 * @see com.qihoopp.framework.ui.view.RefreshViewLayout.IRefreshView
	 */

	@Override
	public boolean isReachTheTop() {
		View firstChild = getChildAt(0);
		if (firstChild == null) {
			return true;
		}
		int scrollY = -firstChild.getTop() + getFirstVisiblePosition()
				* firstChild.getHeight();
		return getFirstVisiblePosition() == 0 && scrollY == 0;
	}

	@Override
	public boolean isReachTheBottom() {
		try {
			if (getLastVisiblePosition() == getAdapter().getCount() - 1) {
				return true;
			}
		} catch (Exception e) {
			LogUtil.w(TAG, e);
		}
		return false;
	}

	@Override
	public void setOnScrollToBottomListener(RefreshViewLayout.OnScrollToBottomListener listener) {
		mScrollToBottomListener = listener;
	}

	@Override
	public void addHeaderRefreshView(View headerView) {
		super.addHeaderView(headerView, null, false);
	}

	@Override
	public void addFooterRefreshView(View footerView) {
		super.addFooterView(footerView, null, false);
	}

	@Override
	public void scrollToTop(boolean isAnim) {
		if (isAnim && Utils.hasFroyo()) {
			smoothScrollToPosition(0);
		} else {
			setSelection(0);
		}
	}

	@Override
	public void scrollToBottom(boolean isAnim) {
		if (isAnim && Utils.hasFroyo()) {
			smoothScrollToPosition(getAdapter().getCount() - 1);
		} else {
			setSelection(getAdapter().getCount() + 1);
		}
	}

	/*
	 * PinnedHeader部分
	 */

	/**
	 * 配置对应位置的headerview
	 * 
	 * @param position
	 *            listview
	 */
	public void configurePinnedSectionView(int position) {
		if (!mIsPinnedSectionOn) {
			mIsPinnedSectionVisible = false;
			return;
		}

		try {
			if (mPinnedSectionView == null) {
				mPinnedSectionView = mSectionAdapter.getSectionView(null,
						Math.max(0, mSectionAdapter.getSectionId(position)));
				mPinnedSectionView.setLayoutParams(new LayoutParams(
						getMeasuredWidth(),
						RelativeLayout.LayoutParams.WRAP_CONTENT));
			} else {
				mPinnedSectionView = mSectionAdapter.getSectionView(
						mPinnedSectionView,
						Math.max(0, mSectionAdapter.getSectionId(position)));
			}
			Utils.measureView(mPinnedSectionView);

			PinnedSectionState state = getPinnedSectionState(position);

			if (position < 0) {
				state = PinnedSectionState.PINNED_HEADER_GONE;
			}

			switch (state) {
			case PINNED_HEADER_GONE:
				mIsPinnedSectionVisible = false;
				break;

			case PINNED_HEADER_VISIBLE:
				mPinnedSectionView.layout(0, 0, getMeasuredWidth(),
						mPinnedSectionView.getMeasuredHeight());
				mIsPinnedSectionVisible = true;
				mPinnedSectionVisibleRect = new Rect(0, 0, getMeasuredWidth(),
						mPinnedSectionView.getMeasuredHeight());
				break;

			case PINNED_HEADER_CLIP:
				View firstView = getChildAt(0);
				int bottom = firstView.getBottom();
				int headerHeight = mPinnedSectionView.getHeight();
				int y;
				if (bottom < headerHeight && headerHeight != 0) {
					y = (bottom - headerHeight);
				} else {
					y = 0;
				}
				if (mPinnedSectionView.getTop() != y) {
					mPinnedSectionView.layout(0, y, getMeasuredWidth(),
							mPinnedSectionView.getMeasuredHeight() + y);
				}
				mIsPinnedSectionVisible = true;
				mPinnedSectionVisibleRect = new Rect(0, y, getMeasuredWidth(),
						mPinnedSectionView.getMeasuredHeight() + y);
				break;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, "not big deal");
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// 按下事件进行捕捉记录，根据点击位置进行事件分发
		if (ev.getAction() == MotionEvent.ACTION_DOWN
				&& mIsPinnedSectionVisible
				&& mPinnedSectionVisibleRect.contains((int) ev.getX(),
						(int) ev.getY())) {
			mIsPinnedSectionAction = true;
		}

		if (mIsPinnedSectionAction
				&& ev.getAction() == MotionEvent.ACTION_UP
				&& mPinnedSectionVisibleRect.contains((int) ev.getX(),
						(int) ev.getY())) {
			mPinnedSectionView.performClick();
			mIsPinnedSectionAction = false;
			return true;
		}

		// 非Pinned区域内抬起
		if (mIsPinnedSectionAction
				&& (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL)) {
			mIsPinnedSectionAction = false;
		}

		if (mIsPinnedSectionAction) {
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}

	/**
	 * 跳转至指定section
	 * 
	 * @param position
	 */
	@SuppressLint("NewApi")
	public void setSelectSection(int position) {
		if (mSectionAdapter == null) {
			return;
		}

		int realPosition = 0;
		for (int i = 0; i < position; i++) {
			realPosition++;
			realPosition += mSectionAdapter.getCount(i);
		}

		if (Utils.hasFroyo()) {
			smoothScrollToPosition(realPosition + getHeaderViewsCount());
		} else {
			setSelection(realPosition + getHeaderViewsCount());
		}

	}

	/**
	 * 添加section展示状态监听
	 * 
	 * @param sectionListener
	 */
	public void setSectionListener(PinnedSectionListener sectionListener) {
		mSectionListener = sectionListener;
	}

	/**
	 * 获取当前悬浮Section显示状态
	 * 
	 * @param position
	 * @return
	 */
	private PinnedSectionState getPinnedSectionState(int position) {
		if (!mIsPinnedSectionOn) {
			return PinnedSectionState.PINNED_HEADER_GONE;
		}

		if (mSectionAdapter.getPositionIsSection(position + 1)) {
			if (position <= 0) {
				return PinnedSectionState.PINNED_HEADER_GONE;
			}
			return PinnedSectionState.PINNED_HEADER_CLIP;
		} else {
			return PinnedSectionState.PINNED_HEADER_VISIBLE;
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mIsPinnedSectionVisible) {
			drawChild(canvas, mPinnedSectionView, getDrawingTime());
		}
	}

	/**
	 * PinnedSection状态
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public enum PinnedSectionState {
		/** 不显示Section，或为下拉刷新状态时 */
		PINNED_HEADER_GONE,
		/** 悬浮在顶部显示的Section */
		PINNED_HEADER_VISIBLE,
		/** 被下一个Section推挤，或者下拉即将显示上一个Section */
		PINNED_HEADER_CLIP
	}

	/**
	 * section展现状态监听
	 * 
	 * @author Calvin
	 * 
	 */
	public interface PinnedSectionListener {

		/**
		 * 通知当前section发生变化
		 * 
		 * @param section
		 */
		public void onSectionChanged(int section);
	}

}
