package kaist.hcil.magtouch;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;

public class MainActivity extends WearableActivity {

    public static String APP_NAME = "app_name";

    public static String TRAIN = "Train";
    public static String MAKE_MODEL = "Make Model";
    public static String BASIC_DEMO = "Basic Demo";
    public static String TEST = "Test";
    public static String MAIL_APP = "Mail App";
    public static String CONTACTS_APP = "Contacts App";
    public static String RECORD = "Record";
    public static String ORIENTATION = "Orientation";
    public static String VIS = "Visualize";
    public static String FILE = "File Manage";
    public static String SYSTEM = "System Stat";
    public static String SITU_TEST = "Situ Test";
    public static String MAG_STAT = "Mag Stat";

    private WearableRecyclerView recyclerView;

    private Spinner appSpinner;
    private ArrayList<String> appList;
    private String selectedApp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        appList = new ArrayList<>();
        appList.add(TRAIN);
        appList.add(MAKE_MODEL);
        appList.add(TEST);
        appList.add(SITU_TEST);
        appList.add(BASIC_DEMO);
        appList.add(MAIL_APP);
        appList.add(CONTACTS_APP);
        appList.add(FILE);
        appList.add(MAG_STAT);
        appList.add(RECORD);
        appList.add(VIS);
        appList.add(ORIENTATION);
        appList.add(SYSTEM);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        MainMenuAdapter adapter = new MainMenuAdapter(appList, new MainMenuAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(int idx) {
                String selected = appList.get(idx);
                openApp(selected);
            }
        });
        recyclerView.setAdapter(adapter);

    }

    private void openApp(String selectedApp) {
        if(selectedApp.isEmpty())
        {
            return;
        }

        Intent intent = new Intent(this, AppActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(APP_NAME, selectedApp);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
