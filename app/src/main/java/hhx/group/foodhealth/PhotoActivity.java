package hhx.group.foodhealth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Enthalqy Huang on 2017/10/1.
 * Modified by Xiaoting Huang on 2017/10/05.
 * PhotoActivity, capture food image using camera and upload the image to Microsoft cognitive
 * service to recognize the food
 */

public class PhotoActivity extends AppCompatActivity {

    // bottom navigation view
    private BottomNavigationView navigation;
    // textView used to display message
    private TextView mTextMessage;

    // image file
    private File imageFile;
    // image path
    private String filePath;
    // imageView, used to display image
    private ImageView imageView;
    // button to confirm analysis result
    private Button buttonYes;
    private Button buttonNo;

    // use microsoft cognitive service
    private VisionServiceClient client;
    private String sub_key = "a028d821be0c49659692938a2792c095";

    private Context mContext;

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_CROP = 1;
    public static final int REQUEST_WRITE = 2;
    public static final String IMAGE_UNSPECIFIED = "image/*";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("Debug", "click home");
                    // change to MainActivity
                    intent.setClass(PhotoActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_camera:
                    // start camera
                    Log.d("Debug", "click camera");
                    showCamera();
                    return true;
                case R.id.navigation_explore:
                    intent.setClass(PhotoActivity.this, FoodDetail.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mContext = this;

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        // initial views
        mTextMessage = (TextView) findViewById(R.id.photo_text);
        imageView = (ImageView) findViewById(R.id.photo_activity);
        buttonYes = (Button) findViewById(R.id.button_yes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast("to detail page");
                Intent intent = new Intent(PhotoActivity.this, FoodDetail.class);
                startActivity(intent);
            }
        });
        buttonNo = (Button) findViewById(R.id.button_no);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCamera();
            }
        });
        navigation = (BottomNavigationView) findViewById(R.id.navigation_photo);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // initial client to request microsoft cognitive service
        if (client == null) {
            client = new MyVisionServiceRestClient(sub_key);
        }
        // set item camera to be selected
        navigation.setSelectedItemId(R.id.navigation_camera);
    }

    @Override
    public void onBackPressed() {
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

    // Requests the Camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    // Request write permission
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
        // store filePath
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
        if (resultCode == Activity.RESULT_CANCELED) {
            intent = new Intent(PhotoActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        switch (requestCode) {
            case REQUEST_CAMERA:
                cropImage(Uri.fromFile(imageFile));
                break;
            case REQUEST_CROP:
                imageView.setImageBitmap(getImage());
                doAnalysis();
                break;
        }
    }

    // get bitmap from filePath
    private Bitmap getImage() {
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // set sample size the compression rate of the image, 2 means the width and height will
        // both be 1/2 of the original image which means 1/4 total pixels of the original image
        options.inSampleSize = calculateInSampleSize(options, 200, 200);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filePath, options);

        return bitmap;
    }

    // calculate the sample size of the image
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // do analysis
    private void doAnalysis() {
        mTextMessage.setText("Analysing...");
        // set the button and navigation bar not clickable when doing analysing
        buttonYes.setEnabled(false);
        buttonNo.setEnabled(false);
        navigation.setEnabled(false);
        new MyRequest().execute();
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"Tags"};

        // get image stream
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Bitmap bitmap = getImage();
        if (output == null){
            return null;
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

            // request cognitive API
            AnalyzeResult result = client.analyzeImage(input, features);

            return gson.toJson(result);
        }
         /*
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        // request cognitive API
        AnalyzeResult result = client.analyzeImage(input, features);

        return gson.toJson(result);*/
    }

    // use asyncTask to do http request, avoid blocking the UI thread
    private class MyRequest extends AsyncTask<String, String, String> {
        private MyRequest() {

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return process();
            } catch (VisionServiceException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new Gson();
            MyAnalyzeResult result = gson.fromJson(s, MyAnalyzeResult.class);
            List<Tag> tags = result.tags;
            boolean findFood = false;

            // check whether the result contains a name that in food name list
            if (tags != null && tags.size() != 0) {
                List<String> name = Arrays.asList(FoodName.names);
                Log.d("Debug", name.toString());
                Collections.sort(tags);
                for (Tag tag : tags) {
                    Log.d("Debug", tag.name);
                    if (name.contains(tag.name)) {
                        findFood = true;
                        // if yes, ask the user to confirm
                        mTextMessage.setText("Is it " + tag.name + " ?");
                        SharedPreferences sharedPref = getSharedPreferences("storage", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("foodname", tag.name);
                        editor.commit();
                        break;
                    }
                }
            }

            if (!findFood) {
                // if not, print error message, and do not enable the operation button
                mTextMessage.setText("Sorry, we are unable to detect the food type, please explore manually");
            } else {
                buttonYes.setEnabled(true);
                buttonNo.setEnabled(true);
            }

            navigation.setEnabled(true);
        }
    }

    // used to make toast
    private void makeToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
