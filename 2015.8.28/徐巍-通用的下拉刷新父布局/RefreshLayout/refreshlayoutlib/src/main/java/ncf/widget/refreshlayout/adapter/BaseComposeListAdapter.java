package ncf.widget.refreshlayout.adapter;

import android.app.Activity;
import android.view.View;

/**
 * 组合形式的列表adapter，适用于列表存在多种样式混合的情况。
 * 
 * @author xuwei3-pd
 * 
 */
public abstract class BaseComposeListAdapter extends BaseListAdapter {

	/**
	 * 构造
	 * 
	 * @param activity
	 */
	public BaseComposeListAdapter(Activity activity) {
		super(activity);
	}

	/**
	 * 获取包含的样式数量，该方法需要在setAdapter之前就返还可能存在的所有样式，不能动态进行改变！
	 * 
	 * @return
	 */
	@Override
	public abstract int getViewTypeCount();

	/**
	 * 获取指定位置的视图样式
	 * 
	 * @param position
	 * @return
	 */
	@Override
	public abstract int getItemViewType(int position);

	/**
	 * 根据样式获取子项View
	 * 
	 * @param convertView
	 * @param style
	 * @param position
	 * @return
	 */
	protected abstract View getComposeItemView(View convertView, int style,
			int position);

	@Override
	protected final View getView(int position, View convertView) {
		return getComposeItemView(convertView, getItemViewType(position),
				position);
	}

}
