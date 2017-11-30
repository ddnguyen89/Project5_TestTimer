package nguyen.testtimer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView timeTV;
    private TextView downloadTV;

    private Timer timer;
    private Timer timer2;
    private long startMillis;
    private long elapsedMillis;
    private int downloadCount;

    private SharedPreferences savedValues;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        timeTV = (TextView) findViewById(R.id.timeTV);
        downloadTV = (TextView) findViewById(R.id.downloadTV);

        savedValues = getSharedPreferences("savedValues", MODE_PRIVATE);

        savedValues.edit().clear().commit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTimer();

        Editor edit = savedValues.edit();

        edit.putLong("startMillis", startMillis);
        edit.putLong("elapsedMillis", elapsedMillis);
        edit.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startMillis = savedValues.getLong("startMillis", System.currentTimeMillis());
        elapsedMillis = savedValues.getLong("elapsedMillis", 0);

        updateView(elapsedMillis, downloadCount);
    }

    private void startTimer() {
        onResume();
        startMillis = System.currentTimeMillis() - elapsedMillis;

        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                elapsedMillis = System.currentTimeMillis() - startMillis;
                updateView(elapsedMillis, downloadCount);
            }
        };
        timer.schedule(task, 0, 1000);

        timer2 = new Timer(true);
        TimerTask task2 = new TimerTask() {

            @Override
            public void run() {
                downloadFile();
                downloadCount += 1;
                updateView(elapsedMillis, downloadCount);
            }
        };
        timer2.schedule(task2, 10000, 10000);
    }

    private void updateView(final long elapsedMillis, final int downloadCount) {
        // UI changes need to be run on the UI thread
        timeTV.post(new Runnable() {

            int elapsedSeconds = (int) elapsedMillis/1000;

            @Override
            public void run() {
                timeTV.setText("Seconds: " + elapsedSeconds);
                downloadTV.setText("File downloaded: " + downloadCount +  " time(s)");
            }
        });
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer2.cancel();
        }
    }

    public void startClick(View view) {
        onPause();
        startTimer();
    }

    public void stopClick(View view) {
        onPause();
    }

    public void downloadFile() {
        try{
            // get the URL
            URL url = new URL("http://rss.cnn.com/rss/cnn_tech.rss");

            // get the input stream
            InputStream in = url.openStream();

            // get the output stream
            FileOutputStream out = openFileOutput("news_feed.xml", MODE_PRIVATE);

            // read input and write output
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            while (bytesRead != -1)
            {
                out.write(buffer, 0, bytesRead);
                bytesRead = in.read(buffer);
            }
            out.close();
            in.close();
        }
        catch (IOException e) {
            Log.e("News reader", e.toString());
        }
    }
}