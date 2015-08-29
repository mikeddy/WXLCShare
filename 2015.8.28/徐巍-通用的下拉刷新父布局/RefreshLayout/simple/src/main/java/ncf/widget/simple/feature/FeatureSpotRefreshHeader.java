package ncf.widget.simple.feature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
public class FeatureSpotRefreshHeader extends BasePullRefreshView {

	/** 进度条 */
	private GeneralProgressView mAwesomeProgress;

	public FeatureSpotRefreshHeader(Context arg0) {
		super(arg0);
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

	@SuppressWarnings("deprecation")
	@Override
	protected View getPullRefreshView() {
		LinearLayout container = new LinearLayout(getActivity());
		container.setGravity(Gravity.CENTER);

		mAwesomeProgress = new GeneralProgressView(getActivity());
		mAwesomeProgress.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		container.addView(mAwesomeProgress);

		return container;
	}

	@Override
	protected void onStateChanged(PullRefreshState oldState,
			PullRefreshState newState) {
		if (newState == PullRefreshState.REFRESHING) {
			mAwesomeProgress.setRefresh(true);
		}

		if (oldState == PullRefreshState.REFRESHING) {
			mAwesomeProgress.setRefresh(false);
		}
	}

	/**
	 * 通用下拉刷新动画控制View
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public class GeneralProgressView extends View {

		/** 基础刷新临界高度 */
		private static final int BASE_STAND_HEIHTG = 35;
		/** 最小缩放比 */
		private static final float MIN_SCALE = 0.5f;
		/** 最大缩放比 */
		private static final float MAX_SCALE = 1.3f;
		/** 圆圈高度和总高度的比5:8 */
		private static final float RATIO = 5 / 8f;

		/** 圆点最大缩放比1:1.8 */
		private static final float SPOT_RATIO_RADIOS = 1 / 1.8f;
		/** 两圆之间间距 */
		private static final float SPOT_GAP = 2.5f;

		/** 动画执行速度 */
		private static final int ANIM_SPEED = 50;
		/** 圆点闪烁速度 */
		private static final float SPOT_SLPASH_SPEED = 1.8f;

		/** 灰色底圆 */
		private Paint mBackgroundPaint;
		/** 圆点画笔 */
		private Paint mSpotPaint;

		/** 图像适配缩放后高度 */
		private float mScaleHeight;

		/** 三个小点的闪烁进度 */
		private float mSpotScaleProgress;

		/** 外围设定的当前是否为正在进行刷新，从而决定是否显示动画 */
		private boolean mRefreshing = false;

		/**
		 * 构造
		 * 
		 * @param context
		 */
		public GeneralProgressView(Context context) {
			super(context);

			mScaleHeight = BASE_STAND_HEIHTG * Utils.getDensity(getContext());

			mBackgroundPaint = new Paint();
			mBackgroundPaint.setAntiAlias(true);
			mBackgroundPaint.setColor(getResources().getColor(
					R.color.bg_pulldownrefresh_out));
			mBackgroundPaint.setStyle(Style.FILL);

			mSpotPaint = new Paint();
			mSpotPaint.setColor(getResources().getColor(
					R.color.bg_pulldownrefresh_other));
			mSpotPaint.setAntiAlias(true);
			mSpotPaint.setStyle(Style.FILL);
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			float scale = getHeight() * RATIO / mScaleHeight;
			scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

			float backgroundRadio = mScaleHeight * scale / 2;
			float spotRadio = backgroundRadio / 7.5f;
			float spotDistance = spotRadio * (2 + SPOT_GAP);

			// 设定箭头图的中点
			float cx = getWidth() / 2;
			float cy = 0;
			if (getHeight() * RATIO < mScaleHeight * MIN_SCALE) {
				cy = mScaleHeight * MIN_SCALE / 2 + getHeight() * (1 - RATIO)
						/ 2;
			} else if (getHeight() * RATIO <= mScaleHeight * MAX_SCALE) {
				cy = mScaleHeight * scale / 2 + getHeight() * (1 - RATIO) / 2;
			} else {
				cy = getHeight() - mScaleHeight * scale / 2 - mScaleHeight
						* MAX_SCALE / RATIO * (1 - RATIO) / 2;
			}

			canvas.drawCircle(cx, cy, backgroundRadio, mBackgroundPaint);

			// 如果非正常刷新状态则三个圆点大小相等
			if (!mRefreshing) {
				canvas.drawCircle(cx - spotDistance, cy, spotRadio,
						updateSpotPaintSetting(spotRadio, spotRadio));
				canvas.drawCircle(cx, cy, spotRadio,
						updateSpotPaintSetting(spotRadio, spotRadio));
				canvas.drawCircle(cx + spotDistance, cy, spotRadio,
						updateSpotPaintSetting(spotRadio, spotRadio));
			} else {
				// 刷新时三个圆点轮番变大
				float rL = spotRadio;
				float rM = spotRadio;
				float rR = spotRadio;

				float scaleBR = 1f + mSpotScaleProgress % 33 / 33
						* SPOT_RATIO_RADIOS;
				float scaleSR = 1f + (33 - mSpotScaleProgress % 33) / 33
						* SPOT_RATIO_RADIOS;

				if (mSpotScaleProgress >= 0 && mSpotScaleProgress < 33) {
					rL *= scaleSR;
					rM *= scaleBR;
				} else if (mSpotScaleProgress >= 33 && mSpotScaleProgress < 66) {
					rM *= scaleSR;
					rR *= scaleBR;
				} else if (mSpotScaleProgress >= 66 && mSpotScaleProgress < 99) {
					rL *= scaleBR;
					rR *= scaleSR;
				}

				mSpotPaint.setColor(getResources().getColor(
						R.color.bg_pulldownrefresh_other));

				canvas.drawCircle(cx - spotDistance, cy, rL,
						updateSpotPaintSetting(rL, spotRadio));
				canvas.drawCircle(cx, cy, rM,
						updateSpotPaintSetting(rM, spotRadio));
				canvas.drawCircle(cx + spotDistance, cy, rR,
						updateSpotPaintSetting(rR, spotRadio));
			}
		}

		/**
		 * 更新圆点画笔设置
		 * 
		 * @param targetR
		 * @param stantardR
		 */
		private Paint updateSpotPaintSetting(float targetR, float stantardR) {
			if (targetR == stantardR) {
				mSpotPaint.setColor(getResources().getColor(
						R.color.bg_pulldownrefresh_other));
			} else {
				int minR = Color.red(getResources().getColor(
						R.color.bg_pulldownrefresh_other));
				int minG = Color.green(getResources().getColor(
						R.color.bg_pulldownrefresh_other));
				int minB = Color.blue(getResources().getColor(
						R.color.bg_pulldownrefresh_other));

				int maxR = Color.red(getResources().getColor(
						R.color.bg_pulldownrefresh_focus));
				int maxG = Color.green(getResources().getColor(
						R.color.bg_pulldownrefresh_focus));
				int maxB = Color.blue(getResources().getColor(
						R.color.bg_pulldownrefresh_focus));

				float scale = (targetR / stantardR) - 1;
				int red = (int) (scale * (maxR - minR) + minR);
				int green = (int) (scale * (maxG - minG) + minG);
				int blue = (int) (scale * (maxB - minB) + minB);

				mSpotPaint.setColor(Color.rgb(red, green, blue));
			}

			return mSpotPaint;
		}

		/** 执行动画的Runnable */
		private Runnable mAnimRunnable = new Runnable() {

			@Override
			public void run() {
				mSpotScaleProgress += SPOT_SLPASH_SPEED;
				mSpotScaleProgress %= 99f;
				invalidate();
				postDelayed(mAnimRunnable, (long) (1000f / ANIM_SPEED));
			}
		};

		/**
		 * 设置当前是否为更新状态
		 * 
		 * @param isRefresh
		 */
		public void setRefresh(boolean isRefresh) {
			mRefreshing = isRefresh;
			if (isRefresh) {
				mSpotScaleProgress = 0;
				post(mAnimRunnable);
			} else {
				removeCallbacks(mAnimRunnable);
			}
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
				post(mAnimRunnable);
			}
			super.onAttachedToWindow();
		}

		@Override
		protected void onDetachedFromWindow() {
			removeCallbacks(mAnimRunnable);
			super.onDetachedFromWindow();
		}
	}

}
