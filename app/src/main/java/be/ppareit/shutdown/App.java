package be.ppareit.shutdown;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
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

    static public boolean isSystemApp() {
        try {
            Context context = getAppContext();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            // First check if it is preloaded.
            // If yes then check if it is System app or not.
            if (ai == null || (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0)
                return false;
            Log.v(TAG, "Installed as system application");
            // get my app signature
            PackageInfo appInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (appInfo == null && appInfo.signatures == null)
                return false;
            // get framework signature
            PackageInfo sysInfo = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            if (sysInfo == null && sysInfo.signatures == null)
                return false;
            // compare both signatures
            boolean systemApp = sysInfo.signatures[0].equals(appInfo.signatures[0]);
            if (!systemApp) Log.v(TAG, "Incorrect signature as system application");
            return systemApp;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * In release this will always return false, the expected behavior.
     *
     * @return true when debugging and when flag to emulate shutdowns is set
     */
    static public boolean emulateShutdowns() {
        Context context = App.getAppContext();
        Resources res = context.getResources();
        return BuildConfig.DEBUG && res.getBoolean(R.bool.emulate_shutdowns);
    }

    /**
     * In release this will always return false, the expected behavior.
     *
     * @return true when debugging and when flag to emulate no root is set
     */
    static public boolean debugNotRooted() {
        Context context = getAppContext();
        Resources res = context.getResources();
        return BuildConfig.DEBUG && res.getBoolean(R.bool.debug_notrooted);
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
