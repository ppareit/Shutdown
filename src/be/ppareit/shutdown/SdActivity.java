package be.ppareit.shutdown;

import android.app.Activity;
import android.content.res.Resources;
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

    private CountDownTimer countdownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long seconds = millisUntilFinished / 1000;
            Log.v(TAG, "Seconds remaining: " + seconds);
            Resources res = getResources();
            String shutdownMessage = (String) res.getText(R.string.shutdown_msg);
            String formatedShutdownMString = String.format(shutdownMessage, seconds);
            TextView msgView = (TextView) findViewById(R.id.shutdown_msg);
            msgView.setText(formatedShutdownMString);
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "Finished countdown, executing shutdown!");
            doShutdown();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "Created");

        RootTools.debugMode = false;

        SetShutdownView();

        // only truly startup when root allowed
        
        if (!RootTools.isRootAvailable()) {
            Log.w(TAG, "Root is not available");
            SetNoRootView();
            return;
        }
        Log.v(TAG, "Root is available");
         
        if (!RootTools.isAccessGiven()) {
            Log.w(TAG, "Root is available but access is not given");
            SetNoRootView();
            return;
        }
        Log.v(TAG, "Root access given");
        
        // ok, everything is fine, give startup

        countdownTimer.start();

    }

    private void SetShutdownView() {
        setContentView(R.layout.shutdown_layout);

        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Cancel clicked");
                countdownTimer.cancel();
                finish();
            }
        });

        Button rebootBtn = (Button) findViewById(R.id.reboot_btn);
        rebootBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Reboot clicked");
                countdownTimer.cancel();
                doReboot();
            }
        });

        Button shutdownBtn = (Button) findViewById(R.id.shutdownnow_btn);
        shutdownBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Shutdown clicked");
                countdownTimer.cancel();
                doShutdown();
            }
        });
    }

    private void SetNoRootView() {
        setContentView(R.layout.noroot_layout);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Cancel clicked");
                countdownTimer.cancel();
                finish();
            }
        });
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

}
