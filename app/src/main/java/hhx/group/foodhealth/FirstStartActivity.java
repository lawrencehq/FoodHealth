package hhx.group.foodhealth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Xiaoting Huang on 2017-10-7.
 * Display guiding information the first time open the app
 */

public class FirstStartActivity extends Activity implements ViewPager.OnPageChangeListener {

    private ViewPager mviewPager;
    private ArrayList<View>views = new ArrayList<>();
    private ImageView[]imageViews;
    private int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_start);

        initview();
        initpoint();
    }

    // load guide
    private void initview(){
        mviewPager = (ViewPager) findViewById(R.id.first_start_viewpager);
        views.add(getLayoutInflater().inflate(R.layout.first_start_layout_1,null));
        views.add(getLayoutInflater().inflate(R.layout.first_start_layout_2,null));
        views.add(getLayoutInflater().inflate(R.layout.first_start_layout_3,null));
        mviewPager.addOnPageChangeListener(this);
        mviewPager.setAdapter(new ViewPagerAdapter());

    }

    private void initpoint(){
        LinearLayout point_layout = (LinearLayout) findViewById(R.id.point_layout);
        imageViews = new ImageView[views.size()];
        for (int i = 0;i<imageViews.length;i++){
            imageViews[i] = (ImageView) point_layout.getChildAt(i);
        }
        index = 0;
        imageViews[index].setImageResource(R.drawable.dot_white_100);
    }

    private void setPoint(int position){
        if (index<0||index == position||index>imageViews.length-1){
            return;
        }
        imageViews[index].setImageResource(R.drawable.dot_dark_100);
        imageViews[position].setImageResource(R.drawable.dot_white_100);
        index = position;
    }
    public void GoMainActivity(View v){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        this.finish();

    }

    class ViewPagerAdapter extends PagerAdapter{

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setPoint(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
