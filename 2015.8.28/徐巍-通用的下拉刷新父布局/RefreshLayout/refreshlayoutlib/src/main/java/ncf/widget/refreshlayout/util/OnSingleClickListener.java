package ncf.widget.refreshlayout.util;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * 单击事件监听，为了避免快速点击导致的触发两次 {@link OnClickListener#onClick(View)}事件
 * 
 * @author xuwei3-pd
 * 
 */
public abstract class OnSingleClickListener implements OnClickListener {

	/** 点击时间记录 */
	private long mClickTimeRecord;

	@Override
	public final void onClick(View v) {
		long currentTime = System.currentTimeMillis();
		if (Math.abs(currentTime - mClickTimeRecord) > 500) {
			mClickTimeRecord = currentTime;
			onSingleClick(v);
		}
	}

	/**
	 * 单击事件
	 * 
	 * @param v
	 */
	public abstract void onSingleClick(View v);

	/**
	 * 空白点击事件
	 * 
	 * @author xuwei
	 * 
	 */
	public static class ClickInterceptListner extends OnSingleClickListener {

		@Override
		public void onSingleClick(View v) {

		}

	}
}
