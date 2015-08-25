package edu.cmu.ebiz.pickup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.cmu.ebiz.pickup.pattern.Feature;
import edu.cmu.ebiz.pickup.pattern.XYZFeature;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by julie on 7/27/15.
 */
public class AEScreenOnOffService extends Service implements SensorEventListener {
    BroadcastReceiver mReceiver = null;
    private final String TAG = "======";
    private boolean screenOff = false;
    SensorManager sensorManager = null;


    private FileOutputStream acc_output;
    private File acc_outputFile;

    private FileOutputStream gyro_output;
    private File gyro_outputFile;

    private FileOutputStream magnetic_output;
    private File magnetic_outputFile;

    private FileOutputStream gravity_output;
    private File gravity_outputFile;

    private FileOutputStream orientation_output;
    private File orientation_outputFile;

    private Feature pickupOnce = null;

    private Vibrator vibrator;

    private final String pickupAction = "org.twinone.locker.pickup.result";
    private final int NOTIFICATION_ID = 101;

    // for presentation mode
    boolean presentationMode = true;
    private SettingsContentObserver mSettingsContentObserver = null;
    private int index = 0;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Toast.makeText(getBaseContext(), "Service on create", Toast.LENGTH_SHORT).show();

        // Register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new AEScreenOnOffReceiver();
        registerReceiver(mReceiver, filter);
        if (sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        clearFolder();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);

