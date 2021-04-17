package sjtu.iiot.wi_fi_scanner_iiot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.view.View;

import java.util.Vector;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    private SuperWiFi rss_scan = null;
    Vector<String> RSSList = null;
    private String testlist = null;
    public static int testID = 0; // The ID of the test result
    private double positionX, positionY;
    private boolean has_scan = false;
    private boolean[] isValid;
    private double[] radius;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText ipText = findViewById(R.id.ipText); // The textlist of the average of the result
        final Button changactivity = findViewById(R.id.button1); // The start button
        final Button cleanlist = findViewById(R.id.button2); // Clear the textlist
        final Button visualize = findViewById(R.id.button3); // Visualize the final result
        verifyStoragePermissions(this);
        rss_scan = new SuperWiFi(this);
        testlist = "";
        testID = 0;
        changactivity.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                testID = testID + 1;
                rss_scan.ScanRss();
                while (rss_scan.isscan()) ;
                RSSList = rss_scan.getRSSlist(); // Get the test result
                final EditText ipText = (EditText) findViewById(R.id.ipText);
                testlist = "testID:" + testID + "\n" + RSSList.toString() + "\n";
                // Positioning
                if (rss_scan.isvalid()) {
                    has_scan = true;
                    positionX = rss_scan.getPositionX();
                    positionY = rss_scan.getPositionY();
                    isValid = rss_scan.getIsValid();
                    radius = rss_scan.getRadius();
                    String position = "position: (" + positionX + "," + positionY + ")" + "\n";
                    testlist = testlist + position;
                }
                ipText.setText(testlist);
            }
        });
        cleanlist.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                testlist = "";
                ipText.setText(testlist); // Clear the textlist
                testID = 0;
            }
        });
        visualize.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (has_scan) {
                    Intent intent = new Intent(MainActivity.this, Visualization.class);
                    intent.putExtra("positionX", positionX);
                    intent.putExtra("positionY", positionY);
                    intent.putExtra("isValid", isValid);
                    intent.putExtra("radius", radius);
                    startActivity(intent);
                }
            }
        });
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION};

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}