package kaist.hcil.magtouch;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.WindowManager;

import kaist.hcil.magtouchlibrary.fragment.VisualizeFragment;
import kaist.hcil.magtouchlibrary.fragment.demo.BasicDemoFragment;
import kaist.hcil.magtouchlibrary.fragment.FileManageFragment;
import kaist.hcil.magtouchlibrary.fragment.MagSensorFragment;
import kaist.hcil.magtouchlibrary.fragment.demo.ContactsAppFragment;
import kaist.hcil.magtouchlibrary.fragment.demo.MailAppFragment;
import kaist.hcil.magtouchlibrary.fragment.MakeModelFragment;
import kaist.hcil.magtouchlibrary.fragment.OrientationFragment;
import kaist.hcil.magtouchlibrary.fragment.SystemStatFragment;
import kaist.hcil.magtouchlibrary.fragment.TestFragment;
import kaist.hcil.magtouchlibrary.fragment.TrainFragment;
import kaist.hcil.magtouchlibrary.fragment.core.TargetFragment;

public class AppActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        String appName = bundle.getString(MainActivity.APP_NAME);
        openApp(appName);
    }

    @Override
    protected void onPause() {

        super.onPause();
        finish();
    }

    private void openApp(String selectedApp)
    {
        if(selectedApp.isEmpty())
        {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        if(selectedApp.equals(MainActivity.TRAIN))
        {
            fragment = TrainFragment.newInstance(TargetFragment.CIRCLE, TargetFragment.smallRadius);
        }
        if(selectedApp.equals(MainActivity.VIS))
        {
            fragment = VisualizeFragment.newInstance();
        }
        if(selectedApp.equals(MainActivity.TEST))
        {
            fragment = TestFragment.newInstance(TargetFragment.CIRCLE, TargetFragment.smallRadius);
        }
        if(selectedApp.equals(MainActivity.MAKE_MODEL))
        {
            fragment = MakeModelFragment.newInstance();
        }
        if(selectedApp.equals(MainActivity.FILE))
        {
            fragment = FileManageFragment.newInstance();
        }
        if(selectedApp.equals(MainActivity.MAG_STAT))
        {
            fragment = MagSensorFragment.newInstance();
        }
        if(selectedApp.equals(MainActivity.ORIENTATION))
        {
            fragment = OrientationFragment.newInstance();
        }
        if(selectedApp.equals(MainActivity.SYSTEM))
        {
            fragment = SystemStatFragment.newInstance();
        }

        if(selectedApp.equals(MainActivity.BASIC_DEMO))
        {
            fragment = BasicDemoFragment.newInstance(TargetFragment.smallRadius);
        }

        if(selectedApp.equals(MainActivity.MAIL_APP))
        {
            fragment = MailAppFragment.newInstance();
        }

        if(selectedApp.equals(MainActivity.CONTACTS_APP))
        {
            fragment = ContactsAppFragment.newInstance();
        }

        if(fragment != null)
        {
            fragmentTransaction.add(R.id.app_fragment_container, fragment);
            fragmentTransaction.commit();
        }
    }

}
