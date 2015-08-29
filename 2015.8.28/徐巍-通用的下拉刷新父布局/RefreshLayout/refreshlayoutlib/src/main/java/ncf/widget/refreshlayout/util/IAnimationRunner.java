package ncf.widget.refreshlayout.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 动画接口，提供动画的补间实现、开始动画、判断当前动画状态、以及结束功能
 * 
 * @author xuwei3-pd
 * 
 */
public interface IAnimationRunner {

	/**
	 * 开始动画
	 */
	public void startAnimation();

	/**
	 * 结束动画
	 */
	public void stopAnimation();

	/**
	 * 取消动画
	 */
	public void cancelAnimation();

	/**
	 * 实现补间动画
	 * 
	 * @param percent
	 */
	public void applyTransformation(float percent);

	/**
	 * 动画结束时
	 */
	public void onAnimationFinished();

	/**
	 * 动画是否结束
	 * 
	 * @return
	 */
	public boolean isAnimationEnded();

	/**
	 * 用于自定义处理补间动画的类
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public abstract class AnimationRunner extends Animation implements
			IAnimationRunner {

		/** 具体动画执行对象 */
		private View mAnimView;
		/** 动画是否结束 */
		private boolean mIsEnded;

		/**
		 * 构造
		 * 
		 * @param view
		 */
		public AnimationRunner(View view, int duration) {
			mAnimView = view;
			setDuration(duration);
		}

		@Override
		protected final void applyTransformation(float interpolatedTime,
				Transformation t) {
			applyTransformation(interpolatedTime);
			if (interpolatedTime == 1 && !mIsEnded) {
				mIsEnded = true;
				onAnimationFinished();
			}
		}

		@Override
		public final void startAnimation() {
			if (mAnimView != null) {
				mAnimView.startAnimation(this);
			}
		}

		@Override
		public final void stopAnimation() {
			if (mAnimView != null) {
				mAnimView.clearAnimation();
				applyTransformation(1);
			}
		}

		@Override
		public void cancelAnimation() {
			if (mAnimView != null) {
				mAnimView.clearAnimation();
			}
		}

		@Override
		public final boolean isAnimationEnded() {
			return hasEnded();
		}

		@Override
		public void reset() {
			mIsEnded = false;
			super.reset();
		}
	}
}
