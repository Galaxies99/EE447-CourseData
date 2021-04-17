package sjtu.iiot.wi_fi_scanner_iiot;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log; //Log can be utilized for debug.

public class SuperWiFi extends MainActivity {
    /*****************************************************************************************************************
     * When you run the APP in your mobile phone, you can utilize the following code for debug:
     * Log.d("TEST_INFO","Your Own String Type Content Here");
     * You can also generate the String via ("String" + int/double value). for example, "CurTime " + 20 = "CurTime 20"
     * ***************************************************************************************************************/
    private final String FileLabelName = "Galaxies";// Define the file Name
    /*****************************************************************************************************************
     * You can define the Wi-Fi SSID to be measured in FileNameGroup, more than 2 SSIDs are OK.
     * It is noting that multiple Wi-Fi APs might share the same SSID such as SJTU.
     * ***************************************************************************************************************/
    private final String[] FileNameGroup = {"SJTU", "406", "408", "507", "508"};
    private final int[] A_WiFi = {60, 60, 60, 60, 60};
    private double[] x_WiFi = {0, -3.6, 4.5, 2.3, 3.2};
    private double[] y_WiFi = {0, 0.4, -2.3, 1.9, -5.2};

    private final int TestTime = 10;//Number of measurement

    private final int NumberOfWiFi = FileNameGroup.length;

    // RSS_Value_Record and RSS_Measurement_Number_Record are used to record RSSI values
    private final int[] RSS_Value_Record = new int[NumberOfWiFi];
    private final int[] RSS_Measurement_Number_Record = new int[NumberOfWiFi];
    private final boolean[] isValid = new boolean[NumberOfWiFi];
    private final double[] dist_WiFi = new double[NumberOfWiFi];
    private final double[] radius = new double[NumberOfWiFi];
    private final double n_Path_Loss_Coeff = 3.25;
    private int ValidWifiNumber = 0;

    // Positioning Algorithm Variables
    private double positionX, positionY;
    private final double epsilon = 1e-8;
    private final double eta = 1e-3;


    private WifiManager mWiFiManager = null;
    private Vector<String> scanned = null;
    boolean isScanning = false;

    public SuperWiFi(Context context) {
        this.mWiFiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.scanned = new Vector<String>();
    }

