package be.ppareit.shutdown;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author ppareit
 *
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        sContext = getApplicationContext();
    }

    /**
     * @return the Context of this application
     */
    public static Context getAppContext() {
        if (sContext == null)
            Log.e(TAG, "Global context not set");
        return sContext;
    }

    static public boolean emulateShutdowns() {
        Context context = App.getAppContext();
        Resources res = context.getResources();
        return res.getBoolean(R.bool.emulate_shutdowns);
    }

    static public boolean debugNotRooted() {
        Context context = getAppContext();
        Resources res = context.getResources();
        final boolean notrooted = res.getBoolean(R.bool.debug_notrooted);
        Log.d(TAG, "Setting debugstate of notrooted to: " + Boolean.toString(notrooted));
        return notrooted;
    }

    /**
     * @return true if this is the very first time this function is ever called
     */
    public static boolean isFirstTimeStarted() {
        Context context = getAppContext();
        if (context == null) {
            Log.e(TAG, "Failed to get a valid context");
            return false;
        }
        // @BUG: this might be called a couple of times on the first run and break
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firsttime = sp.getBoolean("is_firsttime_started", true);
        if (firsttime) {
            sp.edit().putBoolean("is_firsttime_started", false).apply();
        }
        return firsttime;
    }

    /**
     * Get the version name from the manifest.
     * 
     * @return The version as a String.
     */
    public static String getVersionName() {
        Context context = getAppContext();
        String packageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to find the name " + packageName + " in the package");
            return null;
        }
    }

    /**
     * Get the version code from the manifest.
     * 
     * @return The version as a int.
     */
    public static int getVersionCode() {
        Context context = getAppContext();
        String packageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to find the name " + packageName + " in the package");
            return -1;
        }
    }
}
