package be.ppareit.shutdown;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

// See if I can call a broadcast: ACTION_SYNC
// http://developer.android.com/reference/android/content/Intent.html#ACTION_SYNC
//
// Use FLAG_DIM_BEHIND

public class SdActivity extends Activity {

    static final String TAG = SdActivity.class.getSimpleName();

    private Button mRebootBtn;
    private Button mShutdownBtn;

    private CountDownTimer mCountdownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long seconds = millisUntilFinished / 1000;
            Log.v(TAG, "Seconds remaining: " + seconds);
            Resources res = getResources();
            String shutdownMessage = (String) res.getText(R.string.shutdown_msg);
            String formatedShutdownMString = String.format(shutdownMessage, seconds);
            TextView msgView = findView(R.id.shutdown_msg);
            msgView.setText(formatedShutdownMString);
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
            mRebootBtn.setText(R.string.checkroot);
            mRebootBtn.setEnabled(false);
            mShutdownBtn.setText(R.string.checkroot);
            mShutdownBtn.setEnabled(false);
        };

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "Doing the rootchecking in the background");
            return RootTools.isRootAvailable() && RootTools.isAccessGiven();
        }

        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "Finished root checking, result: " + result.toString());
            if (result) {
                mRebootBtn.setText(R.string.reboot);
                mRebootBtn.setEnabled(true);
                mShutdownBtn.setText(R.string.shutdownnow);
                mShutdownBtn.setEnabled(true);
                mCountdownTimer.start();
            } else {
                mRebootBtn.setText(R.string.noroot);
                mShutdownBtn.setText(R.string.noroot);
                setNoRootView();
            }
        };

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
            return;
        }
        CommandCapture shutdownCommand = new CommandCapture(0, "reboot -p");
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
            return;
        }
        CommandCapture rebootCommand = new CommandCapture(0, "reboot");
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