    private void startScan() {
        this.isScanning = true;
        Thread scanThread = new Thread(new Runnable() {
            public void run() {
                scanned.clear();//Clear last result
                for (int index = 1; index <= NumberOfWiFi; index++) {
                    RSS_Value_Record[index - 1] = 0;
                    RSS_Measurement_Number_Record[index - 1] = 1;
                }
                int CurTestTime = 1; //Record the test time and write into the SD card
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat
                        ("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis()); //Get the current time
                String CurTimeString = formatter.format(curDate);
                for (int index = 1; index <= NumberOfWiFi; index++) {
                    write2file(FileLabelName + "-" + FileNameGroup[index - 1] + ".txt", "Test_ID: " + testID + " TestTime: " + CurTimeString + " BEGIN\r\n");
                }

                while (CurTestTime++ <= TestTime) performScan();

                for (int index = 1; index <= NumberOfWiFi; index++)
                    scanned.add(FileLabelName + "-" + FileNameGroup[index - 1] + " = " + RSS_Value_Record[index - 1] / RSS_Measurement_Number_Record[index - 1] + "\r\n");

                // Localization
                ValidWifiNumber = 0;
                for (int index = 1; index <= NumberOfWiFi; index++) {
                    RSS_Value_Record[index - 1] /= RSS_Measurement_Number_Record[index - 1];
                    isValid[index - 1] = RSS_Value_Record[index - 1] != 0;
                    if (isValid[index - 1])
                        ValidWifiNumber ++;
                }

                if (ValidWifiNumber >= 3) {
                    // Calculating Distance
                    for (int index = 1; index <= NumberOfWiFi; index++) {
                        if (isValid[index - 1])
                            dist_WiFi[index - 1] = Math.pow(10, (-1 * RSS_Value_Record[index - 1] - A_WiFi[index - 1]) / (10 * n_Path_Loss_Coeff));
                    }
                    // Positioning: Using Gradient Descent Algorithm to Solve the Optimization
                    Random rand = new Random();
                    positionX = (rand.nextDouble() - 0.5) * 20;
                    positionY = (rand.nextDouble() - 0.5) * 20;
                    double loss = 0;
                    for (int index = 1; index <= NumberOfWiFi; index ++) {
                        if (isValid[index - 1]) {
                            double delta_x = (x_WiFi[index - 1] - positionX) * (x_WiFi[index - 1] - positionX);
                            double delta_y = (y_WiFi[index - 1] - positionY) * (y_WiFi[index - 1] - positionY);
                            loss += (delta_x + delta_y - dist_WiFi[index - 1] * dist_WiFi[index - 1]) * (delta_x + delta_y - dist_WiFi[index - 1] * dist_WiFi[index - 1]);
                        }
                    }
                    loss = loss / ValidWifiNumber;
                    double last_loss = 0;
                    while (Math.abs(loss - last_loss) > epsilon) {
                        // Updating X, Y
                        double gradientX = 0, gradientY = 0;
                        for (int index = 1; index <= NumberOfWiFi; index++) {
                            if (isValid[index - 1]) {
                                double delta_x = positionX - x_WiFi[index - 1];
                                double delta_y = positionY - y_WiFi[index - 1];
                                double dist = dist_WiFi[index - 1];
                                gradientX += 4 * delta_x * delta_x * delta_x - 4 * dist * dist * delta_x +
                                        4 * delta_y * delta_y * delta_x;
                                gradientY += 4 * delta_y * delta_y * delta_y - 4 * dist * dist * delta_y +
                                        4 * delta_x * delta_x * delta_y;
                            }
                        }

                        gradientX = gradientX / ValidWifiNumber;
                        gradientY = gradientY / ValidWifiNumber;
                        Log.d("INFO", " " + positionX + " " + positionY + " " + loss + " " + gradientX + " " + gradientY);
                        positionX = positionX - eta * gradientX;
                        positionY = positionY - eta * gradientY;

                        // Calculate new loss
                        last_loss = loss;
                        loss = 0;
                        for (int index = 1; index <= NumberOfWiFi; index++) {
                            if (isValid[index - 1]) {
                                double delta_x = (x_WiFi[index - 1] - positionX) * (x_WiFi[index - 1] - positionX);
                                double delta_y = (y_WiFi[index - 1] - positionY) * (y_WiFi[index - 1] - positionY);
                                loss += (delta_x + delta_y - dist_WiFi[index - 1] * dist_WiFi[index - 1]) * (delta_x + delta_y - dist_WiFi[index - 1] * dist_WiFi[index - 1]);
                            }
                        }
                        loss = loss / ValidWifiNumber;
                    }
                    for (int index = 1; index <= NumberOfWiFi; index ++) {
                        if (isValid[index - 1]) {
                            double delta_x = (x_WiFi[index - 1] - positionX) * (x_WiFi[index - 1] - positionX);
                            double delta_y = (y_WiFi[index - 1] - positionY) * (y_WiFi[index - 1] - positionY);
                            radius[index - 1] = Math.sqrt(delta_x + delta_y);
                        }
                    }
                }



                for (int index = 1; index <= NumberOfWiFi; index++) {//Mark the end of the test in the file
                    write2file(FileLabelName + "-" + FileNameGroup[index - 1] + ".txt", "testID:" + testID + "END\r\n");
                }
                isScanning = false;
            }
        });
        scanThread.start();
    }

    private void performScan()//The realization of the test
    {
        if (mWiFiManager == null)
            return;
        try {
            if (!mWiFiManager.isWifiEnabled()) {
                mWiFiManager.setWifiEnabled(true);
            }
            mWiFiManager.startScan();//Start to scan
            try {
                //Wait for (?) ms for next scan
                int scanningTime = 1000;
                Thread.sleep(scanningTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.scanned.clear();
            List<ScanResult> sr = mWiFiManager.getScanResults();
            for (ScanResult ap : sr) {
                for (int index = 1; index <= FileNameGroup.length; index++) {
                    if (ap.SSID.equals(FileNameGroup[index - 1])) {//Write the result to the file
                        RSS_Value_Record[index - 1] = RSS_Value_Record[index - 1] + ap.level;
                        RSS_Measurement_Number_Record[index - 1]++;
                        write2file(FileLabelName + "-" + FileNameGroup[index - 1] + ".txt", ap.level + "\r\n");
                    }
                }
            }
        } catch (Exception e) {
            this.isScanning = false;
            this.scanned.clear();
        }
    }

    public void ScanRss() {
        startScan();
    }

    public boolean isscan() {
        return isScanning;
    }

    public boolean isvalid() {
        return ValidWifiNumber >= 3;
    }

    public Vector<String> getRSSlist() {
        return scanned;
    }

    public int getNumberOfWiFi() {
        return NumberOfWiFi;
    }

    public boolean[] getIsValid() {
        return isValid;
    }

    public double[] getRadius() {
        return radius;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    private void write2file(String filename, String a) {//Write to the SD card
        try {
            @SuppressLint("SdCardPath") File file = new File("/sdcard/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            @SuppressLint("SdCardPath") RandomAccessFile randomFile = new
                    RandomAccessFile("/sdcard/" + filename, "rw"); // The length of the file(byte)
            long fileLength = randomFile.length(); // Put the writebyte to the end of the file
            randomFile.seek(fileLength);
            randomFile.writeBytes(a);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}