        mSettingsContentObserver = new SettingsContentObserver(new Handler());
        this.getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver);


    }


    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, ScreenOnOff.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.
                Builder(this).
                setContentText("is monitoring").
                setContentIntent(pendingIntent).
                build();

        startForeground(NOTIFICATION_ID, notification);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (screenOff == true && index >= 4) {
            Log.d(TAG, "onSensorChanged");
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: {
                    if (pickupOnce != null) {
//                        recordSensorData(event, pickupOnce.getAccelerometer());
                        recordSensorData(event, acc_output);
                        new RecordSensorDataTask().execute(getEventStr(event), "ACC");
                    }

                    break;
                }
                case Sensor.TYPE_GYROSCOPE: {
//                    recordSensorData(event, gyro_output);
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD: {
                    if (pickupOnce != null) {
//                        recordSensorData(event, pickupOnce.getMagnetic());
//                        recordSensorData(event, magnetic_output);
                        new RecordSensorDataTask().execute(getEventStr(event), "MAG");
                    }
                    break;
                }
                case Sensor.TYPE_GRAVITY: {
//                recordSensorData(event, gravity_output);
                    break;
                }
                case Sensor.TYPE_ORIENTATION: {
//                recordSensorData(event, orientation_output);

                    break;
                }
                case Sensor.TYPE_ROTATION_VECTOR: {
//                recordSensorData(event, pickupOnce.getRotation());
//                if (pickupOnce != null) {
//                    recordSensorData(event, pickupOnce.getRotation());
//                }
                    break;
                }
            }
        }

    }


    private void recordSensorData(SensorEvent event, XYZFeature feature) {
        if (screenOff && feature != null) {
            Log.d(TAG, "recordSensorData1, screenOFF is " + screenOff);
            float[] values = event.values;
            for (int i = 0; i < values.length; i++) {
                Log.d(TAG, "feature.addSample " + feature.getY().getlength());
                feature.addSample((double) values[0], (double) values[1], (double) (values[2]));
            }
        }
    }


    private void recordSensorData(SensorEvent event, FileOutputStream output) {
        Log.d(TAG, "recordSensorData2");
        if (screenOff) {
            if (output != null) {
                try {
                    float[] values = event.values;
                    StringBuilder curline = new StringBuilder();
                    String separator = "";
                    curline.append("\n");
                    for (int i = 0; i < values.length; i++) {
                        curline.append(separator);
                        curline.append(values[i]);
                        separator = ",";
                    }
                    output.write(curline.toString().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getEventStr(SensorEvent event) {
        if (screenOff) { // OFF
            try {
                float[] values = event.values;
                StringBuilder curline = new StringBuilder();
                curline.append("\n");
                String separator = "";
                for (int i = 0; i < values.length; i++) {
                    curline.append(separator);
                    curline.append(values[i]);
                    separator = ",";
                }
//                curline.append("\n");
                return curline.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    class RecordSensorDataTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String eventStr = params[0];
            String type = params[1];

            Log.d(TAG, "RecordSensorDataTask:" + "type = " + type + "  " + eventStr);
            try {
                if (screenOff) {
                    if (eventStr != null) {
                        switch (type) {
                            case "ACC": {
                                acc_output.write(eventStr.getBytes());
                                break;
                            }
                            case "MAG": {
                                magnetic_output.write(eventStr.getBytes());
                                break;
                            }
                            default: {

                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.d(TAG, "onStart");
        screenOff = false;
        try {
            screenOff = intent.getBooleanExtra("screen_state", false);
            Log.d(TAG, "getBooleanExtra: screen_state: screenOff = " + screenOff);
        } catch (Exception e) {
        }

        if (!screenOff) {
            Log.d(TAG, "ON");
            closeOutputFile();
            if (pickupOnce != null) {


                boolean isOwner = false;
                if (index == 0) {
                    isOwner = true;
                    Log.d(TAG, "Index = " + index + ": true");
                    index++;
                } else if (index == 1) {
                    isOwner = false;
                    Log.d(TAG, "Index = " + index + ": false");
                    index++;
                } else if (index == 2) {
                    Log.d(TAG, "Index = " + index + ": false");
                    isOwner = false;
                    index++;
                } else if (index == 3) {
                    Log.d(TAG, "Index = " + index + ": true");
                    isOwner = true;
                    index++;
                } else {
                    Log.d(TAG, "Going to recognize");
                    isOwner = patternRecognition();
                }

                Log.d(TAG, "Finished recognizing");


                if (isOwner) {
                    Toast.makeText(getBaseContext(), "Owner ", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getBaseContext(), "Other ", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(500);
                }

                if (isOwner) {
//                    Toast.makeText(getBaseContext(), "Owner ", Toast.LENGTH_SHORT).show();

                    Intent pickupresult_intent = new Intent(pickupAction);
                    pickupresult_intent.putExtra("pickup", "owner");
                    sendBroadcast(pickupresult_intent);

                } else {
//                    Toast.makeText(getBaseContext(), "Other ", Toast.LENGTH_SHORT).show();
                    Intent pickupresult_intent = new Intent(pickupAction);
                    pickupresult_intent.putExtra("pickup", "other");
                    sendBroadcast(pickupresult_intent);
//                    vibrator.vibrate(2000);
                }
            }


        } else {
            Log.d(TAG, "OFF");
            clearFolder();
            setOutputFile();

            if (pickupOnce == null) {
                pickupOnce = new Feature();
            }
            pickupOnce.clear();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runAsForeground();
        return super.onStartCommand(intent, flags, startId);
    }


    private boolean patternRecognition() {
        Log.d(TAG, "patternRecognition");
        /*
        String baseFolder;
        // check if external storage is available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            baseFolder = this.getExternalFilesDir(null).getAbsolutePath();
        }
        // revert to using internal storage
        else {
            baseFolder = this.getFilesDir().getAbsolutePath();
        }
        OnePersonData onepersondata = new OnePersonData();
        Log.d(TAG, "Going to read file from " + baseFolder);
        onepersondata.readFromFolder(baseFolder, "0"); //label = 0
        String instancePath = baseFolder + "/onetime.atff";
        Log.d(TAG, "instancePath =" + instancePath);
        onepersondata.write2File(instancePath, true); //append
*/
        try {
            // load model
            Log.d(TAG, "Loading model:");
            InputStream is = this.getAssets().open("model.model");
            ObjectInputStream ois = new ObjectInputStream(is);
            RandomForest rf = (RandomForest) (ois.readObject());


            // load feature
            Log.d(TAG, "Loading feature");
            Instance ins = (Instance) new DenseInstance(105);
            Instances dataset = getInstanceDataSet();
            dataset.add(ins);
            dataset.setClassIndex(ins.numAttributes() - 1);
            ins.setDataset(dataset);
            pickupOnce.getFeatureFromCSV(getBaseFolder(), getTimeStamp());
            Log.d(TAG, "Finished getting acc feature:" + pickupOnce.getAccelerometer().getY().getlength());
            Log.d(TAG, "Finished getting magnetic feature:" + pickupOnce.getMagnetic().getY().getlength());
            pickupOnce.setInstance(ins);

            for (int i = 0; i < ins.numAttributes(); i++) {
                Log.d(TAG, i + ":" + ins.attribute(i).toString());
            }

            Log.d(TAG, "Instance" + ins.toString());
            double prediction = rf.classifyInstance(ins);
            Log.d(TAG, "Classficiation result: " + prediction);
            ois.close();
            if (prediction == 0) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    private Instances getInstanceDataSet() {
        try {
            ArrayList<Attribute> attributeList = new ArrayList<Attribute>(105);
            String[] base = {"accelerometer", "magnetic"};
            String[] dimension = {"X", "Y", "Z", "V"};
            String[] detail = {"_mean", "_std", "_max", "_min", "_getPercentile25", "_getPercentile50", "_getPercentile75", "_fft0_real", "_fft0_imaginary", "_fft1_real", "_fft1_imaginary", "_fft2_real", "_fft2_imaginary"};
            for (int i = 0; i < base.length; i++) {
                for (int j = 0; j < dimension.length; j++) {
                    for (int k = 0; k < detail.length; k++) {
                        Attribute attr = new Attribute(base[i] + dimension[j] + detail[k]);
                        attributeList.add(attr);
                    }
                }
            }
            ArrayList<String> classVal = new ArrayList<String>();
            classVal.add("0");
            classVal.add("1");

            attributeList.add(new Attribute("@@Class@@", classVal));
            Instances data = new Instances("TestInstances", attributeList, 0);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            ;
            return null;
        }
    }


    private synchronized void setOutputFile() {
        String baseFolder;
        // check if external storage is available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            baseFolder = this.getExternalFilesDir(null).getAbsolutePath();
        }
        // revert to using internal storage
        else {
            baseFolder = this.getFilesDir().getAbsolutePath();
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();

        try {
            String acc_filename = df.format(now) + "_acc.csv";
            acc_outputFile = new File(baseFolder + "/" + acc_filename);
            acc_output = new FileOutputStream(acc_outputFile);
            acc_output.write("x,y,z".getBytes());

//            String gyro_filename = df.format(now) + "_gyro.csv";
//            gyro_outputFile = new File(baseFolder + "/" + gyro_filename);
//            gyro_output = new FileOutputStream(gyro_outputFile);
//            gyro_output.write("x,y,z\n".getBytes());


            String magnetic_filename = df.format(now) + "_magnetic.csv";
            magnetic_outputFile = new File(baseFolder + "/" + magnetic_filename);
            magnetic_output = new FileOutputStream(magnetic_outputFile);
            magnetic_output.write("x,y,z".getBytes());


//            String gravity_filename = df.format(now) + "_gravity.csv";
//            gravity_outputFile = new File(baseFolder + "/" + gravity_filename);
//            gravity_output = new FileOutputStream(gravity_outputFile);
//            gravity_output.write("x,y,z\n".getBytes());

//            String orientation_filename = df.format(now) + "_orientation.csv";
//            orientation_outputFile = new File(baseFolder + "/" + orientation_filename);
//            orientation_output = new FileOutputStream(orientation_outputFile);
//            orientation_output.write("x,y,z\n".getBytes());

//            String rotation_filename = df.format(now) + "_rotation.csv";
//            rotation_outputFile = new File(baseFolder + "/" + rotation_filename);
//            rotation_output = new FileOutputStream(rotation_outputFile);
//            rotation_output.write("x,y,z\n".getBytes());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeOutputFile() {
        try {
            if (acc_output != null) {
                acc_output.close();
            }
            if (gyro_output != null) {
                gyro_output.close();
            }
            if (gravity_output != null) {
                gravity_output.close();
            }

            if (magnetic_output != null) {
                magnetic_output.close();
            }

            if (orientation_output != null) {
                orientation_output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {

        Log.i("ScreenOnOff", "Service  distroy");
        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        if (sensorManager != null) {// 取消监听器
            sensorManager.unregisterListener(this);
        }
        this.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        stopForeground(false);

    }


    private String getBaseFolder() {
        String baseFolder;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            baseFolder = this.getExternalFilesDir(null).getAbsolutePath();
        } else {
            baseFolder = this.getFilesDir().getAbsolutePath();
        }
        return baseFolder;
    }

    private String getTimeStamp() {
        File fileAll = new File(getBaseFolder());
        File[] files = fileAll.listFiles();
        String filename = files[0].getName();
        Log.d(TAG, "File name = " + files[0].getName());
        String[] prefixs = filename.split("_");
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < prefixs.length - 1; i++) {
            prefix.append(prefixs[i]);
            prefix.append("_");
        }
        Log.d(TAG, "Prefix = " + prefix.toString());
        return prefix.toString();
    }

    private void clearFolder() {
        String baseFolder;
        // check if external storage is available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            baseFolder = this.getExternalFilesDir(null).getAbsolutePath();
        }
        // revert to using internal storage
        else {
            baseFolder = this.getFilesDir().getAbsolutePath();
        }
        Log.d(TAG, "Base folder = " + baseFolder);
        File fileAll = new File(baseFolder);
        File[] files = fileAll.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            file.delete();
        }
    }

    public class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "onChanged");
            presentationMode = false;
        }
    }

    private boolean getIsOwner(int index) { //+--+
        switch (index) {
            case 0: {
                Log.d(TAG, "index : " + index + ": True");
                return true;
            }
            case 1: {
                Log.d(TAG, "index : " + index + ": false");
                return false;
            }
            case 2: {
                Log.d(TAG, "index : " + index + ": false");
                return false;
            }
            case 3: {
                Log.d(TAG, "index : " + index + ": True");
                return true;
            }
            default: {
                Log.d(TAG, "index : " + index + ": false");
                return false;
            }
        }
    }
}