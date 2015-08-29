package ncf.widget.refreshlayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;

import ncf.widget.refreshlayout.util.IAnimationRunner;
import ncf.widget.refreshlayout.util.LogUtil;
import ncf.widget.refreshlayout.util.NoProGuard;
import ncf.widget.refreshlayout.util.OnSingleClickListener;
import ncf.widget.refreshlayout.util.Utils;
import ncf.widget.refreshlayout.view.RefreshListView;
import ncf.widget.refreshlayout.view.RefreshScrollView;

/**
 * 封装了下拉刷新、上拉加载、滚动加载实现的ViewGroup。<br>
 * 通过调用 {@link #setPullDownRefreshListener(OnPullDownRefreshListener)}、
 * {@link #setPullUpRefreshListener(OnPullUpRefreshListener)}、
 * {@link #setScrollRefreshListener(OnScrollRefreshListener)}注册对相应加载功能的回调。<br>
 * 可通过xml配置和动态添加的方式填充具体内容View，例如{@link RefreshListView}或
 * {@link RefreshScrollView}：<br>
 * 
 * 动态添加
 * 
 * <pre>
 * RefreshViewLayout refreshViewLayout = (RefreshViewLayout) findViewById(id);
 * RefreshListView listView = new RefreshListView(getContext());
 * refreshViewLayout.setContentView(listView);
 * </pre>
 * 
 * 动态获取
 * 
 * <pre>
 * RefreshViewLayout refreshViewLayout = (RefreshViewLayout) findViewById(id);
 * RefreshListView listView = (RefreshListView) refreshViewLayout
 * 		.getContentView(RefreshListView.class);
 * </pre>
 * 
 * XML配置
 * 
 * <pre>
 * RefreshViewLayout refreshViewLayout = (RefreshViewLayout) findViewById(id);
 * RefreshListView listView = (RefreshListView) refreshViewLayout.getContentView();
 * <com.qihoopp.framework.ui.view.RefreshViewLayout
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" >
 * 
 *         <com.qihoopp.framework.ui.view.RefreshListView
 *             android:id="@+id/list"
 *             android:layout_width="match_parent"
 *             android:layout_height="match_parent" >
 *         </com.qihoopp.framework.ui.view.RefreshListView>
 *     </com.qihoopp.framework.ui.view.RefreshViewLayout>
 * </pre>
 * 
 * 
 * @author xuwei3-pd
 * 
 */
public class RefreshViewLayout extends RelativeLayout {

	/** Tag */
	private final String TAG = "RefreshViewLayout";
	/** content view id */
	private static final int ID_CONTENT_VIEW = 10086;
	/** 判定拉取动作的最小位移 */
	private static final int DETECT_MIN_HEIGHT = 10;

	/** 是否完成初始化行为 */
	private boolean mIsInit;
	/** 填充的内容控件 */
	private IRefreshView mContentView;
	/** 是否需要拦截当前事件 */
	private boolean mShouldInterceptEvent;

	/*
	 * 下拉刷新配置
	 */
	/** 是否允许下拉刷新 */
	private boolean mIsPullDownRefreshEnable;
	/** 下拉刷新View */
	private BasePullRefreshView mPullDownView;
	/** 当前下拉刷新状态 */
	private PullRefreshState mPullDownRefreshState = PullRefreshState.DONE;
	/** 下拉刷新事件触发监听 */
	private OnPullDownRefreshListener mPullDownRefreshListener;
	/** 是否已经捕获到下拉刷新事件 */
	private boolean mIsPullDownEventDetected;
	/** 下拉刷新事件触摸起点记录 */
	private int mPullDownEventStartY;

	/*
	 * 上拉加载配置
	 */
	/** 是否允许上拉加载 */
	private boolean mIsPullUpRefreshEnable;
	/** 上拉加载View */
	private BasePullRefreshView mPullUpView;
	/** 当前上拉加载状态 */
	private PullRefreshState mPullUpRefreshState = PullRefreshState.DONE;
	/** 上拉加载事件触发监听 */
	private OnPullUpRefreshListener mPullUpRefreshListener;
	/** 是否已经捕获到上拉加载事件 */
	private boolean mIsPullUpEventDetected;
	/** 上拉加载事件触摸起点记录 */
	private int mPullUpEventStartY;

	/*
	 * 滚动加载配置
	 */
	/** 是否允许滑动加载 */
	private boolean mIsScrollRefreshEnable;
	/** 滚动加载View */
	private BaseScrollRefreshView mScrollRefreshView;
	/** 当前滚动加载状态 */
	private ScrollRefreshState mScrollRefreshState = ScrollRefreshState.DONE;
	/** 滚动加载事件触发监听 */
	private OnScrollRefreshListener mScrollRefreshListener;

	/**
	 * 构造
	 * 
	 * @param context
	 */
	public RefreshViewLayout(Context context) {
		super(context);
	}

