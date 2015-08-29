package ncf.widget.simple.feature;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ncf.widget.refreshlayout.RefreshViewLayout.BasePullRefreshView;
import ncf.widget.refreshlayout.RefreshViewLayout.PullRefreshState;
import ncf.widget.refreshlayout.util.Utils;
import ncf.widget.simple.R;

/**
 * 伸缩图片Header
 * Created by XuWei on 15/8/19.
 */
public class FeatureThemeRefreshHeader extends BasePullRefreshView {

    private static final int BASE_HEIGHT = 100;

    private ImageView mHeaderImageView;

    /**
     * 构造
     *
     * @param context
     */
    public FeatureThemeRefreshHeader(Context context) {
        super(context);
    }

    @Override
    protected void setClipHeight(int clipHeight) {
        LinearLayout.LayoutParams progressParams = (LinearLayout.LayoutParams) mHeaderImageView
        .getLayoutParams();
        progressParams.height = clipHeight;
        mHeaderImageView.setLayoutParams(progressParams);
    }

    @Override
    protected int getBaseClipHeight() {
        return Utils.dip2px(getActivity(), 100);
    }

    @Override
    protected int getClipHeight() {
        return mHeaderImageView.getLayoutParams().height;
    }

    @Override
    protected int getTriggerHeight() {
        return Utils.dip2px(getActivity(), 250);
    }

    @Override
    protected View getPullRefreshView() {
        LinearLayout container = new LinearLayout(getActivity());
        mHeaderImageView = new ImageView(getActivity());
        mHeaderImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mHeaderImageView.setImageResource(R.drawable.bg_feature_header);

        container.addView(mHeaderImageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dip2px(getActivity(), BASE_HEIGHT)));
        return container;
    }

    @Override
    protected void onStateChanged(PullRefreshState oldState, PullRefreshState newState) {

    }

}
