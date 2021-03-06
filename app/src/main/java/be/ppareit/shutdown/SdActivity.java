package be.ppareit.shutdown;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

// See if I can call a broadcast: ACTION_SYNC
// http://developer.android.com/reference/android/content/Intent.html#ACTION_SYNC
//
// Use FLAG_DIM_BEHIND

public class SdActivity extends Activity {

    static final String TAG = SdActivity.class.getSimpleName();

    private TextView mShutdownMsg;
    private Button mRebootBtn;
    private Button mShutdownBtn;

    private CountDownTimer mCountdownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long seconds = millisUntilFinished / 1000;
            Log.v(TAG, "Seconds remaining: " + seconds);
            Resources res = getResources();
            String shutdownMessage = (String) res.getText(R.string.shutdown_msg);
            String formattedShutdownMString = String.format(shutdownMessage, seconds);
            TextView msgView = findView(R.id.shutdown_msg);
            msgView.setText(formattedShutdownMString);
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "Finished countdown, executing shutdown!");
            doShutdown();
        }
    };

    private AsyncTask<Void, Void, Boolean> mRootCheckTask = new AsyncTask<Void, Void, Boolean>() {

        protected void onPreExecute() {
            Log.d(TAG, "Beginning the root checking task");
            mShutdownMsg.setText(R.string.checkroot);
            mRebootBtn.setEnabled(false);
            mShutdownBtn.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "Doing the rootchecking in the background");
            if (App.debugNotRooted() == true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
            return RootTools.isRootAvailable() && RootTools.isAccessGiven();
        }

        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "Finished root checking, result: " + result.toString());
            if (result) {
                mRebootBtn.setEnabled(true);
                mShutdownBtn.setEnabled(true);
                mShutdownMsg.setText(R.string.shutdown_msg);
                mCountdownTimer.start();
            } else {
                mRebootBtn.setText(R.string.noroot);
                mShutdownBtn.setText(R.string.noroot);
                setNoRootView();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "Created");

        RootTools.debugMode = false;

        setShutdownView();

        mRootCheckTask.execute();

    }

    private void setShutdownView() {
        setContentView(R.layout.shutdown_layout);

        mShutdownMsg = findView(R.id.shutdown_msg);

        Button cancelBtn = findView(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Cancel clicked");
                doCancel();
            }
        });

        mRebootBtn = findView(R.id.reboot_btn);
        mRebootBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Reboot clicked");
                mCountdownTimer.cancel();
                doReboot();
            }
        });

        mShutdownBtn = findView(R.id.shutdownnow_btn);
        mShutdownBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Shutdown clicked");
                mCountdownTimer.cancel();
                doShutdown();
            }
        });
    }

    private void setNoRootView() {
        setContentView(R.layout.noroot_layout);
        Button cancelBtn = findView(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Cancel clicked");
                doCancel();
            }
        });
    }

    protected void doCancel() {
        mRootCheckTask.cancel(true);
        mCountdownTimer.cancel();
        finish();
    }

    protected void doShutdown() {
        Log.v(TAG, "Shutting down the device");
        if (App.emulateShutdowns()) {
            Log.v(TAG, "Emulating a shutdown");
            Toast.makeText(this, "Emulating a shutdown", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (App.isSystemApp()) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            pm.reboot("-p");
            Log.e(TAG, "Failed to shutdown as system app, fallback to root method");
        }
        Command shutdownCommand = new Command(0, "sync", "sleep 1", "reboot -p");
        try {
            RootTools.getShell(true).add(shutdownCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doReboot() {
        Log.v(TAG, "Rebooting the device");
        if (App.emulateShutdowns()) {
            Log.v(TAG, "Emulating a reboot");
            Toast.makeText(this, "Emulating a reboot", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (App.isSystemApp()) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            pm.reboot("");
            Log.e(TAG, "Failed to reboot as system app, fallback to root method");
        }
        Command rebootCommand = new Command(0, "sync", "sleep 1", "reboot");
        try {
            RootTools.getShell(true).add(rebootCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(int id) {
        return (T) this.findViewById(id);
    }

}
