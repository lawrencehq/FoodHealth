package hhx.group.foodhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Enthalqy Huang on 2017/10/1.
 * MainActivity, display the records and total cal the user have taken today
 */

public class MainActivity extends AppCompatActivity {

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("Debug", "click home");
                    Log.d("Debug", "refresh home");
                    makeToast("refresh the home page");
                    display();
                    return true;
                case R.id.navigation_camera:
                    Log.d("Debug", "click camera");
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, PhotoActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_explore:
                    Log.d("Debug", "click explore");

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
        setContentView(R.layout.activity_main);
        mContext = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        cal = (TextView) findViewById(R.id.textView);
        // initial listView, set adapter
        listView = (ListView) findViewById(R.id.listView);
        adapter = new SimpleAdapter(this, recordData, R.layout.list_item,
                new String[] {"name", "protein", "fat", "carbo", "fiber", "energy", "quan"},
                new int[] {R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5, R.id.text6, R.id.text7});
        listView.setAdapter(adapter);

        // display user info
        display();
    }

    private void display() {
        // get SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("storage", Context.MODE_PRIVATE);
        int uid = sharedPref.getInt("uid", 0);
        // if uid exists, refresh the view, else create user
        if (uid == 0) {
            new MyRequestRegister().execute();
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
        SharedPreferences sharedPref = getSharedPreferences("storage", Context.MODE_PRIVATE);
        int uid = sharedPref.getInt("uid", 0);

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
            title.put("name", "name");
            title.put("protein", "protein");
            title.put("fat", "fat");
            title.put("carbo", "carbo");
            title.put("fiber", "fiber");
            title.put("energy", "energy");
            title.put("quan", "quan");
            recordData.add(title);
            // put data into list array for listView to display
            for (Record record : records.data) {
                // record data
                HashMap<String, String> map = new HashMap<>();
                map.put("name", record.name);
                map.put("protein", record.protein+"");
                map.put("fat", record.fat+"");
                map.put("carbo", record.carbohydrates+"");
                map.put("fiber", record.dietary_fiber+"");
                map.put("energy", record.energy+"");
                map.put("quan", record.quantity+"");
                recordData.add(map);
                totalCal += record.energy * record.quantity;
            }

            return totalCal;
        }


        return 0;
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
                    SharedPreferences sharedPref = getSharedPreferences("storage", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("uid", uid);
                    editor.commit();
                } else {
                    makeToast("Register fail");
                }
                return uid+"";
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
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
                    makeToast("No records found");
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
            }
        }
    }

    // make toast
    private void makeToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }


}
