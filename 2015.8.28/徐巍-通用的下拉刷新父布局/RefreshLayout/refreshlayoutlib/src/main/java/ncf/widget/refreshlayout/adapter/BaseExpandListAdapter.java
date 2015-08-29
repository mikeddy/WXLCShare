package ncf.widget.refreshlayout.adapter;

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;

import ncf.widget.refreshlayout.util.OnSingleClickListener;


/**
 * 分组展示的adapter，用于展示类似联系人分组、内容分组等样式。继承自{@link BaseSectionAdapter}可通过
 * {@link #expandUnit(int)}以及{@link #shrinkUnit(int)}方法控制分组的展开与收拢。
 * 
 * @author xuwei3-pd
 * 
 */
public abstract class BaseExpandListAdapter extends BaseSectionAdapter {

	/** 单元数据状态记录 */
	private SparseArray<ExpandUnitState> mExpandUnits;
	/** 状态通知回调 */
	private ExpandListAdapterListener mExpandAdapterListener;

	/**
	 * 构造
	 * 
	 * @param activity
	 */
	public BaseExpandListAdapter(Activity activity) {
		this(activity, null);
	}

	/**
	 * 构造
	 * 
	 * @param activity
	 * @param listener
	 */
	public BaseExpandListAdapter(Activity activity,
			ExpandListAdapterListener listener) {
		super(activity);
		mExpandUnits = new SparseArray<ExpandUnitState>();
		mExpandAdapterListener = listener;
	}

	/**
	 * 展开分组
	 * 
	 * @param sectionId
	 */
	public void expandUnit(int sectionId) {
		expandUnit(sectionId, null);
	}

	/**
	 * 收起分组
	 * 
	 * @param sectionId
	 */
	public void shrinkUnit(int sectionId) {
		shrinkUnit(sectionId, null);
	}

	/**
	 * 是否为展开状态
	 * 
	 * @param sectionId
	 * @return
	 */
	public boolean isExpand(int sectionId) {
		return getExpandUnitState(sectionId).mIsExpand;
	}

	/**
	 * 展开分组
	 * 
	 * @param sectionId
	 * @param sectionView
	 */
	private void expandUnit(int sectionId, View sectionView) {
		ExpandUnitState state = getExpandUnitState(sectionId);
		if (!state.mIsExpand) {
			if (mExpandAdapterListener != null
					&& mExpandAdapterListener.isSingleExpand()) {
				for (int i = 0; i < mExpandUnits.size(); i++) {
					ExpandUnitState eachState = mExpandUnits.get(i);
					if (eachState.mIsExpand) {
						shrinkUnit(i);
						break;
					}
				}
			}
			state.mIsExpand = true;
			notifyDataSetChanged();
			if (mExpandAdapterListener != null) {
				mExpandAdapterListener.onUnitExpand(sectionId, sectionView);
			}
		}
	}

	/**
	 * 收起分组
	 * 
	 * @param sectionId
	 * @param sectionView
	 */
	private void shrinkUnit(int sectionId, View sectionView) {
		ExpandUnitState state = getExpandUnitState(sectionId);
		if (state.mIsExpand) {
			state.mIsExpand = false;
			notifyDataSetChanged();
			if (mExpandAdapterListener != null) {
				mExpandAdapterListener.onUnitShrink(sectionId, sectionView);
			}
		}
	}

	/**
	 * 获取分组状态信息，由此方法处理信息采集工作
	 * 
	 * @param sectionId
	 * @return
	 */
	private ExpandUnitState getExpandUnitState(int sectionId) {
		ExpandUnitState record = mExpandUnits.get(sectionId);

		// 尚未录入
		if (record == null) {
			record = new ExpandUnitState();
			record.mUnitChildCount = getExpandCount(sectionId);

			if (mExpandAdapterListener != null
					&& mExpandAdapterListener.isSingleExpand()) {
				record.mIsExpand = sectionId == 0;
			} else {
				record.mIsExpand = true;
			}

			mExpandUnits.put(sectionId, record);
		} else {
			record.mUnitChildCount = getExpandCount(sectionId);
		}

		return record;
	}

	@Override
	public final int getCount(int sectionId) {
		ExpandUnitState record = getExpandUnitState(sectionId);

		if (record.mIsExpand) {
			return record.mUnitChildCount;
		} else {
			return 0;
		}
	}

	@Override
	public final int getSectionCount() {
		return getExpandSectionCount();
	}

	@Override
	public final View getSectionView(View convertView, final int sectionId) {
		View expandView = getExpandView(convertView, sectionId);
		expandView.setOnClickListener(new OnSingleClickListener() {

			@Override
			public void onSingleClick(View v) {
				ExpandUnitState state = mExpandUnits.get(sectionId);
				if (state.mIsExpand) {
					shrinkUnit(sectionId, v);
				} else {
					expandUnit(sectionId, v);
				}
			}

		});
		return expandView;
	}

	/**
	 * 获取展开分组之中的子项数量
	 * 
	 * @param sectionId
	 * @return
	 */
	public abstract int getExpandCount(int sectionId);

	/**
	 * 获取分组数量
	 * 
	 * @return
	 */
	public abstract int getExpandSectionCount();

	/**
	 * 获取指定的展开分组view
	 * 
	 * @param convertView
	 * @param sectionId
	 * @return
	 */
	public abstract View getExpandView(View convertView, int sectionId);

	/**
	 * 存储某一单元数据状态
	 * 
	 * @author Calvin
	 * 
	 */
	private class ExpandUnitState {

		/** 该单元内中的子列表项数量 */
		private int mUnitChildCount;
		/** 是否展开 */
		private boolean mIsExpand;

	}

	/**
	 * 展开单元的回调接口，通知某一单元展开、收起动作触发，等
	 * 
	 * @author xuwei3-pd
	 * 
	 */
	public interface ExpandListAdapterListener {

		/**
		 * 是否为单一单元展开模式
		 * 
		 * @return
		 */
		public boolean isSingleExpand();

		/**
		 * 展开回调，可用于执行动画<br>
		 * 可在回调中执行{@link IFRefreshListView#setSelectSection(int)}
		 * 方法矫正ListView滚动位置
		 * 
		 * @param position
		 * @param sectionView
		 */
		public void onUnitExpand(int position, View sectionView);

		/**
		 * 收起回调，可用于执行动画，注意：其中sectionView可能为空<br>
		 * 可在回调中执行{@link IFRefreshListView#setSelectSection(int)}
		 * 方法矫正ListView滚动位置
		 * 
		 * @param position
		 * @param sectionView
		 */
		public void onUnitShrink(int position, View sectionView);
	}
}