	/**
	 * 构造
	 * 
	 * @param context
	 */
	public RefreshViewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 添加下拉刷新事件监听，默认UI实现，若需自定义UI效果请调用
	 * {@link #setPullDownRefreshListener(OnPullDownRefreshListener, BasePullRefreshView)}
	 * 
	 * @param pullDownRefreshListener
	 */
	public void setPullDownRefreshListener(
			OnPullDownRefreshListener pullDownRefreshListener) {
		setPullDownRefreshListener(pullDownRefreshListener,
				new DefaultPullDownRefreshView(getContext()));
	}

	/**
	 * 添加下拉刷新事件监听，自定义UI效果。
	 * 
	 * @param pullDownRefreshListener
	 * @param pullDownRefreshView
	 */
	public void setPullDownRefreshListener(
			OnPullDownRefreshListener pullDownRefreshListener,
			BasePullRefreshView pullDownRefreshView) {
		mPullDownRefreshListener = pullDownRefreshListener;
		mIsPullDownRefreshEnable = true;
		mPullDownView = pullDownRefreshView;
		mPullDownRefreshState = PullRefreshState.DONE;
	}

	/**
	 * 添加上拉加载事件监听，默认UI实现，若需自定义UI效果请调用
	 * {@link #setPullUpRefreshListener(OnPullUpRefreshListener, BasePullRefreshView)}
	 * 
	 * @param pullUpRefreshListener
	 */
	public void setPullUpRefreshListener(
			OnPullUpRefreshListener pullUpRefreshListener) {
		setPullUpRefreshListener(pullUpRefreshListener,
				new DefaultPullUpRefreshView(getContext()));
	}

	/**
	 * 添加上拉加载事件监听，自定义UI效果。
	 * 
	 * @param pullUpRefreshListener
	 * @param pullUpRefreshView
	 */
	public void setPullUpRefreshListener(
			OnPullUpRefreshListener pullUpRefreshListener,
			BasePullRefreshView pullUpRefreshView) {
		mPullUpRefreshListener = pullUpRefreshListener;
		mIsPullUpRefreshEnable = true;
		mPullUpView = pullUpRefreshView;
		mPullUpRefreshState = PullRefreshState.DONE;

		// 上拉加载和滚动加载唯一存在
		mIsScrollRefreshEnable = false;
	}

	/**
	 * 添加滑动加载事件监听，默认UI实现，若需自定义UI效果请调用
	 * {@link #setScrollRefreshListener(OnScrollRefreshListener, BaseScrollRefreshView)}
	 * 
	 * @param scrollRefreshListener
	 */
	public void setScrollRefreshListener(
			OnScrollRefreshListener scrollRefreshListener) {
		setScrollRefreshListener(scrollRefreshListener,
				new DefaultScrollRefreshView(getContext()));
	}

	/**
	 * 添加滑动加载事件监听，自定义UI效果。
	 * 
	 * @param scrollRefreshListener
	 * @param scrollRefreshView
	 */
	public void setScrollRefreshListener(
			OnScrollRefreshListener scrollRefreshListener,
			BaseScrollRefreshView scrollRefreshView) {
		mScrollRefreshListener = scrollRefreshListener;
		mIsScrollRefreshEnable = true;
		mScrollRefreshView = scrollRefreshView;
		mScrollRefreshState = ScrollRefreshState.DONE;

		// 上拉加载和滚动加载唯一存在
		mIsPullUpRefreshEnable = false;
	}

	/**
	 * 设置实际内容的View，注意调用顺序在
	 * {@link #setPullDownRefreshListener(OnPullDownRefreshListener)}、
	 * {@link #setPullUpRefreshListener(OnPullUpRefreshListener)}、
	 * {@link #setScrollRefreshListener(OnScrollRefreshListener)}之后。
	 * 
	 * @param contentView
	 */
	public void setContentView(IRefreshView contentView) {
		mContentView = contentView;
		init();
	}

	/**
	 * 获取通过layout xml构造的contentview，注意调用顺序在
	 * {@link #setPullDownRefreshListener(OnPullDownRefreshListener)}、
	 * {@link #setPullUpRefreshListener(OnPullUpRefreshListener)}、
	 * {@link #setScrollRefreshListener(OnScrollRefreshListener)}之后。
	 * 
	 * @return
	 */
	public IRefreshView getContentView() {
		if (mContentView != null) {
			if (!mIsInit) {
				init();
			}
			return mContentView;
		} else {
			return null;
		}
	}

	/**
	 * 获取通过类名构造的contentview，注意调用顺序在
	 * {@link #setPullDownRefreshListener(OnPullDownRefreshListener)}、
	 * {@link #setPullUpRefreshListener(OnPullUpRefreshListener)}、
	 * {@link #setScrollRefreshListener(OnScrollRefreshListener)}之后。
	 * 
	 * @param clazz
	 * @return
	 */
	public IRefreshView getContentView(Class<? extends IRefreshView> clazz) {
		try {
			Constructor<? extends IRefreshView> constructor = clazz
					.getConstructor(Context.class);
			IRefreshView constructView = constructor.newInstance(getContext());
			mContentView = constructView;
			init();
			return constructView;
		} catch (Exception e) {
			LogUtil.e(TAG, "create content refresh view failed");
			LogUtil.w(TAG, e);
			return null;
		}

	}

	@Override
	protected void onFinishInflate() {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child instanceof IRefreshView) {
				mContentView = (IRefreshView) child;
			}
		}
	}

	/**
	 * 进行一次下拉刷新
	 */
	public void forcePullDownRefresh() {
		if (getCurrentState() != RefreshViewState.NORMAL) {
			return;
		}

		mContentView.scrollToTop(true);
		onPullDownStateChanged(PullRefreshState.REFRESHING);
	}

	/**
	 * 进行一次上拉加载
	 */
	public void forcePullUpRefresh() {
		if (getCurrentState() != RefreshViewState.NORMAL) {
			return;
		}

		mContentView.scrollToBottom(true);
		onPullUpStateChanged(PullRefreshState.REFRESHING);
	}

	/**
	 * 通知下拉刷新完成，默认不滚到顶部
	 */
	public void onPullDownRefreshComplete() {
		onPullDownRefreshComplete(false);
	}

	/**
	 * 通知下拉刷新完成，是否滚到顶部
	 */
	public void onPullDownRefreshComplete(boolean isScrollToTop) {
		if (!mIsPullDownRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullDownStateChanged(PullRefreshState.DONE);
			}
		};
		if (mPullDownView.hasSuffixTransitionAnim()) {
			mPullDownView.triggerSuffixTransition(action);
		} else {
			action.run();
		}

		mPullDownView.notifyUpdateTime(System.currentTimeMillis());
		if (isScrollToTop) {
			mContentView.scrollToTop(true);
		}

		// 复位上拉以及滚动加载
		if (mIsScrollRefreshEnable) {
			onScrollRefreshStateChanged(ScrollRefreshState.DONE);
		}
		if (mIsPullUpRefreshEnable) {
			onPullUpStateChanged(PullRefreshState.DONE);
		}
	}

	/**
	 * 通知下拉刷新失败
	 */
	public void onPullDownRefreshFailed() {
		if (!mIsPullDownRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullDownStateChanged(PullRefreshState.DONE);
			}
		};
		if (mPullDownView.hasSuffixTransitionAnim()) {
			mPullDownView.triggerSuffixTransition(action);
		} else {
			action.run();
		}
	}

	/**
	 * 通知下拉刷新无后续可加载项
	 */
	public void onPullDownRefreshNoMore() {
		if (!mIsPullDownRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullDownStateChanged(PullRefreshState.NOMORE);
			}
		};
		if (mPullDownView.hasSuffixTransitionAnim()) {
			mPullDownView.triggerSuffixTransition(action);
		} else {
			action.run();
		}
	}

	/**
	 * 通知上拉加载完成
	 */
	public void onPullUpRefreshComplete() {
		if (!mIsPullUpRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullUpStateChanged(PullRefreshState.DONE);
			}
		};
		if (mPullUpView.hasSuffixTransitionAnim()) {
			mPullUpView.triggerSuffixTransition(action);
		} else {
			action.run();
		}

		mPullUpView.notifyUpdateTime(System.currentTimeMillis());
	}

	/**
	 * 通知上拉加载失败
	 */
	public void onPullUpRefreshFailed() {
		if (!mIsPullUpRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullUpStateChanged(PullRefreshState.DONE);
			}
		};
		if (mPullUpView.hasSuffixTransitionAnim()) {
			mPullUpView.triggerSuffixTransition(action);
		} else {
			action.run();
		}
	}

	/**
	 * 通知上拉加载无后续可加载项
	 */
	public void onPullUpRefreshNoMore() {
		if (!mIsPullUpRefreshEnable) {
			return;
		}

		Runnable action = new Runnable() {
			@Override
			public void run() {
				onPullUpStateChanged(PullRefreshState.NOMORE);
			}
		};
		if (mPullUpView.hasSuffixTransitionAnim()) {
			mPullUpView.triggerSuffixTransition(action);
		} else {
			action.run();
		}
	}

	/**
	 * 通知滚动加载完成
	 */
	public void onScrollRefreshComplete() {
		onScrollRefreshStateChanged(ScrollRefreshState.DONE);
		mScrollRefreshView.notifyUpdateTime(System.currentTimeMillis());
	}

	/**
	 * 通知滚动加载失败
	 */
	public void onScrollRefreshFail() {
		onScrollRefreshStateChanged(ScrollRefreshState.FAIL);
	}

	/**
	 * 通知滑动加载无后续可加载项
	 */
	public void onScrollRefreshNoMore() {
		onScrollRefreshStateChanged(ScrollRefreshState.NOMORE);
	}

	/**
	 * 获取当前RefreshView状态
	 * 
	 * @return
	 */
	public RefreshViewState getCurrentState() {
		if (mPullDownRefreshState == PullRefreshState.REFRESHING) {
			return RefreshViewState.WAITING_PULL_DOWN_REFRESH_RESULT;
		} else if (mPullUpRefreshState == PullRefreshState.REFRESHING) {
			return RefreshViewState.WAITING_PULL_UP_REFRESH_RESULT;
		} else if (mScrollRefreshState == ScrollRefreshState.REFRESHING) {
			return RefreshViewState.WAITING_SCROLLREFRESH_RESULT;
		}
		return RefreshViewState.NORMAL;
	}

	/**
	 * 初始化
	 */
	private void init() {
		mIsInit = true;
		removeAllViews();

		// 若传入对象为ListView，则采取List添加Header、Footer的方法加入下拉上拉的View
		if (mContentView instanceof IRefreshListView) {
			if (mIsPullDownRefreshEnable) {
				((IRefreshListView) mContentView)
						.addHeaderRefreshView(mPullDownView.getContentView());
			}
			if (mIsPullUpRefreshEnable) {
				((IRefreshListView) mContentView)
						.addFooterRefreshView(mPullUpView.getContentView());
			}
			if (mIsScrollRefreshEnable) {
				((IRefreshListView) mContentView)
						.addFooterRefreshView(mScrollRefreshView
								.getContentView());
				mContentView.setOnScrollToBottomListener(mOnScrollListener);
			}

			addView((View) mContentView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		// 若传入对象为ScrollView，则需要嵌套一个容器来承载Header和Footer
		else if (mContentView instanceof IRefreshScrollView) {
			LinearLayout contentContainer = new LinearLayout(getContext());
			contentContainer.setOrientation(LinearLayout.VERTICAL);
			View userContentView = (View) mContentView;

			mContentView = new RefreshScrollView(getContext());
			((ViewGroup) mContentView).addView(contentContainer,
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));

			if (mIsPullDownRefreshEnable) {
				contentContainer.addView(mPullDownView.getContentView(),
						new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT));
			}

			contentContainer.addView(userContentView,
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT));

			if (mIsPullUpRefreshEnable) {
				contentContainer.addView(mPullUpView.getContentView(),
						new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			if (mIsScrollRefreshEnable
					&& mContentView instanceof IRefreshScrollView) {
				contentContainer.addView(mScrollRefreshView.getContentView(),
						new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT));
				mContentView.setOnScrollToBottomListener(mOnScrollListener);
			}

			addView((View) mContentView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		// 若为一般性视图，如LinearLayout
		else if (mContentView instanceof IRefreshView) {
			View contentView = (View) mContentView;
			contentView.setClickable(true);

			contentView.setId(ID_CONTENT_VIEW);
			contentView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));

			if (mIsPullDownRefreshEnable) {
				View header = mPullDownView.getContentView();
				header.setId(ID_CONTENT_VIEW - 1);
				addView(header, new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT));

				LayoutParams contentParams = (LayoutParams) contentView
						.getLayoutParams();
				contentParams
						.addRule(RelativeLayout.BELOW, ID_CONTENT_VIEW - 1);
			}

			if (mIsPullUpRefreshEnable) {
				View footer = mPullUpView.getContentView();
				footer.setId(ID_CONTENT_VIEW + 1);
				LayoutParams footerParams = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				footerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				addView(footer, footerParams);

				LayoutParams contentParams = (LayoutParams) contentView
						.getLayoutParams();
				contentParams
						.addRule(RelativeLayout.ABOVE, ID_CONTENT_VIEW + 1);
			}

			if (mIsScrollRefreshEnable) {
				throw new UnsupportedOperationException(
						"IRefreshView not support the ScrollRefresh");
			}
		}

		if (mIsPullDownRefreshEnable) {
			onPullDownStateChanged(PullRefreshState.DONE);
		}
		if (mIsPullUpRefreshEnable) {
			onPullUpStateChanged(PullRefreshState.DONE);
		}
		if (mIsScrollRefreshEnable) {
			onScrollRefreshStateChanged(ScrollRefreshState.DONE);
		}
	}

	/**
	 * 通知下拉刷新
	 */
	private void onPullDownRefresh() {
		if (mIsPullDownRefreshEnable && mPullDownRefreshListener != null) {
			mPullDownRefreshListener.onPullDownRefresh();
		}
	}

	/**
	 * 通知上拉加载
	 */
	private void onPullUpRefresh() {
		if (mIsPullUpRefreshEnable && mPullUpRefreshListener != null) {
			mPullUpRefreshListener.onPullUpRefresh();
		}
	}

	/**
	 * 通知滑动加载
	 */
	private void onScrollRefresh() {
		if (mIsScrollRefreshEnable && mScrollRefreshListener != null) {
			mScrollRefreshListener.onScrollRefresh();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
			mShouldInterceptEvent = false;

		boolean pullDownVisibleBefore = mPullDownRefreshState != PullRefreshState.DONE;
		boolean pullUpVisibleBefore = mPullUpRefreshState != PullRefreshState.DONE;

		onRefreshViewTouchEvent(ev);

		boolean pullDownVisibleAfter = mPullDownRefreshState != PullRefreshState.DONE;
		boolean pullUpVisibleAfter = mPullUpRefreshState != PullRefreshState.DONE;

		//在滑动过程中，如果状态在两个边界进行切换时，辅助mock点击事件使滑动行为可连续进行
		if (ev.getAction() == MotionEvent.ACTION_MOVE && (pullDownVisibleBefore != pullDownVisibleAfter || pullUpVisibleBefore != pullUpVisibleAfter)) {
			mShouldInterceptEvent = !mShouldInterceptEvent;

			if (!mShouldInterceptEvent) {
				MotionEvent mockEv = MotionEvent.obtain(ev);
				mockEv.setAction(MotionEvent.ACTION_DOWN);
				super.dispatchTouchEvent(mockEv);
			} else {
				MotionEvent mockEv = MotionEvent.obtain(ev);
				mockEv.setAction(MotionEvent.ACTION_CANCEL);
				super.dispatchTouchEvent(mockEv);
			}
		}

		try {
			return super.dispatchTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mShouldInterceptEvent;
	}

	/**
	 * 配置刷新的触摸事件
	 * 
	 * @param ev
	 */
	private void onRefreshViewTouchEvent(MotionEvent ev) {
		// 下拉刷新事件
		if (mIsPullDownRefreshEnable
				&& (mIsPullDownEventDetected || mContentView.isReachTheTop())) {
			mPullDownView.getContentView().clearAnimation();
			onPullDownTouchEvent(ev);
		}

		// 上拉加载事件
		if (mIsPullUpRefreshEnable
				&& (mIsPullUpEventDetected || mContentView.isReachTheBottom())) {
			mPullUpView.getContentView().clearAnimation();
			onPullUpTouchEvent(ev);
		}
	}

	/**
	 * 拦截当前滑动事件，取消掉子View的事件拦截行为
	 * 
	 * @param view
	 */
	// private void cancelChildDispatch(View view) {
	// if (view instanceof ViewGroup) {
	// for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	// View childView = ((ViewGroup) view).getChildAt(i);
	// cancelChildDispatch(childView);
	// }
	// }
	//
	// if (view != mContentView) {
	// MotionEvent cancelEvent = MotionEvent.obtain(0, 0,
	// MotionEvent.ACTION_CANCEL & MotionEvent.ACTION_MASK, 0, 0,
	// 0);
	// view.dispatchTouchEvent(cancelEvent);
	// cancelEvent.recycle();
	// }
	//
	// }

	/**
	 * 下拉刷新事件分发
	 * 
	 * @param event
	 */
	private void onPullDownTouchEvent(MotionEvent event) {
		// 记录初始状态
		if (!mIsPullDownEventDetected) {
			mIsPullDownEventDetected = true;
			mPullDownEventStartY = (int) event.getY();
			// 恢复之前存在状态产生的位移
			mPullDownEventStartY -= (int) ((mPullDownView.getClipHeight() - mPullDownView
					.getBaseClipHeight()) * mPullDownView.getPullRatio());
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mPullDownRefreshState != PullRefreshState.REFRESHING) {
				if (mPullDownRefreshState == PullRefreshState.RELEASE_TO_REFRESH) {
					onPullDownStateChanged(PullRefreshState.REFRESHING);
				} else if (mPullDownRefreshState == PullRefreshState.NOMORE) {
					onPullDownStateChanged(PullRefreshState.NOMORE);
				} else if (mPullDownRefreshState != PullRefreshState.DONE) {
					onPullDownStateChanged(PullRefreshState.DONE);
				}
			} else {
				// 不变更状态，仅执行动画
				runPullRefreshViewAnim(mPullDownView,
						mPullDownView.getTriggerHeight());
			}

			mIsPullDownEventDetected = false;
			mPullDownEventStartY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			clearAnimation();
			int distanceY = (int) ((event.getY() - mPullDownEventStartY) / mPullDownView
					.getPullRatio());

			// 若为拖拽事件，则取消掉其他子View的事件拦截
			// if (distanceY > DETECT_MIN_HEIGHT *
			// Utils.getDensity(getContext())) {
			// cancelChildDispatch((View) mContentView);
			// }

			// 矫正最大拉取高度
			if (distanceY > mPullDownView.getMaxPullHeight()) {
				mPullDownEventStartY += distanceY
						- mPullDownView.getMaxPullHeight();
				distanceY = mPullDownView.getMaxPullHeight();
			}

			// 矫正基础可视区域
			if (mPullDownView.getBaseClipHeight() != 0) {
				distanceY += mPullDownView.getBaseClipHeight();
			}

			// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

			// 可以松手去刷新了
			if (mPullDownRefreshState == PullRefreshState.RELEASE_TO_REFRESH) {
				mContentView.scrollToTop(false);

				// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
				if (distanceY < mPullDownView.getTriggerHeight()
						&& distanceY > mPullDownView.getBaseClipHeight()) {
					onPullDownStateChanged(PullRefreshState.PULL_TO_REFRESH);
				}
				// 一下子推到顶了
				else if (distanceY <= mPullDownView.getBaseClipHeight()) {
					onPullDownStateChanged(PullRefreshState.DONE);
				}
				// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
				else {
					// 不用进行特别的操作，只用更新paddingTop的值就行了
				}
			}
			// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
			if (mPullDownRefreshState == PullRefreshState.PULL_TO_REFRESH) {
				mContentView.scrollToTop(false);

				// 下拉到可以进入RELEASE_TO_REFRESH的状态，并增加额外条件判断canAccessPullToRefresh()
				if (distanceY >= mPullDownView.getTriggerHeight()
						&& isEnableLoadAndRefresh()) {
					onPullDownStateChanged(PullRefreshState.RELEASE_TO_REFRESH);
				}
				// 上推到顶了
				else if (distanceY <= mPullDownView.getBaseClipHeight()) {
					onPullDownStateChanged(PullRefreshState.DONE);
					mPullDownView.onPull(mPullDownRefreshState,
							mPullDownView.getBaseClipHeight());
					mIsPullDownEventDetected = false;
					break;
				}
			}

			// done状态下
			if (mPullDownRefreshState == PullRefreshState.DONE) {
				if (distanceY > mPullDownView.getBaseClipHeight()
						+ DETECT_MIN_HEIGHT * Utils.getDensity(getContext())) {
					onPullDownStateChanged(PullRefreshState.PULL_TO_REFRESH);
				}
			}

			// nomore状态下
			if (mPullDownRefreshState == PullRefreshState.NOMORE
					&& distanceY > mPullDownView.getBaseClipHeight()) {
				mContentView.scrollToTop(false);
			}

			// 更新headView的paddingTop
			if (mPullDownRefreshState == PullRefreshState.RELEASE_TO_REFRESH
					|| mPullDownRefreshState == PullRefreshState.PULL_TO_REFRESH
					|| mPullDownRefreshState == PullRefreshState.NOMORE) {
				mPullDownView.setClipHeight(distanceY);
				mPullDownView.onPull(mPullDownRefreshState,
						mPullDownView.getProgress());
			} else if (mPullDownRefreshState == PullRefreshState.REFRESHING) {
				mPullDownView.setClipHeight(Math.max(distanceY,
						mPullDownView.getTriggerHeight()));
				mPullDownView.onPull(mPullDownRefreshState,
						mPullDownView.getProgress());

				if (distanceY < mPullDownView.getTriggerHeight()) {
					mIsPullDownEventDetected = false;
				} else {
					mContentView.scrollToTop(false);
				}
			}
			break;
		}
	}

	/**
	 * 上拉加载事件分发
	 * 
	 * @param event
	 */
	private void onPullUpTouchEvent(MotionEvent event) {
		// 记录初始状态
		if (!mIsPullUpEventDetected) {
			mIsPullUpEventDetected = true;
			mPullUpEventStartY = (int) event.getY();
			// 恢复之前存在状态产生的位移
			mPullUpEventStartY += (int) ((mPullUpView.getClipHeight() - mPullUpView
					.getBaseClipHeight()) * mPullUpView.getPullRatio());
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mPullUpRefreshState != PullRefreshState.REFRESHING) {
				if (mPullUpRefreshState == PullRefreshState.RELEASE_TO_REFRESH) {
					onPullUpStateChanged(PullRefreshState.REFRESHING);
				} else if (mPullUpRefreshState == PullRefreshState.NOMORE) {
					onPullUpStateChanged(PullRefreshState.NOMORE);
				} else if (mPullUpRefreshState != PullRefreshState.DONE) {
					onPullUpStateChanged(PullRefreshState.DONE);
				}
			} else {
				// 不变更状态，仅执行动画
				runPullRefreshViewAnim(mPullUpView,
						mPullUpView.getTriggerHeight());
			}
			mIsPullUpEventDetected = false;
			mPullUpEventStartY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			clearAnimation();

			int distanceY = -(int) ((event.getY() - mPullUpEventStartY) / mPullUpView
					.getPullRatio());

			// 若为拖拽事件，则取消掉其他子View的事件拦截
			// if (distanceY > DETECT_MIN_HEIGHT *
			// Utils.getDensity(getContext())) {
			// cancelChildDispatch((View) mContentView);
			// }

			// 矫正最大拉取高度
			if (distanceY > mPullUpView.getMaxPullHeight()) {
				mPullUpEventStartY += distanceY
						- mPullUpView.getMaxPullHeight();
				distanceY = mPullUpView.getMaxPullHeight();
			}

			// 矫正基础可视区域
			if (mPullUpView.getBaseClipHeight() != 0) {
				distanceY += mPullUpView.getBaseClipHeight();
			}

			// 可以松手去刷新了
			if (mPullUpRefreshState == PullRefreshState.RELEASE_TO_REFRESH) {
				mContentView.scrollToBottom(false);
				// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
				if (distanceY < mPullUpView.getTriggerHeight()
						&& distanceY > mPullUpView.getBaseClipHeight()) {
					onPullUpStateChanged(PullRefreshState.PULL_TO_REFRESH);
				}
				// 一下子推到顶了
				else if (distanceY <= mPullUpView.getBaseClipHeight()) {
					onPullUpStateChanged(PullRefreshState.DONE);
				}
				// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
				else {
					// 不用进行特别的操作，只用更新paddingTop的值就行了
				}
			}
			// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
			if (mPullUpRefreshState == PullRefreshState.PULL_TO_REFRESH) {
				mContentView.scrollToBottom(false);
				// 下拉到可以进入RELEASE_TO_REFRESH的状态，并增加额外条件判断canAccessPullToRefresh()
				if (distanceY >= mPullUpView.getTriggerHeight()
						&& isEnableLoadAndRefresh()) {
					onPullUpStateChanged(PullRefreshState.RELEASE_TO_REFRESH);
				}
				// 上推到顶了
				else if (distanceY <= mPullUpView.getBaseClipHeight()) {
					onPullUpStateChanged(PullRefreshState.DONE);
					mPullUpView.onPull(mPullUpRefreshState,
							mPullUpView.getBaseClipHeight());
					mIsPullUpEventDetected = false;
					break;
				}
			}

			// done状态下
			if (mPullUpRefreshState == PullRefreshState.DONE) {
				if (distanceY > mPullUpView.getBaseClipHeight()
						+ DETECT_MIN_HEIGHT * Utils.getDensity(getContext())) {
					onPullUpStateChanged(PullRefreshState.PULL_TO_REFRESH);
				}
			}

			// nomore状态下
			if (mPullUpRefreshState == PullRefreshState.NOMORE
					&& distanceY > mPullUpView.getBaseClipHeight()) {
				mContentView.scrollToBottom(false);
			}

			// 更新headView的paddingTop
			if (mPullUpRefreshState == PullRefreshState.RELEASE_TO_REFRESH
					|| mPullUpRefreshState == PullRefreshState.PULL_TO_REFRESH
					|| mPullUpRefreshState == PullRefreshState.NOMORE) {
				mPullUpView.setClipHeight(distanceY);
				mPullUpView.onPull(mPullUpRefreshState,
						mPullUpView.getProgress());
			} else if (mPullUpRefreshState == PullRefreshState.REFRESHING) {
				mPullUpView.setClipHeight(Math.max(distanceY,
						mPullUpView.getTriggerHeight()));
				mPullUpView.onPull(mPullUpRefreshState,
						mPullUpView.getProgress());

				if (distanceY < mPullUpView.getTriggerHeight()) {
					mIsPullUpEventDetected = false;
				} else {
					mContentView.scrollToBottom(false);
				}
			}

			break;
		}
	}

	/** 滑动事件监听 */
	private OnScrollToBottomListener mOnScrollListener = new OnScrollToBottomListener() {

		@Override
		public void onScrollToBottom() {
			if (isEnableLoadAndRefresh()
					&& mScrollRefreshState == ScrollRefreshState.DONE) {
				onScrollRefreshStateChanged(ScrollRefreshState.REFRESHING);
			}
		}
	};

	/**
	 * 判断当前是否可以进行加载或刷新
	 * 
	 * @return
	 */
	private boolean isEnableLoadAndRefresh() {
		// 若正在进行上提加载
		if (mPullUpRefreshState == PullRefreshState.REFRESHING) {
			return false;
			// 若正在进行滚动加载
		} else if (mScrollRefreshState == ScrollRefreshState.REFRESHING) {
			return false;
		} else if (mPullDownRefreshState == PullRefreshState.REFRESHING) {
			return false;
		}
		return true;
	}

	/**
	 * 变更下拉View状态,位移
	 * 
	 * @param newState
	 */
	private void onPullDownStateChanged(PullRefreshState newState) {
		if (!mIsPullDownRefreshEnable) {
			return;
		}

		PullRefreshState oldState = mPullDownRefreshState;
		mPullDownRefreshState = newState;
		mPullDownView.onStateChangedInternal(oldState, newState);

		switch (newState) {
		case RELEASE_TO_REFRESH:
			LogUtil.d(TAG, "Set PullDown release to refresh");
			break;
		case PULL_TO_REFRESH:
			LogUtil.d(TAG, "Set PullDown pulldown to refresh");
			break;
		case REFRESHING:
			runPullRefreshViewAnim(mPullDownView,
					mPullDownView.getTriggerHeight());
			if (mPullDownView.hasPrefixTransitionAnim()) {
				mPullDownView.triggerPrefixTransition(new Runnable() {
					@Override
					public void run() {
						onPullDownRefresh();
					}
				});
			} else {
				onPullDownRefresh();
			}
			LogUtil.d(TAG, "Set PullDown refreshing");
			break;
		case DONE:
			runPullRefreshViewAnim(mPullDownView,
					mPullDownView.getBaseClipHeight());
			LogUtil.d(TAG, "Set PullDown done");
			break;
		case NOMORE:
			runPullRefreshViewAnim(mPullDownView,
					mPullDownView.getBaseClipHeight());
			LogUtil.d(TAG, "Set PullDown nomore");
			break;
		}
	}

	/**
	 * 变更上拉View状态,位移
	 * 
	 * @param newState
	 */
	private void onPullUpStateChanged(PullRefreshState newState) {
		if (!mIsPullUpRefreshEnable) {
			return;
		}

		PullRefreshState oldState = mPullUpRefreshState;
		mPullUpRefreshState = newState;
		mPullUpView.onStateChangedInternal(oldState, newState);

		switch (newState) {
		case RELEASE_TO_REFRESH:
			if (mPullUpView.isRefreshImmediate()) {
				onPullUpStateChanged(PullRefreshState.REFRESHING);
			}
			LogUtil.d(TAG, "Set PullUp release to refresh");
			break;
		case PULL_TO_REFRESH:
			LogUtil.d(TAG, "Set PullUp pulldown to refresh");
			break;
		case REFRESHING:
			runPullRefreshViewAnim(mPullUpView, mPullUpView.getTriggerHeight());
			if (mPullUpView.hasPrefixTransitionAnim()) {
				mPullUpView.triggerPrefixTransition(new Runnable() {
					@Override
					public void run() {
						onPullUpRefresh();
					}
				});
			} else {
				onPullUpRefresh();
			}
			LogUtil.d(TAG, "Set PullUp refreshing");
			break;
		case DONE:
			runPullRefreshViewAnim(mPullUpView, mPullUpView.getBaseClipHeight());
			LogUtil.d(TAG, "Set PullUp done");
			break;
		case NOMORE:
			runPullRefreshViewAnim(mPullUpView, mPullUpView.getBaseClipHeight());
			LogUtil.d(TAG, "Set PullUp nomore");
			break;
		}

	}

	/**
	 * 变更滑动加载View状态
	 * 
	 * @param newState
	 */
	private void onScrollRefreshStateChanged(ScrollRefreshState newState) {
		if (!mIsScrollRefreshEnable) {
			return;
		}

		ScrollRefreshState oldState = mScrollRefreshState;
		mScrollRefreshState = newState;
		mScrollRefreshView.onStateChanged(oldState, newState);

		switch (newState) {
		case REFRESHING:
			mScrollRefreshView.getContentView().setOnClickListener(null);
			onScrollRefresh();
			break;
		case DONE:
			mScrollRefreshView.getContentView().setOnClickListener(null);
			break;
		case FAIL:
			mScrollRefreshView.getContentView().setOnClickListener(
					new OnSingleClickListener() {
						@Override
						public void onSingleClick(View v) {
							onScrollRefreshStateChanged(ScrollRefreshState.REFRESHING);
						}
					});
			break;
		case NOMORE:
			mScrollRefreshView.getContentView().setOnClickListener(null);
			break;
		}
	}

	/**
	 * 偏移到指定位置创建补间动画
	 * @param view
	 * @param clipHeight
	 */
	private void runPullRefreshViewAnim(BasePullRefreshView view, int clipHeight) {
		int duration = view.getPullAnimDuration();
		PullRefreshAnimation pullRefreshAnimation = new PullRefreshAnimation(
				view, clipHeight, duration);
		pullRefreshAnimation.startAnimation();
	}

	/**
	 * 实现刷新功能的View所需实现接口
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface IRefreshView {

		/**
		 * 是否处于页面的顶部，用于计算衡量展开头部下拉View
		 * 
		 * @return
		 */
		public boolean isReachTheTop();

		/**
		 * 是否处于页面的底部，用于计算衡量展开底部上拉View
		 * 
		 * @return
		 */
		public boolean isReachTheBottom();

		/**
		 * 添加对滑动到底部触发滑动加载的事件监听
		 * 
		 * @param listener
		 */
		public void setOnScrollToBottomListener(
				OnScrollToBottomListener listener);

		/**
		 * 滚动到页面顶部
		 * 
		 * @param isAnim
		 */
		public void scrollToTop(boolean isAnim);

		/**
		 * 滚动到页面底部
		 * 
		 * @param isAnim
		 */
		public void scrollToBottom(boolean isAnim);

	}

	/**
	 * 实现刷新功能的ScrollView所需实现接口
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface IRefreshScrollView extends IRefreshView {

	}

	/**
	 * 实现刷新功能的ListView所需实现接口
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface IRefreshListView extends IRefreshView {

		/**
		 * 添加顶部下拉View到ListView中
		 * 
		 * @param headerView
		 */
		public void addHeaderRefreshView(View headerView);

		/**
		 * 添加底部上拉或提示加载View到ListView中
		 * 
		 * @param footerView
		 */
		public void addFooterRefreshView(View footerView);

	}

	/**
	 * 滑动到底部触发事件监听
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface OnScrollToBottomListener {

		/**
		 * 滑动到View底部
		 */
		public void onScrollToBottom();
	}

	/**
	 * 获取当前列表状态
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public enum RefreshViewState {
		WAITING_PULL_DOWN_REFRESH_RESULT, WAITING_PULL_UP_REFRESH_RESULT, WAITING_SCROLLREFRESH_RESULT, NORMAL
	}

	/**
	 * 下拉刷新状态
	 * 
	 * @see #RELEASE_TO_REFRESH 释放后刷新
	 * @see #PULL_TO_REFRESH 下拉后刷新
	 * @see #REFRESHING 正在刷新
	 * @see #DONE 正常状态
	 * @see #NOMORE 无可用加载项
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public enum PullRefreshState {
		RELEASE_TO_REFRESH, PULL_TO_REFRESH, REFRESHING, DONE, NOMORE
	}

	/**
	 * 滑动刷新状态
	 * 
	 * @see #REFRESHING 正在加载
	 * @see #FAIL 加载失败
	 * @see #DONE 正常状态
	 * @see #NOMORE 无后续加载项
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public enum ScrollRefreshState {
		REFRESHING, FAIL, DONE, NOMORE
	}

	/**
	 * 下拉刷新事件监听
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface OnPullDownRefreshListener {
		public void onPullDownRefresh();
	}

	/**
	 * 上拉加载事件监听
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface OnPullUpRefreshListener {
		public void onPullUpRefresh();
	}

	/**
	 * 滑动加载事件监听
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface OnScrollRefreshListener {
		public void onScrollRefresh();
	}

	/**
	 * 拖拉刷新View控制基类，分别通过实现:<br>
	 * 
	 * {@link #setClipHeight(int)}方法控制显示隐藏可见区域。<br>
	 * {@link #getClipHeight()}方法协助返回当前可见区域高度。<br>
	 * {@link #getPullRefreshView()}方法获取刷新view。<br>
	 * {@link #onStateChanged(PullRefreshState, PullRefreshState)}
	 * 方法控制在状态切换时需要执行的界面操作。<br>
	 * 选择性实现{@link #onPull(PullRefreshState, float)}方法根据当前进度来实现细节化ui操作。<br>
	 * <br>
	 * 注意：最外层View需要为LinearLayout布局。
	 * 
	 * @see {@link DefaultPullDownRefreshView}参考实现。
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public static abstract class BasePullRefreshView implements NoProGuard {

		/** 实际手指滑动的距离与界面显示距离的偏移比，例如：手指画过300px距离，则只展示出100px的拉伸，橡皮效果。 */
		private final static int DEFAULT_PULL_RATIO = 3;
		/** 默认的补间动画时长 300ms */
		private final static int DEFAULT_PULL_ANIMATION_DURATION = 300;

		/** context */
		private Activity mActivity;
		/** 拖拉View */
		private View mContentView;
		/** 拖拉View的高度 */
		private int mContentViewHeight;

		/** 上次下拉刷新时间记录 */
		private long mLastUpdateTime;
		/** 是否在上拉之后即触发加载 */
		private boolean mRefreshImmediate = false;

		/** 上次所处的状态 */
		private PullRefreshState mLastPullState = PullRefreshState.DONE;
		/** 当前所处的状态 */
		private PullRefreshState mCurrentPullState = PullRefreshState.DONE;

		/** 起始动画结束后的操作 */
		private Runnable mPrefixNextAction;
		/** 终止动画结束后的操作 */
		private Runnable mSuffixNextAction;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public BasePullRefreshView(Context context) {
			mActivity = (Activity) context;
			mLastUpdateTime = System.currentTimeMillis();
		}

		/**
		 * 获取之前一次拉取状态
		 * 
		 * @return
		 */
		protected final PullRefreshState getLastPullState() {
			return mLastPullState;
		}

		/**
		 * 获取当前拉取状态
		 * 
		 * @return
		 */
		protected final PullRefreshState getCurrentPullState() {
			return mCurrentPullState;
		}

		/**
		 * 是否有起始动画
		 * 
		 * @return
		 */
		protected boolean hasPrefixTransitionAnim() {
			return false;
		}

		/**
		 * 是否有终止动画
		 * 
		 * @return
		 */
		protected boolean hasSuffixTransitionAnim() {
			return false;
		}

		/**
		 * 起始动画
		 */
		protected void onPrefixTransition() {
			// 子类重写
		}

		/**
		 * 终止动画
		 */
		protected void onSuffixTransition() {
			// 子类重写
		}

		/**
		 * 执行起始动画
		 * 
		 * @param nextAction
		 *            结束后触发操作
		 */
		protected final void triggerPrefixTransition(Runnable nextAction) {
			mPrefixNextAction = nextAction;
			onPrefixTransition();
		}

		/**
		 * 执行起始动画
		 * 
		 * @param nextAction
		 *            结束后触发操作
		 */
		protected final void triggerSuffixTransition(Runnable nextAction) {
			mSuffixNextAction = nextAction;
			onSuffixTransition();
		}

		/**
		 * 通知过渡动画结束
		 */
		protected final void onPrefixTransitionAnimFinish() {
			if (hasPrefixTransitionAnim()
					&& mCurrentPullState == PullRefreshState.RELEASE_TO_REFRESH) {
				mContentView.post(mPrefixNextAction);
			}
		}

		/**
		 * 通知过渡动画结束
		 */
		protected final void onSuffixTransitionAnimFinish() {
			if (hasSuffixTransitionAnim()
					&& mCurrentPullState == PullRefreshState.REFRESHING) {
				mContentView.post(mSuffixNextAction);
			}
		}

		/**
		 * 获取绑定的activity对象
		 * 
		 * @return
		 */
		protected final Activity getActivity() {
			return mActivity;
		}

		/**
		 * 供{@link RefreshViewLayout}内部获取拉动的View
		 * 
		 * @return
		 */
		protected final View getContentView() {
			if (mContentView == null) {
				mContentView = getPullRefreshView();

				if (mContentView.getMeasuredHeight() == 0) {
					Utils.measureView(mContentView);
				}
				mContentViewHeight = mContentView.getMeasuredHeight();

				setClipHeight(getBaseClipHeight());
			}
			return mContentView;
		}

		/**
		 * 供{@link RefreshViewLayout}内部获取拉动的View高度
		 * 
		 * @return
		 */
		protected final int getContentHeight() {
			return mContentViewHeight;
		}

		/**
		 * 供{@link RefreshViewLayout} 内部获取触发刷新的高度，可用于分割下拉View和之上的水印，实现下拉之后的水印效果。
		 * 
		 * @return
		 */
		protected int getTriggerHeight() {
			return getContentHeight() + getBaseClipHeight();
		}

		/**
		 * 供{@link RefreshViewLayout} 内部获取下拉View露出隐藏区域的高度，可供实现类似path下拉放大背景的效果。
		 * 
		 * @return
		 */
		protected int getBaseClipHeight() {
			return 0;
		}

		/**
		 * 获取最大拉取高度
		 * 
		 * @return
		 */
		protected int getMaxPullHeight() {
			return Integer.MAX_VALUE;
		}

		/**
		 * 获取当前拖拽进度
		 * 
		 * @return
		 */
		protected final float getProgress() {
			return (float) (getClipHeight() - getBaseClipHeight())
					/ (getTriggerHeight() - getBaseClipHeight());
		}

		/**
		 * 获取动画执行时间
		 * 
		 * @return
		 */
		protected int getPullAnimDuration() {
			return DEFAULT_PULL_ANIMATION_DURATION;
		}

		/**
		 * 获取拉动距离的偏移比，用于实现橡皮筋效果
		 * 
		 * @return
		 */
		protected float getPullRatio() {
			return DEFAULT_PULL_RATIO;
		}

		/**
		 * 获取上次成功加载的时间
		 * 
		 * @return
		 */
		protected final long getLastUpdateTime() {
			return mLastUpdateTime;
		}

		/**
		 * 变更上次加载时间
		 * 
		 * @param time
		 */
		private final void notifyUpdateTime(long time) {
			mLastUpdateTime = time;
		}

		/**
		 * 设置是否在触发临界状态后立即刷新，即不需松手只需要拉动到trigger位置就发生刷新行为
		 * 
		 * @param enable
		 */
		protected final void setRefreshImmediate(boolean enable) {
			mRefreshImmediate = enable;
		}

		/**
		 * 获取是否在触发临界状态后立即刷新
		 */
		protected final boolean isRefreshImmediate() {
			return mRefreshImmediate;
		}

		/**
		 * 设置实现露出隐藏区域的可视高度，注意对{@link #getBaseClipHeight()}的处理。
		 * 
		 * @param clipHeight
		 */
		protected abstract void setClipHeight(int clipHeight);

		/**
		 * 获取当前露出隐藏区域的可视高度
		 * 
		 * @return
		 */
		protected abstract int getClipHeight();

		/**
		 * 生成拉动刷新的View
		 * 
		 * @return
		 */
		protected abstract View getPullRefreshView();

		/**
		 * 刷新View状态发生改变时，需要执行的操作
		 * 
		 * @param oldState
		 * @param newState
		 */
		private void onStateChangedInternal(PullRefreshState oldState,
				PullRefreshState newState) {
			mLastPullState = oldState;
			mCurrentPullState = newState;
			onStateChanged(oldState, newState);
		}

		/**
		 * 刷新View状态发生改变时，需要执行的操作
		 * 
		 * @param oldState
		 * @param newState
		 */
		protected abstract void onStateChanged(PullRefreshState oldState,
				PullRefreshState newState);

		/**
		 * 通知刷新的进度变更以及当前所处状态，其中progress参数为{@link #setClipHeight(int)}方法中返还的结果。
		 * 
		 * @param pullDownRefreshState
		 * @param progress
		 */
		protected void onPull(PullRefreshState pullDownRefreshState,
				float progress) {
			// can be override
		}
	}

	/**
	 * 滑动加载View控制基类，分别通过实现:<br>
	 * {@link BaseScrollRefreshView#getScrollRefreshView()}方法获取加载view。<br>
	 * {@link BaseScrollRefreshView#onStateChanged(ScrollRefreshState, ScrollRefreshState)}
	 * 方法控制在状态切换时需要执行的界面操作。<br>
	 * 
	 * @see {@link DefaultScrollRefreshView}参考实现。
	 * @author xuwei3-pd
	 * 
	 */
	public static abstract class BaseScrollRefreshView implements NoProGuard {

		/** context */
		private Activity mActivity;
		/** 滑动加载View */
		private View mContentView;
		/** 滑动加载View的高度 */
		private int mContentViewHeight;
		/** 上次加载时间记录 */
		private long mLastUpdateTime;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public BaseScrollRefreshView(Context context) {
			mActivity = (Activity) context;
		}

		/**
		 * 获取绑定的activity
		 * 
		 * @return
		 */
		protected Activity getActivity() {
			return mActivity;
		}

		/**
		 * 供{@link RefreshViewLayout}内部获取的View
		 * 
		 * @return
		 */
		protected final View getContentView() {
			if (mContentView == null) {
				mContentView = getScrollRefreshView();

				if (mContentView.getMeasuredHeight() == 0) {
					Utils.measureView(mContentView);
				}
				mContentViewHeight = mContentView.getMeasuredHeight();
			}
			return mContentView;
		}

		/**
		 * 供{@link RefreshViewLayout}内部获取滚动加载的View高度
		 * 
		 * @return
		 */
		protected final int getContentHeight() {
			return mContentViewHeight;
		}

		/**
		 * 获取上次成功加载的时间
		 * 
		 * @return
		 */
		protected final long getLastUpdateTime() {
			return mLastUpdateTime;
		}

		/**
		 * 变更上次加载时间
		 * 
		 * @param time
		 */
		private final void notifyUpdateTime(long time) {
			mLastUpdateTime = time;
		}

		/**
		 * 生成滚动加载的View
		 * 
		 * @return
		 */
		protected abstract View getScrollRefreshView();

		/**
		 * 滚动加载View状态发生改变时，需要执行的操作
		 * @param oldState
		 * @param newState
		 */
		protected abstract void onStateChanged(ScrollRefreshState oldState,
				ScrollRefreshState newState);

	}

	/**
	 * 默认的下拉刷新view
	 * 
	 * @author Calvin
	 * 
	 */
	private static final class DefaultPullDownRefreshView extends
			BasePullRefreshView {

		/*
		 * 缺省的文字提示
		 */
		private static final String PULL_TO_REFRESH = "下拉刷新...";
		private static final String RELEASE_TO_REFRESH = "释放刷新...";
		private static final String REFRESHING = "正在刷新...";
		private static final String NO_MORE = "无可用加载项";
		private static final String TIME_TIPS = "最后更新: ";

		/*
		 * 缺省的下拉刷新View配置
		 */

		/** 下拉刷新的状态提示 */
		private TextView mStateTips;
		/** 下拉刷新的更新时间 */
		private TextView mLastUpdatedTime;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public DefaultPullDownRefreshView(Context context) {
			super(context);
		}

		@Override
		protected final void setClipHeight(int clipHeight) {
			int paddingTop = clipHeight - getContentHeight();
			getContentView().setPadding(0, paddingTop, 0, 0);
		}

		@Override
		protected int getClipHeight() {
			return getContentView().getPaddingTop() + getContentHeight();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected View getPullRefreshView() {
			LinearLayout container = new LinearLayout(getActivity());

			LinearLayout pullRefreshView = new LinearLayout(getActivity());
			pullRefreshView.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, (int) (Utils
							.getDensity(getActivity()) * 50)));
			pullRefreshView.setOrientation(LinearLayout.VERTICAL);
			pullRefreshView.setGravity(Gravity.CENTER);

			mStateTips = new TextView(getActivity());
			mStateTips.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			mStateTips.setGravity(Gravity.CENTER);
			mStateTips.setBackgroundColor(Color.WHITE);
			mStateTips.setTextColor(Color.BLACK);

			mLastUpdatedTime = new TextView(getActivity());
			mLastUpdatedTime.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			mLastUpdatedTime.setGravity(Gravity.CENTER);
			mLastUpdatedTime.setBackgroundColor(Color.WHITE);
			mLastUpdatedTime.setTextColor(Color.BLACK);

			pullRefreshView.addView(mStateTips);
			pullRefreshView.addView(mLastUpdatedTime);
			container.addView(pullRefreshView);

			return container;
		}

		/**
		 * 下拉刷新View状态发生改变时，需要执行的操作
		 * @param oldState
		 * @param newState
		 */
		@Override
		protected void onStateChanged(PullRefreshState oldState,
				PullRefreshState newState) {
			switch (newState) {
			case RELEASE_TO_REFRESH:
				mStateTips.setVisibility(View.VISIBLE);
				mLastUpdatedTime.setVisibility(View.VISIBLE);

				mStateTips.setText(RELEASE_TO_REFRESH);
				break;
			case PULL_TO_REFRESH:
				mStateTips.setVisibility(View.VISIBLE);
				mLastUpdatedTime.setVisibility(View.VISIBLE);

				if (oldState == PullRefreshState.DONE) {
					mLastUpdatedTime.setText(TIME_TIPS
							+ Utils.countTimeIntervalText(getLastUpdateTime()));
				}

				mStateTips.setText(PULL_TO_REFRESH);
				break;
			case REFRESHING:
				mStateTips.setText(REFRESHING);
				mLastUpdatedTime.setVisibility(View.VISIBLE);
				break;
			case DONE:
				mStateTips.setText(PULL_TO_REFRESH);
				mLastUpdatedTime.setVisibility(View.VISIBLE);
				mLastUpdatedTime.setText(TIME_TIPS
						+ Utils.countTimeIntervalText(getLastUpdateTime()));
				break;
			case NOMORE:
				mLastUpdatedTime.setVisibility(View.GONE);
				mStateTips.setText(NO_MORE);
				break;
			}
		}

	}

	/**
	 * 默认的上拉加载view
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	private static final class DefaultPullUpRefreshView extends
			BasePullRefreshView {

		/*
		 * 缺省的文字提示
		 */
		private static final String PULL_TO_LOAD = "上拉加载...";
		private static final String RELEASE_TO_LOAD = "释放加载...";
		private static final String LOADING = "正在加载...";
		private static final String NO_MORE = "无可用加载项";

		/*
		 * 缺省的上拉加载View配置
		 */

		/** 上拉加载的状态提示 */
		private TextView mStateTips;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public DefaultPullUpRefreshView(Context context) {
			super(context);
		}

		@Override
		protected final void setClipHeight(int clipHeight) {
			int paddingBottom = clipHeight - getContentHeight();
			getContentView().setPadding(0, 0, 0, paddingBottom);
		}

		@Override
		protected int getClipHeight() {
			return getContentView().getPaddingBottom() + getContentHeight();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected View getPullRefreshView() {
			LinearLayout container = new LinearLayout(getActivity());

			mStateTips = new TextView(getActivity());
			mStateTips.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, (int) (Utils
							.getDensity(getActivity()) * 40)));
			mStateTips.setGravity(Gravity.CENTER);
			mStateTips.setBackgroundColor(Color.WHITE);
			mStateTips.setTextColor(Color.BLACK);

			container.addView(mStateTips);
			return container;
		}

		/**
		 * 上拉加载View状态发生改变时，需要执行的操作
		 * @param oldState
		 * @param newState
		 */
		@Override
		protected void onStateChanged(PullRefreshState oldState,
				PullRefreshState newState) {
			switch (newState) {
			case RELEASE_TO_REFRESH:
				mStateTips.setVisibility(View.VISIBLE);

				mStateTips.setText(RELEASE_TO_LOAD);
				break;
			case PULL_TO_REFRESH:
				mStateTips.setVisibility(View.VISIBLE);

				mStateTips.setText(PULL_TO_LOAD);
				break;
			case REFRESHING:
				mStateTips.setText(LOADING);
				break;
			case DONE:
				mStateTips.setText(PULL_TO_LOAD);
				break;
			case NOMORE:
				mStateTips.setText(NO_MORE);
				break;
			}
		}

	}

	/**
	 * 默认的滚动加载view
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	private static final class DefaultScrollRefreshView extends
			BaseScrollRefreshView {

		/*
		 * 缺省的文字提示
		 */
		private static final String LOADING = "正在加载...";
		private static final String FAILED = "点击重新加载";

		/*
		 * 缺省的滑动加载View配置
		 */
		/** 滑动加载的FootView的提示文字 */
		private TextView mScrollRefreshTips;

		public DefaultScrollRefreshView(Context context) {
			super(context);
		}

		/**
		 * 生成滚动加载的View
		 * 
		 * @return
		 */
		@SuppressWarnings("deprecation")
		protected View getScrollRefreshView() {
			LinearLayout container = new LinearLayout(getActivity());

			mScrollRefreshTips = new TextView(getActivity());
			mScrollRefreshTips.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, (int) (Utils
							.getDensity(getActivity()) * 40)));
			mScrollRefreshTips.setGravity(Gravity.CENTER);
			mScrollRefreshTips.setBackgroundColor(Color.WHITE);
			mScrollRefreshTips.setTextColor(Color.BLACK);

			container.addView(mScrollRefreshTips);
			return container;
		}

		/**
		 * 滚动加载View状态发生改变时，需要执行的操作
		 * @param oldState
		 * @param newState
		 */
		protected void onStateChanged(ScrollRefreshState oldState,
				ScrollRefreshState newState) {
			switch (newState) {
			case REFRESHING:
				mScrollRefreshTips.setText(LOADING);
				break;
			case DONE:
				mScrollRefreshTips.setText(LOADING);
				getContentView().setPadding(0, 0, 0, 0);
				break;
			case FAIL:
				mScrollRefreshTips.setText(FAILED);
				break;
			case NOMORE:
				getContentView().setPadding(0, -getContentHeight(), 0, 0);
				break;
			}
		}
	}

	/**
	 * 拉动View动作补间动画
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	private class PullRefreshAnimation extends IAnimationRunner.AnimationRunner {

		/** 动画执行View */
		private BasePullRefreshView mTargetView;
		/** 起始点 */
		private int mStart;
		/** 距离差 */
		private int mOffSet;

		public PullRefreshAnimation(BasePullRefreshView view, int clipHeight,
				int duration) {
			super(view.mContentView, duration);
			mTargetView = view;

			mStart = mTargetView.getClipHeight();
			mOffSet = clipHeight - mStart;
			setInterpolator(new DecelerateInterpolator());
		}

		@Override
		public void applyTransformation(float percent) {
			if (mOffSet == 0) {
				return;
			}

			mTargetView.setClipHeight((int) (mStart + mOffSet * percent));
			if (mTargetView == mPullDownView) {
				mTargetView.onPull(mPullDownRefreshState,
						mTargetView.getProgress());

				//在主动出发刷新的时候，配合刷新View滑动到顶部
				if (mPullDownRefreshState == PullRefreshState.REFRESHING)
					mContentView.scrollToTop(false);
			} else if (mTargetView == mPullUpView) {
				mTargetView.onPull(mPullUpRefreshState,
						mTargetView.getProgress());

				//在主动出发刷新的时候，配合刷新View滑动到底部
				if (mPullUpRefreshState == PullRefreshState.REFRESHING)
					mContentView.scrollToBottom(false);
			}
		}

		@Override
		public void onAnimationFinished() {

		}
	}

}
