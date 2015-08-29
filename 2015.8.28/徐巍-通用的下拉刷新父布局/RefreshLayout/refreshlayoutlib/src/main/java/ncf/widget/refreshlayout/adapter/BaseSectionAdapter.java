package ncf.widget.refreshlayout.adapter;

import android.app.Activity;
import android.view.View;

/**
 * 基于{@link BaseComposeListAdapter}实现的分栏list adapter，缺省为单一样式的adapter，重写
 * {@link #getViewTypeCount(int)}、 {@link #getItemViewType(int, int)}、
 * {@link #getComposeItemView(View, int, int, int)}方法可获取组合样式的BaseSectionAdapter。
 * 
 * @author xuwei3-pd
 * 
 */
public abstract class BaseSectionAdapter extends BaseComposeListAdapter {

	/** section样式id */
	private static final int COMPOSE_STYLE_SECTION = 0;
	/** 单一item样式id */
	private static final int COMPOSE_STYLE_ITEM = 1;

	/** 单一样式adapter */
	private boolean mIsComposeAdapter = true;

	/**
	 * 构造
	 * 
	 * @param activity
	 */
	public BaseSectionAdapter(Activity activity) {
		super(activity);

		// 初始化mIsComposeAdapter工作，判断是否为单一样式模式
		getViewTypeCount(0);
	}

	/*
	 * 改写BaseAdapter部分 (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */

	@Override
	public final int getCount() {
		int sectionCount = getSectionCount();
		if (sectionCount <= 0) {
			return 0;
		}

		int count = sectionCount;
		for (int i = 0; i < sectionCount; i++) {
			count += getCount(i);
		}
		return count;
	}

	/**
	 * 获取指定sectionId下的item数量
	 * 
	 * @param sectionId
	 * @return
	 */
	public abstract int getCount(int sectionId);

	/*
	 * 改写BaseAdapter部分 (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */

	@Override
	public final Object getItem(int position) {
		// 分配给各个section
		int sectionCount = getSectionCount();
		for (int i = 0; i < sectionCount; i++) {
			if (position == 0) {
				return COMPOSE_STYLE_SECTION;
			}
			// 去除section项
			position = position - 1;
			if (getCount(i) > position) {
				return getItem(i, position);
			} else {
				position -= getCount(i);
			}
		}
		return null;
	}

	/**
	 * 获取是定sectionId下的某个item
	 * 
	 * @param sectionId
	 * @param position
	 * @return
	 */
	protected abstract Object getItem(int sectionId, int position);

	/*
	 * 改写自BaseListAdapter对getViewHeight方法进行分发(non-Javadoc)
	 * 
	 * @see com.qihoopp.framework.ui.adapter.BaseListAdapter#getViewHeight(int)
	 */

	@Override
	protected final int getViewHeight(int position) {
		// 分配给各个section
		int sectionCount = getSectionCount();
		for (int i = 0; i < sectionCount; i++) {
			if (position == 0) {
				return getSectionViewHeight(i);
			}
			// 去除section项
			position = position - 1;
			if (getCount(i) > position) {
				return getItemViewHeight(i, position);
			} else {
				position -= getCount(i);
			}
		}
		return super.getViewHeight(position);
	}

	/**
	 * 获取指定位置section高度
	 * 
	 * @param sectionId
	 * @return
	 */
	protected int getSectionViewHeight(int sectionId) {
		return super.getViewHeight(sectionId);
	}

	/**
	 * 获取指定section下某个positionitem高度
	 * 
	 * @param sectionId
	 * @param position
	 * @return
	 */
	protected int getItemViewHeight(int sectionId, int position) {
		return super.getViewHeight(position);
	}

	/*
	 * 改写自BaseComposeListAdapter，对组合样式汇总进行分发 (non-Javadoc)
	 * 
	 * @see
	 * com.qihoopp.framework.ui.adapter.BaseComposeListAdapter#getViewTypeCount
	 * ()
	 */
	@Override
	public final int getViewTypeCount() {
		int sectionCount = getSectionCount();
		if (sectionCount <= 0) {
			if (!mIsComposeAdapter) {
				return 2;
			} else {
				throw new UnsupportedOperationException(
						"ViewTypeCount不可动态进行改变，请在setAdapter之前进行设置。");
			}
		}

		// 缺省的单一样式模式
		if (!mIsComposeAdapter) {
			// section+item
			return 2;
		} else {
			// section+每个section下额外样式
			int sumStyleCount = 1;

			for (int i = 0; i < sectionCount; i++) {
				sumStyleCount += getViewTypeCount(i);
			}
			return sumStyleCount;
		}
	}

	/**
	 * 获取指定section下的样式数量
	 * 
	 * @param sectionId
	 * @return
	 */
	public int getViewTypeCount(int sectionId) {
		// 缺省为单一样式列表
		mIsComposeAdapter = false;
		return 1;
	}

