package hhx.group.foodhealth;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodDetail extends AppCompatActivity {

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ExpandableListView mExpandableListView;
    private ExpandableListAdapter mExpandableListAdapter;

    private ProgressBar mProgressBar;
    private ImageView mFoodImage;
    private TextView mfoodName;
    private TextView mEnergy;
    private TextView mEnergyDetail;
    private TextView mProtein;
    private TextView mFat;
    private TextView mCarbohydrates;
    private TextView mDietaryFiber;
    private RelativeLayout mMain;
    private LinearLayout mInputLayout;
    private EditText mInput;
    private Button mAdd;
    private Food selectedItem;

    private BottomNavigationView mBottomNav;

    private Context mContext;

    private List<Category> categoryList = new ArrayList<>();
    private Map<String, List<Food>> categoryToFoodMap = new HashMap<>();
    private Map<String, Food> foodMap = new HashMap<>();

    private Map<String, List<Food>> mExpandableListData = new HashMap<>();
    private List<String> mExpandableListTitle = new ArrayList<>();

    final Context context = this;
    private ImageLoader imageLoader;

    private SharedPreferences mPrefs;
    int uid;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("Debug", "click home");
                    // change to MainActivity
                    intent.setClass(FoodDetail.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_camera:
                    // start camera
                    Log.d("Debug", "click camera");
                    intent.setClass(FoodDetail.this, PhotoActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_explore:
                    return true;
            }
            return false;
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food_detail);

        mContext = this.context;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mExpandableListView = (ExpandableListView) findViewById(R.id.navList);
        mFoodImage = (ImageView) findViewById(R.id.food_image);
        mfoodName = (TextView) findViewById(R.id.food_name);
        mEnergy = (TextView) findViewById(R.id.energy);
        mEnergyDetail = (TextView) findViewById(R.id.energy_detail);
        mProtein = (TextView) findViewById(R.id.protein_detail);
        mFat = (TextView) findViewById(R.id.fat_detail);
        mCarbohydrates = (TextView) findViewById(R.id.carbohydrates_detail);
        mDietaryFiber = (TextView) findViewById(R.id.dietary_fiber_detail);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        mMain = (RelativeLayout) findViewById(R.id.main);
        mInputLayout = (LinearLayout) findViewById(R.id.add_meal);
        mInput = (EditText) findViewById(R.id.meal_input);
        mAdd = (Button) findViewById(R.id.add_meal_submit);

        mBottomNav.setSelectedItemId(R.id.navigation_explore);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(FoodDetail.this));

        mPrefs = getSharedPreferences("storage", Context.MODE_PRIVATE);

        uid = mPrefs.getInt("uid", 0);

        // get category data
        new GetCategory().execute("http://52.255.60.10/index.php/category");

        // set up navigation drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setupDrawer();

        mMain.requestFocus();

        mInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (! hasFocus){
                    mBottomNav.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mInputLayout.setBackgroundColor(0x00000000);
                } else {
                    mBottomNav.setVisibility(View.GONE);
                    mInputLayout.setBackgroundColor(Color.BLACK);
                }
            }
        });

        mMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mMain.requestFocus();

            }
        });


        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check for valid input
                if (!mInput.getText().toString().equals("")){
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_confirm);
                    dialog.setTitle("Add Meal");

                    TextView text = (TextView) dialog.findViewById(R.id.confirm_message);
                    text.setText("Are you sure you want to add " + mInput.getText().toString().trim() + "00g/mL of " + selectedItem.getName().toLowerCase() + " to your meal? ");

                    Button btnCancel = (Button) dialog.findViewById(R.id.confirm_cancel);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mMain.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    });

                    Button btnAdd = (Button) dialog.findViewById(R.id.confirm_add);
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mProgressBar.setVisibility(View.VISIBLE);
                            String foodid = selectedItem.getId();
                            String quantity = mInput.getText().toString();
                            // add record
                            new AddRecord().execute("http://52.255.60.10/index.php/addRecord/" + uid + "/" + foodid + "/" + quantity);

                        }
                    });

                    dialog.show();
                } else {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_notification);
                    dialog.setTitle("Add Meal");

                    TextView text = (TextView) dialog.findViewById(R.id.notification_message);
                    text.setText("Please enter a quantity! ");

                    Button dialogButton = (Button) dialog.findViewById(R.id.notification_ok);
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mInput.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        }
                    });

                    dialog.show();

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    private void addDrawerItems() {
        mExpandableListAdapter = new CustomExpandableListAdapter(this, mExpandableListTitle, mExpandableListData);
        mExpandableListView.setAdapter(mExpandableListAdapter);

        // load food detail when selected
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                selectedItem = mExpandableListData.get(mExpandableListTitle.get(groupPosition)).get(childPosition);

                imageLoader.displayImage("http://52.255.60.10/image/" + selectedItem.getImage(), mFoodImage);

                mfoodName.setText(selectedItem.getName());
                mEnergy.setText(String.valueOf(selectedItem.getEnergy()));
                mEnergyDetail.setText(selectedItem.getEnergy() + "KCal");
                mProtein.setText(selectedItem.getProtein() + "g");
                mFat.setText(selectedItem.getFat() + "g");
                mCarbohydrates.setText(selectedItem.getCarbohydrates() + "g");
                mDietaryFiber.setText(selectedItem.getDietary_fiber() + "g");

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // asyntask to get category data
    private class GetCategory extends AsyncTask<String,String, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                reader.close();
                String finalJson = buffer.toString();


                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("data");

                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = null;
                    try {
                        finalObject = parentArray.getJSONObject(i);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    Category category = null;
                    try {

                        String id = finalObject.getString("id");
                        String name = finalObject.getString("name").trim();
                        String image = finalObject.getString("image");


                        category = new Category(id, name.substring(0, 1).toUpperCase() + name.substring(1), image);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    categoryList.add(category);

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (categoryList.size() != 0){
                    new GetFood().execute("http://52.255.60.10/index.php/food");

            } else {
                Toast.makeText(mContext, "Please check your internet connection! ", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
            }

        }

    }

    // asyntask to get food data
    private class GetFood extends AsyncTask<String, String, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                reader.close();
                String finalJson = buffer.toString();


                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("data");

                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = null;
                    try {
                        finalObject = parentArray.getJSONObject(i);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    Food food = null;
                    try {

                        String id = finalObject.getString("id");
                        String cid = finalObject.getString("cid");
                        String name = finalObject.getString("name").trim();
                        String image = finalObject.getString("image");
                        double energy = finalObject.getDouble("energy");
                        double protein = finalObject.getDouble("protein");
                        double fat = finalObject.getDouble("fat");
                        double carbohydrates = finalObject.getDouble("carbohydrates");
                        double dietary_fiber = finalObject.getDouble("dietary_fiber");
                        String description = finalObject.getString("description");

                        food = new Food(id, cid, name.substring(0, 1).toUpperCase() + name.substring(1), image, energy, protein,
                                fat, carbohydrates, dietary_fiber, description);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    if(food != null){
                        foodMap.put(food.getName().toLowerCase(), food);

                        if(categoryToFoodMap.get(food.getCid()) != null){
                            categoryToFoodMap.get(food.getCid()).add(food);
                        } else {
                            List<Food> foodList = new ArrayList<>();
                            foodList.add(food);
                            categoryToFoodMap.put(food.getCid(), foodList);
                        }

                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (categoryList != null){
                for(int i = 0; i < categoryList.size(); i++) {
                    mExpandableListData.put(categoryList.get(i).getName(), categoryToFoodMap.get(categoryList.get(i).getId()));
                }
            }

            mExpandableListTitle = new ArrayList(mExpandableListData.keySet());

            addDrawerItems();
            setupDrawer();

            // check if navigated from camera activity
            String selectedFood = mPrefs.getString("foodname", null);

            if (selectedFood != null) {
                selectedItem = foodMap.get(selectedFood);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("foodname", null);
                editor.commit();
            } else if (mExpandableListData.get(mExpandableListTitle.get(0)) != null){
                // set the first item as default
                selectedItem = mExpandableListData.get(mExpandableListTitle.get(0)).get(0);

            }

            imageLoader.displayImage("http://52.255.60.10/image/" + selectedItem.getImage(), mFoodImage);

            mfoodName.setText(selectedItem.getName());
            mEnergy.setText(String.valueOf(selectedItem.getEnergy()));
            mEnergyDetail.setText(selectedItem.getEnergy() + "KCal");
            mProtein.setText(selectedItem.getProtein() + "g");
            mFat.setText(selectedItem.getFat() + "g");
            mCarbohydrates.setText(selectedItem.getCarbohydrates() + "g");
            mDietaryFiber.setText(selectedItem.getDietary_fiber() + "g");

            mDrawerLayout.closeDrawer(GravityCompat.START);
            mProgressBar.setVisibility(View.GONE);

        }

    }
    // asyntask to add record
    private class AddRecord extends AsyncTask<String, String, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                reader.close();
                String finalJson = buffer.toString();


                JSONObject parentObject = new JSONObject(finalJson);
                String info = parentObject.getString("info");

                if (info.equals("Request success")){
                    return true;
                } else {
                    return false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_notification);
            dialog.setTitle("Add Meal");

            TextView text = (TextView) dialog.findViewById(R.id.notification_message);

            if (result){
                text.setText("Meal has been successfully added! ");
                mInput.getText().clear();
                mMain.requestFocus();

            } else {
                text.setText("Meal adding failed. Please try again later! ");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }

            Button dialogButton = (Button) dialog.findViewById(R.id.notification_ok);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            mProgressBar.setVisibility(View.GONE);
            dialog.show();
        }

    }


}
