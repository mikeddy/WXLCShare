package ncf.widget.simple.feature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import ncf.widget.refreshlayout.RefreshViewLayout.*;
import ncf.widget.refreshlayout.util.Utils;
import ncf.widget.simple.R;

/**
 * 列表下拉刷新样式
 * 
 * @author xuwei3-pd
 * 
 */
public class FeatureArrowRefreshHeader extends BasePullRefreshView {

	/** 进度条 */
	private FeatureProgressView mAwesomeProgress;

	public FeatureArrowRefreshHeader(Context context) {
		super(context);
	}

	@Override
	protected void setClipHeight(int clipHeight) {
		LayoutParams progressParams = (LayoutParams) mAwesomeProgress
				.getLayoutParams();
		progressParams.height = clipHeight;
		mAwesomeProgress.setLayoutParams(progressParams);
	}

	@Override
	protected int getTriggerHeight() {
		return mAwesomeProgress.getStandHeight();
	}

	@Override
	protected int getClipHeight() {
		return mAwesomeProgress.getLayoutParams().height;
	}

	@Override
	protected View getPullRefreshView() {
		LinearLayout container = new LinearLayout(getActivity());
		container.setGravity(Gravity.CENTER);

		mAwesomeProgress = new FeatureProgressView(getActivity());
		mAwesomeProgress.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		container.addView(mAwesomeProgress);

		mAwesomeProgress.setBackgroundColor(getActivity().getResources()
				.getColor(R.color.bg_feature_header));

		return container;
	}

	@Override
	protected void onStateChanged(PullRefreshState oldState,
			PullRefreshState newState) {
		if (newState == PullRefreshState.REFRESHING) {
			mAwesomeProgress.setRefreshState(true);
		}

		if (oldState == PullRefreshState.REFRESHING) {
			mAwesomeProgress.setRefreshState(false);
		}

		if (newState == PullRefreshState.PULL_TO_REFRESH) {
			mAwesomeProgress.mDrawingInternal = mAwesomeProgress.mInternalImg;
			mAwesomeProgress.mDrawingExternal = mAwesomeProgress.mExternalImg;
		}
	}

	@Override
	protected boolean hasSuffixTransitionAnim() {
		return true;
	}

	@Override
	protected void onSuffixTransition() {
		mAwesomeProgress.runRefreshSuffix();
	}

	/**
	 * 通用下拉刷新动画控制View
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	class FeatureProgressView extends View {

		/** 基础刷新临界高度 */
		private static final int BASE_STAND_HEIHTG = 48;
		/** 最小缩放比 */
		private static final float MIN_SCALE = 1 / 3f;
		/** 最大缩放比 */
		private static final float MAX_SCALE = 1 * .7f;
		/** 圆圈高度和总高度的比5:8 */
		private static final float RATIO = 5 / 8f;

		/** 动画执行速度 */
		private static final int ANIM_SPEED = 50;
		/** 旋转速度 */
		private static final float ROTATE_SPEED = 20f;

		/** 图像适配缩放后高度 */
		private float mScaleHeight;

		/** 旋转角度 */
		private float mRotateDegrees;

		/** 外围设定的当前是否为正在进行刷新，从而决定是否显示动画 */
		private boolean mRefreshing = false;

		private Bitmap mInternalImg;
		private Bitmap mInternalActiveImg;
		private Bitmap mExternalImg;
		private Bitmap mExternalActiveImg;

		private Matrix mInternalMatrix;
		private Matrix mExternalMatrix;

