package ncf.widget.simple;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ncf.widget.refreshlayout.RefreshViewLayout;
import ncf.widget.refreshlayout.adapter.BaseListAdapter;
import ncf.widget.refreshlayout.adapter.RecyclerHeaderFooterAdapter;
import ncf.widget.refreshlayout.view.RefreshListView;
import ncf.widget.refreshlayout.view.RefreshRecyclerView;
import ncf.widget.refreshlayout.view.RefreshScrollView;
import ncf.widget.simple.feature.FeatureArrowRefreshHeader;
import ncf.widget.simple.view.RefreshLinearLayout;
import ncf.widget.simple.view.RefreshTextView;
import ncf.widget.simple.view.RefreshWebView;

/**
 * Created by XuWei on 15/8/18.
 */
public class MainActivity extends Activity {

    RefreshViewLayout mRefreshViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mRefreshViewLayout = (RefreshViewLayout) findViewById(R.id.refresh);
        mRefreshViewLayout.setPullDownRefreshListener(new RefreshViewLayout.OnPullDownRefreshListener() {
                                                          @Override
                                                          public void onPullDownRefresh() {
                                                              new Handler().postDelayed(new Runnable() {
                                                                  @Override
                                                                  public void run() {
                                                                      mRefreshViewLayout.onPullDownRefreshComplete();
                                                                  }
                                                              }, 3000);
                                                          }
                                                      },
//                new FeatureThemeRefreshHeader(this));
                new FeatureArrowRefreshHeader(this));
//                new FeatureSpotRefreshHeader(this));


        mRefreshViewLayout.setPullUpRefreshListener(new RefreshViewLayout.OnPullUpRefreshListener() {
            @Override
            public void onPullUpRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshViewLayout.onPullUpRefreshComplete();
                    }
                }, 3000);
            }
        });

//        mRefreshViewLayout.setScrollRefreshListener(new RefreshViewLayout.OnScrollRefreshListener() {
//            @Override
//            public void onScrollRefresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRefreshViewLayout.onScrollRefreshComplete();
//                        Toast.makeText(getApplicationContext(), "onScrollRefreshComplete", Toast.LENGTH_LONG).show();
//                    }
//                }, 3000);
//            }
//        });

//        initScrollView();
//        initListView();
//        initCustomViewGroup();
//        initTextView();
        initWebView();
//        initRecyclerView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshViewLayout.forcePullDownRefresh();
//                mRefreshViewLayout.forcePullUpRefresh();
            }
        }, 3000);
    }

    private void initScrollView() {
        RefreshScrollView scrollView = (RefreshScrollView) mRefreshViewLayout.getContentView(RefreshScrollView.class);
        scrollView.addView(getLayoutInflater().inflate(R.layout.scrollview, null), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void initListView() {
        RefreshListView listView = (RefreshListView) mRefreshViewLayout.getContentView(RefreshListView.class);
        listView.setAdapter(new BaseListAdapter(this) {
            @Override
            protected View getView(int position, View convertView) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.item, null);
                return convertView;
            }

            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }
        });
    }

    private void initCustomViewGroup() {
        mRefreshViewLayout.getContentView(RefreshLinearLayout.class);
    }

    private void initTextView() {
        mRefreshViewLayout.getContentView(RefreshTextView.class);
    }

    private void initWebView() {
        mRefreshViewLayout.getContentView(RefreshWebView.class);
    }

    private void initRecyclerView() {
        RefreshRecyclerView recyclerView = new RefreshRecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new RecyclerHeaderFooterAdapter(recyclerView) {

            @Override
            public int getContentItemCount() {
                return 50;
            }

            @Override
            public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
                return new TestHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false));
            }

            @Override
            public void onBindContentViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            }

            class TestHolder extends RecyclerView.ViewHolder {

                public TestHolder(View itemView) {
                    super(itemView);
                }
            }

        });
        mRefreshViewLayout.setContentView(recyclerView);
    }

}
