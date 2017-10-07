package hhx.group.foodhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Enthalqy Huang on 2017/10/1.
 * Modified by Xiaoting Huang on 2017/10/07. 
 * MainActivity, display the records and total cal the user have taken today
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Context mContext;
    // address of the server
    private String baseUrl = "http://52.255.60.10/index.php/";
    private final OkHttpClient httpClient = new OkHttpClient();
    // textView to display statistic info
    private TextView cal;
    // listView for records
    private ListView listView;
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> recordData = new ArrayList<>();

    private TextView mEnergyCons;

    SensorManager sensorManager;
    private static String CURRENT_DATE;
    private int today_count;
    private int current_system_count;
    private int last_system_count;
    String lastRecord;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    final double STEP_LENGTH = 0.75;
    final int M_PER_KM = 1000;
    final int CALORIES_BURN = 50;

    private SharedPreferences mPref;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("Debug", "click home");
                    Log.d("Debug", "refresh home");
                    makeToast("refresh the home page");
                    display();
                    mEnergyCons.setText(String.format( "%.2f", String.valueOf(today_count * STEP_LENGTH / M_PER_KM * CALORIES_BURN)));
                    return true;
                case R.id.navigation_camera:
                    Log.d("Debug", "click camera");
                    intent.setClass(MainActivity.this, PhotoActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_explore:
                    Log.d("Debug", "click explore");
                    intent.setClass(MainActivity.this, FoodDetail.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Debug", "MainActivity onCreate");
        setContentView(R.layout.activity_main);
        mContext = this;
        mPref = getSharedPreferences("storage", Context.MODE_PRIVATE);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mEnergyCons = (TextView) findViewById(R.id.energy_consumption);
        cal = (TextView) findViewById(R.id.textView);

        // initial listView, set adapter
        listView = (ListView) findViewById(R.id.listView);
        adapter = new SimpleAdapter(this, recordData, R.layout.list_item,
                new String[] {"name", "quantity", "protein", "fat", "carbo", "fiber", "energy"},
                new int[] {R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5, R.id.text6, R.id.text7});
        listView.setAdapter(adapter);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(MainActivity.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Sensor not found! ", Toast.LENGTH_SHORT).show();
        }

        // display user info
        display();


        initTodayData();

        mEnergyCons.setText(String.format( "%.2f", today_count * STEP_LENGTH / M_PER_KM * CALORIES_BURN));





    }

    private void initTodayData() {

        Date today = Calendar.getInstance().getTime();
        CURRENT_DATE = sdf.format(today);

        lastRecord = mPref.getString("record_date", "");
        if (lastRecord.equals(CURRENT_DATE)){
            today_count = mPref.getInt("step_today", 0);
        } else {
            today_count = 0;
        }


    }


    @Override
    public void onBackPressed() {
    }


    private void display() {
        // get SharedPreferences
        int uid = mPref.getInt("uid", 0);
        // if uid exists, refresh the view, else create user
        if (uid == 0) {
            new MyRequestRegister().execute();
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("step_history", current_system_count);
            editor.putInt("step_today", 0);
            Date date = Calendar.getInstance().getTime();
            editor.putString("record_date", sdf.format(date));
            editor.commit();

        } else {
            new MyRequestGetRecord().execute();
        }
    }

    // register user
    private int register(String username) throws IOException, JSONException {
        Request request = new Request.Builder().url(baseUrl + "addUser/"+username+"/1/175/75").build();
        Log.d("Debug", baseUrl+username+"/1/175/75");

        Response response = httpClient.newCall(request).execute();
        JSONObject object = new JSONObject(response.body().string());
        if (object.getBoolean("status")) {
            return object.getInt("data");
        } else {
            return 0;
        }
    }

    // get total cal the user have taken today
    private int getTotalCal() throws IOException, JSONException {
        // get format date of today
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date());
        // get user id
        int uid = mPref.getInt("uid", 0);

        Request request = new Request.Builder().url(baseUrl+"getRecords/" + date + "/" + uid).build();

        Response response = httpClient.newCall(request).execute();
        String result = response.body().string();
        Log.d("Debug", result);
        JSONObject object = new JSONObject(result);
        if (object.getBoolean("status")) {
            int totalCal = 0;
            Gson gson = new Gson();
            Records records = gson.fromJson(result, Records.class);
            recordData.clear();
            // title of each field
            HashMap<String, String> title = new HashMap<>();
            title.put("name", "Name");
            title.put("protein", "Protein");
            title.put("fat", "Fat");
            title.put("carbo", "Carbo");
            title.put("fiber", "Fiber");
            title.put("energy", "Energy");
            title.put("quantity", "Quantity");
            recordData.add(title);
            // put data into list array for listView to display
            for (Record record : records.data) {
                // record data
                HashMap<String, String> map = new HashMap<>();
                map.put("name", record.name.substring(0,1).toUpperCase() + record.name.substring(1));
                map.put("protein", record.protein+"");
                map.put("fat", record.fat+"");
                map.put("carbo", record.carbohydrates+"");
                map.put("fiber", record.dietary_fiber+"");
                map.put("energy", record.energy+"");
                map.put("quantity", record.quantity+"");
                recordData.add(map);
                totalCal += record.energy * record.quantity;
            }

            return totalCal;
        }


        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Sensor not found! ", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        current_system_count = (int) event.values[0];
        last_system_count = mPref.getInt("step_history", 0);
        int step;
        if (lastRecord.equals(CURRENT_DATE)) {

            if (current_system_count < last_system_count){
                step = current_system_count + last_system_count;
            } else {
                step = current_system_count - last_system_count + today_count;
            }
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("step_history", current_system_count);
            editor.putInt("step_today", step);
            editor.commit();
        } else {
            step = 0;
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("step_history", current_system_count);
            editor.putInt("step_today", step);
            editor.putString("record_date", CURRENT_DATE);
            editor.commit();

        }




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // use asynctask to do http request
    private class MyRequestRegister extends AsyncTask<String, String, String> {

        public MyRequestRegister() {

        }

        @Override
        protected String doInBackground(String... params) {
            // register user, use a random UUID as the username
            String str = UUID.randomUUID().toString();
            String username = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
            try {
                int uid = register(username);
                if (uid != 0) {
                    // store uid
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("uid", uid);
                    editor.commit();

                } else {
                    return null;
                }
                return uid+"";
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null){
                makeToast("Register fail");
            }
        }

    }

    // use asynctask to do http request
    private class MyRequestGetRecord extends AsyncTask<String, String, String> {

        public MyRequestGetRecord() {

        }


        @Override
        protected String doInBackground(String... params) {
            try {
                int total = getTotalCal();
                if (total != 0) {
                    return total+"";
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                cal.setText(s);
                // refresh listView
                adapter.notifyDataSetChanged();
            } else {
                cal.setText("0");
                makeToast("No records found");
            }
        }
    }

    // make toast
    private void makeToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }


}