		private Paint mBitmapPaint;
		private Bitmap mDrawingInternal;
		private Bitmap mDrawingExternal;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public FeatureProgressView(Context context) {
			super(context);

			mInternalActiveImg = BitmapFactory.decodeResource(this.getContext()
					.getResources(),
					R.drawable.img_header_feature_internal_green);
			mExternalActiveImg = BitmapFactory.decodeResource(getResources(),
					R.drawable.img_header_feature_external_green);

			mInternalImg = BitmapFactory.decodeResource(this.getContext()
					.getResources(),
					R.drawable.img_header_feature_internal_gray);
			mExternalImg = BitmapFactory.decodeResource(getResources(),
					R.drawable.img_header_feature_external_gray);

			mScaleHeight = BASE_STAND_HEIHTG * Utils.getDensity(getContext());

			mBitmapPaint = new Paint();
			mBitmapPaint.setAntiAlias(true);

			mInternalMatrix = new Matrix();
			mExternalMatrix = new Matrix();

			mDrawingInternal = mInternalImg;
			mDrawingExternal = mExternalImg;
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			float scale = getHeight() * RATIO / mScaleHeight;
			scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

			// 设定箭头图的中点
			float cx = getWidth() / 2;
			float cy = 0;
			if (getHeight() * RATIO < mScaleHeight * MIN_SCALE) {
				cy = mScaleHeight * MIN_SCALE / 2 + getHeight() * (1 - RATIO)
						/ 2 + Utils.dip2px(getContext(), 5);
			} else {
				cy = mScaleHeight * scale / 2 + getHeight() * (1 - RATIO) / 2
						+ Utils.dip2px(getContext(), 5);
			}

			mExternalMatrix.reset();
			mInternalMatrix.reset();

			mInternalMatrix.postScale(scale, scale,
					mInternalImg.getWidth() / 2, mInternalImg.getHeight() / 2);
			mExternalMatrix.postScale(scale, scale,
					mExternalImg.getWidth() / 2, mExternalImg.getHeight() / 2);

			if (mRefreshing) {
				mExternalMatrix.postRotate(mRotateDegrees,
						mExternalImg.getWidth() / 2,
						mExternalImg.getHeight() / 2);
			}

			mInternalMatrix.postTranslate(cx - mInternalImg.getWidth() / 2, cy
					- mInternalImg.getHeight() / 2);
			mExternalMatrix.postTranslate(cx - mExternalImg.getWidth() / 2, cy
					- mExternalImg.getHeight() / 2);

			canvas.drawBitmap(mDrawingInternal, mInternalMatrix, mBitmapPaint);
			canvas.drawBitmap(mDrawingExternal, mExternalMatrix, mBitmapPaint);

		}

		/** 执行动画的Runnable */
		private Runnable mRefreshAnimRunnable = new Runnable() {

			@Override
			public void run() {
				mRotateDegrees += ROTATE_SPEED;
				mRotateDegrees %= 360f;
				invalidate();
				postDelayed(mRefreshAnimRunnable, (long) (1000f / ANIM_SPEED));
			}
		};

		/** 执行结束动画的Runnable */
		private Runnable mSuffixAnimRunnable = new Runnable() {

			@Override
			public void run() {
				mRotateDegrees += ROTATE_SPEED;
				mRotateDegrees %= 360f;
				if (Math.abs(mRotateDegrees - 360f) <= ROTATE_SPEED * 1.5) {
					removeCallbacks(mSuffixAnimRunnable);
					mRotateDegrees = 0;
					mDrawingInternal = mInternalActiveImg;
					mDrawingExternal = mExternalActiveImg;
					postDelayed(new Runnable() {
						public void run() {
							onSuffixTransitionAnimFinish();
						}
					}, 300);
				} else {
					postDelayed(mSuffixAnimRunnable,
							(long) (1000f / ANIM_SPEED));
				}
				invalidate();
			}
		};

		/**
		 * 设置当前是否为更新状态
		 * 
		 * @param isRefresh
		 */
		void setRefreshState(boolean isRefresh) {
			if (isRefresh == mRefreshing) {
				return;
			}

			if (isRefresh) {
				mDrawingInternal = mInternalImg;
				mDrawingExternal = mExternalImg;
			} else {
				mDrawingInternal = mInternalActiveImg;
				mDrawingExternal = mExternalActiveImg;
			}

			mRefreshing = isRefresh;
			if (isRefresh) {
				mRotateDegrees = 0;
				post(mRefreshAnimRunnable);
			} else {
				removeCallbacks(mRefreshAnimRunnable);
				removeCallbacks(mSuffixAnimRunnable);
			}
		}

		/**
		 * 执行刷新动画的收尾动画
		 */
		void runRefreshSuffix() {
			if (mRefreshing != true) {
				return;
			}

			removeCallbacks(mRefreshAnimRunnable);
			post(mSuffixAnimRunnable);
		}

		/**
		 * 获取刷新时进度条View应有的高度
		 * 
		 * @return
		 */
		public int getStandHeight() {
			return (int) (mScaleHeight / RATIO);
		}

		@Override
		protected void onAttachedToWindow() {
			if (mRefreshing) {
				removeCallbacks(mRefreshAnimRunnable);
				post(mRefreshAnimRunnable);
			}
			super.onAttachedToWindow();
		}

		@Override
		protected void onDetachedFromWindow() {
			removeCallbacks(mRefreshAnimRunnable);
			super.onDetachedFromWindow();
		}
	}

}