	/*
	 * 改写自BaseComposeListAdapter，对子项item样式类型进行分发 (non-Javadoc)
	 * 
	 * @see com.qihoopp
	 * .framework.ui.adapter.BaseComposeListAdapter#getItemViewType(int)
	 */
	@Override
	public final int getItemViewType(int position) {
		// 分配给各个section
		int style = 0;
		int sectionCount = getSectionCount();
		for (int i = 0; i < sectionCount; i++) {
			if (position == 0) {
				style = COMPOSE_STYLE_SECTION;
				break;
			}

			// 去除section项
			position = position - 1;
			if (getCount(i) > position) {
				// 当前section中的样式+section样式本身
				style += mIsComposeAdapter ? getItemViewType(i, position) + 1
						: COMPOSE_STYLE_ITEM;
				break;
			} else {
				position -= getCount(i);
				style += mIsComposeAdapter ? getViewTypeCount(i) : 0;
			}

		}
		return style;
	}

	/**
	 * 获取指定section下的某一位置样式类型
	 * 
	 * @param sectionId
	 * @param position
	 * @return
	 */
	protected int getItemViewType(int sectionId, int position) {
		// 缺省为单一样式
		mIsComposeAdapter = false;
		return 0;
	}

	/*
	 * 改写自BaseComposeListAdapter，对子项View的获取进行分发(non-Javadoc)
	 * 
	 * @see
	 * com.qihoopp.framework.ui.adapter.BaseComposeListAdapter#getComposeItemView
	 * (android.view.View, int, int)
	 */
	@Override
	protected final View getComposeItemView(View convertView, int style,
			int position) {
		int sectionCount = getSectionCount();
		for (int i = 0; i < sectionCount; i++) {
			if (position == 0 && style == COMPOSE_STYLE_SECTION) {
				return getSectionView(convertView, i);
			}
			// 去除section
			position = position - 1;

			if (getCount(i) > position) {
				if (mIsComposeAdapter) {
					// 去除section
					style = style - 1;
					for (int j = 0; j < i; j++) {
						style -= getViewTypeCount(j);
					}
					return getComposeItemView(convertView, i, style, position);
				} else {
					return getItemView(convertView, i, position);
				}
			} else {
				position -= getCount(i);
			}
		}
		return null;
	}

	/**
	 * 获取分栏条数
	 * 
	 * @return
	 */
	public abstract int getSectionCount();

	/**
	 * 获取指定sectionId下的section name
	 * 
	 * @param sectionId
	 * @return
	 */
	public String getSectionName(int sectionId) {
		// do nothing
		return String.valueOf(sectionId);
	}

	/**
	 * 获取指定position所处sectionId
	 * 
	 * @param position
	 * @return
	 */
	public final int getSectionId(int position) {
		if (position < 0 || position >= getCount()) {
			return -1;
		}

		for (int i = 0; i < getSectionCount(); i++) {
			if (position == 0) {
				return i;
			}

			// 去除section项
			position = position - 1;
			if (getCount(i) > position) {
				return i;
			} else {
				position -= getCount(i);
			}
		}

		return -1;
	}

	/**
	 * 获取指定position是否为section
	 * 
	 * @param position
	 * @return
	 */
	public final boolean getPositionIsSection(int position) {
		if (position < 0 || position >= getCount()) {
			return false;
		}

		for (int i = 0; i < getSectionCount(); i++) {
			if (position == 0) {
				return true;
			}

			// 去除section项
			position = position - 1;
			if (getCount(i) > position) {
				return false;
			} else {
				position -= getCount(i);
			}
		}

		return false;
	}

	/**
	 * 获取指定section view
	 * 
	 * @param convertView
	 * @param sectionId
	 * @return
	 */
	public abstract View getSectionView(View convertView, int sectionId);

	/**
	 * 获取指定section下的item view
	 * 
	 * @param convertView
	 * @param sectionId
	 * @param position
	 * @return
	 */
	protected abstract View getItemView(View convertView, int sectionId,
			int position);

	/**
	 * 获取指定section下的item view，若子类未实现 {@link #getComposeStyleCount}以及
	 * {@link #getItemComposeStyle(int, int)}的话，则无需针对style进行逻辑判断。
	 * 
	 * @param convertView
	 * @param sectionId
	 * @param style
	 * @param position
	 * @return
	 */
	protected View getComposeItemView(View convertView, int sectionId,
			int style, int position) {
		// 缺省为单一样式
		if (mIsComposeAdapter) {
			throw new UnsupportedOperationException(
					"Compose Type need override the getComposeItemView(View converView, int sectionId, int style, int position) method");
		}
		return null;
	}
}
