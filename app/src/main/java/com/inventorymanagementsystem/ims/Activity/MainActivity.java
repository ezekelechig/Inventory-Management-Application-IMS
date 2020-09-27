package com.inventorymanagementsystem.ims.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.inventorymanagementsystem.ims.Fragment.DashboardFragment;
import com.inventorymanagementsystem.ims.Fragment.OrderFragment;
import com.inventorymanagementsystem.ims.R;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //Java Activity for HomePage - -  includes two swipeable views - Dashboard and Order Fragment

    SharedPreferences sharedPreferences;

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;

    com.getbase.floatingactionbutton.FloatingActionButton add_order, add_inventory, load_money;

    ParseUser parseUser;
    ParseInstallation installation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("IMSPreferences", 0);

        parseUser = ParseUser.getCurrentUser();
        if (parseUser != null && parseUser.getSessionToken() != null) {
            installation = ParseInstallation.getCurrentInstallation();
            installation.put("user_objectId", parseUser.getObjectId());
            installation.saveInBackground();
            //getUserDetailsFromParse();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        setPrefValues();
        findViewsById();
        setupActionbar();
        setUpProgressDialog();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.notifications:
                Intent notifications = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(notifications);
                break;

            case R.id.settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
                break;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpProgressDialog() {

    }

    private void findViewsById() {


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        add_order = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.add_order);
        add_inventory = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.add_inventory);
        load_money = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.load_money);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

//        Setup viewpager
        setupViewPager(viewPager);

//        Setup tab layout with view pager
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

//        on click listener for add order floating action button
        add_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add_order_intent = new Intent(MainActivity.this, AddOrderActivity.class);
                startActivity(add_order_intent);
            }
        });

//        on click listener for add to inventory floating action button
        add_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sharedPreferences.getBoolean("is_admin", false)) {
                    Intent add_inventory_intent = new Intent(MainActivity.this, AddInventoryActivity.class);
                    startActivity(add_inventory_intent);
                } else
                    Toast.makeText(getBaseContext(), "Only admin can add inventory", Toast.LENGTH_SHORT).show();

            }
        });

        load_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean("is_admin", false)) {
                    Toast.makeText(getBaseContext(), "Admin can not load money.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent add_inventory_intent = new Intent(MainActivity.this, LoadMoneyActivity.class);
                    startActivity(add_inventory_intent);
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DashboardFragment(), getString(R.string.fa_dashboard) + " Dashboard");
        adapter.addFragment(new OrderFragment(), getString(R.string.fa_clock) + " Order History");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);
        setTitle("IMS");
    }

    private void setPrefValues() {
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
