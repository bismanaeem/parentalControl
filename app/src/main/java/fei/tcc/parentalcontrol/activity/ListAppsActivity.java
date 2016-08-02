package fei.tcc.parentalcontrol.activity;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fei.tcc.parentalcontrol.R;
import fei.tcc.parentalcontrol.adapter.SelectableAdapter;
import fei.tcc.parentalcontrol.vo.AppVo;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;

public class ListAppsActivity extends AppCompatActivity {

    private static final String TAG = ListAppsActivity.class.getSimpleName();

    private UsageStatsManager mUsageStatsManager;

    private Button mOpenUsageSettingButton;

    private ListView appListView;

    private SelectableAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        mUsageStatsManager = (UsageStatsManager) this.getSystemService("usagestats"); //Context.USAGE_STATS_SERVICE

        mOpenUsageSettingButton = (Button) findViewById(R.id.button_open_usage_setting);

        appListView = (ListView) findViewById(R.id.list_apps);

        appListView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);

        // Obtain the installed apps in system
        List<AppVo> apps = getInstalledApps();

        sAdapter = new SelectableAdapter(this, android.R.layout.simple_list_item_multiple_choice, apps);

        appListView.setAdapter(sAdapter);

        //selectAllItemsByDefault(apps);

    }

    /**
     * Select all apps passed
     *
     * @param apps apps to be selected
     */
    private void selectAllItemsByDefault(List<AppVo> apps) {
        for (int i = 0; i < apps.size(); i++) {
            appListView.setItemChecked(i, true);
        }
    }

    /**
     * Use PackageManager to get the list of installed apps
     *
     * @return list of apps installed
     */
    @NonNull
    private List<AppVo> getInstalledApps() {

        // get app list of used apps
//        List<ApplicationInfo> installedApplications = pm.getInstalledApplications(GET_META_DATA);
//        for (ApplicationInfo ai : installedApplications) {
//            String appName = pm.getApplicationLabel(ai).toString();
//            Drawable appIcon = pm.getApplicationIcon(ai);
//
//            AppVo appVo = new AppVo();
//            appVo.setName(appName);
//            appVo.setIcon(appIcon);
//
//            apps.add(appVo);
//        }

        // PackageManager to get info about apps
        PackageManager pm = this.getPackageManager();

        // Get the info about usage of the apps
        List<UsageStats> usageStatistics = getUsageStatistics(UsageStatsManager.INTERVAL_YEARLY);

        List<AppVo> apps = new ArrayList<>();
        for (UsageStats usageStat : usageStatistics) {
            AppVo app = new AppVo();

            String packageName = usageStat.getPackageName();

            try {
                // TODO what is zero?
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                Drawable applicationIcon = pm.getApplicationIcon(applicationInfo);
                String applicationName = pm.getApplicationLabel(applicationInfo).toString();

                app.setName(applicationName);
                app.setIcon(applicationIcon);
                app.setUsageStats(usageStat);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "ApplicationInfo not found with package " + packageName);
                e.printStackTrace();
            }

            apps.add(app);
        }

        return apps;
    }

    public List<UsageStats> getUsageStatistics(int intervalType) {
        // Get the app statistics since one year ago from the current time.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (queryUsageStats.size() == 0) {
            Log.i(TAG, "The user may not allow the acscess to apps usage. ");
            Toast.makeText(this,
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }

        return queryUsageStats;
    }
}
