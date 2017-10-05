package hhx.group.foodhealth;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // fragments
    private HomeFragment homeFragment;
    private PhotoFragment photoFragment;
    private ExploreFragment exploreFragment;
    // record the last clicked item
    private MenuItem lastItem;
    private Context mContext;
    // image file
    private File imageFile;
    private String filePath;

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_CROP = 1;
    public static final int REQUEST_WRITE = 2;
    public static final String IMAGE_UNSPECIFIED = "image/*";

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
                    changeFragment(photoFragment);
                    showCamera();

//                    if (lastItem.getItemId() == item.getItemId()) {
//                        Log.d("Debug", "refresh camera");
//                        Toast.makeText(mContext, "refresh the photo page", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.d("Debug", "change to photo");
//                        lastItem = item;
//                        changeFragment(photoFragment);
//                    }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

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

    // change to target fragment
    private void changeFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.content, fragment)
                .attach(fragment).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    private void showCamera() {
        Log.d("Debug", "Call show camera, check permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted, request for permission
            Log.d("Debug", "Permission nor granted, request for permission");
            requestCameraPermission();
            startCamera();
        } else {
            Log.d("Debug", "Permission already granted");
            startCamera();
        }
    }

    /**
     * Requests the Camera permission
     *
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
    }

    private void startCamera() {
        // create file to store the image
        Log.d("Debug", "Call start camera, check storage permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted, request for permission
            requestWritePermission();
        }
        createImageFile();
        if (!imageFile.exists()) {
            return;
        }
        // start camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    // create image file
    private void createImageFile() {
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
        imageFile = new File(filePath);
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Unable to create image file.", Toast.LENGTH_SHORT).show();
        }
    }

    // cut the image using system library
    private void cropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, REQUEST_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_CAMERA:
                cropImage(Uri.fromFile(imageFile));
                break;
            case REQUEST_CROP:
                //changeFragment(photoFragment);
                Intent broadcast = new Intent("com.getImage");
                broadcast.putExtra("image_path", filePath);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(broadcast);
                Log.d("Debug", "send broadcast");
                break;
        }
    }



}
