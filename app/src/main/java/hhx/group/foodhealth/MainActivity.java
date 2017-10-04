package hhx.group.foodhealth;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // fragments
    private HomeFragment homeFragment;
    private PhotoFragment photoFragment;
    private ExploreFragment exploreFragment;
    // record the last clicked tiem
    private MenuItem lastItem;
    private Context mContext;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("Debug", "click home");
                    if (lastItem.getItemId() == item.getItemId()) {
                        Log.d("Debug", "refresh home");
                        Toast.makeText(mContext, "refresh the home page", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Debug", "change to home");
                        lastItem = item;
                        changeFragment(homeFragment);
                    }
                    return true;
                case R.id.navigation_camera:
                    Log.d("Debug", "click camera");
                    if (lastItem.getItemId() == item.getItemId()) {
                        Log.d("Debug", "refresh camera");
                        Toast.makeText(mContext, "refresh the photo page", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Debug", "change to photo");
                        lastItem = item;
                        changeFragment(photoFragment);
                    }
                    return true;
                case R.id.navigation_explore:
                    Log.d("Debug", "click explore");
                    if (lastItem.getItemId() == item.getItemId()) {
                        Log.d("Debug", "refresh explore");
                        Toast.makeText(mContext, "refresh the explore page", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Debug", "change to explore");
                        lastItem = item;
                        changeFragment(exploreFragment);
                    }
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Debug", "MainActivity onCreate");
        // hide title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mContext = this;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        lastItem = navigation.getMenu().getItem(0);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //initial fragments
        initialFragments();
        // set default fragment to home fragment
        changeFragment(homeFragment);
    }

    // initial fragments
    private void initialFragments() {
        homeFragment = HomeFragment.newInstance("", "");
        photoFragment = PhotoFragment.newInstance("", "");
        exploreFragment = ExploreFragment.newInstance("", "");
    }

    private void changeFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.content, fragment)
                .attach(fragment).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

}
