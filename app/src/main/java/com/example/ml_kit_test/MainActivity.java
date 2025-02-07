package com.example.ml_kit_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.io.IOException;
import java.util.List;

import static java.lang.Math.atan2;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";
    private BroadcastResult broadcast = new BroadcastResult();
    public static final String MyFilter01 = "action01";
    public static final String Intent01 = "intent01";
    public static final String BODYRightELBOW = "bodyRightELBOW";
    public static final String BODYLeftELBOW = "bodyLeftELBOW";
    public static final String BODYRightShoulder = "bodyRightShoulder";
    public static final String BODYLeftShoulder = "bodyLeftShoulder";
    public static final String BODYRightHip = "bodyRightHip";
    public static final String BODYRightKnee = "bodyRightKnee";
    public static final String BODYLeftHip = "bodyLeftHip";
    public static final String BODYLeftKnee = "bodyLeftKnee";
    private Handler aHandler;
    private int count = 0;
    private String msgCunt = "";

    private TextView txt_main_tip;
    PoseDetectorOptions options;
    //AccuratePoseDetectorOptions options;
    PoseDetector poseDetector;
    InputImage image;
    Bitmap bitmap;
    int rotationDegree;
    /////////////
    private static final String POSE_DETECTION = "Pose Detection";
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private CameraSource cameraSource = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ///////////
    //private DrawerLayout drawerLayout;
    private DrawerLayout drawer;
    private String selectItem;
    //右手肘
    double bodyRightELBOW;
    //左手肘
    double bodyLeftELBOW;
    //右肩
    double bodyRightShoulder;
    //左肩
    double bodyLeftShoulder;
    //右臀
    double bodyRightHip;
    //右膝
    double bodyRightKnee;
    //左臀
    double bodyLeftHip;
    //左膝
    double bodyLeftKnee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 11);
                Log.d(TAG, "onCreate: OK");
                initView();
            }
        } else {
            //启动相机
            Log.d(TAG, "onCreate: NO");
            initView();
        }
    }

    private void initView(){
        selectItem = "戰士一式";
        //selectItem = "平板式";
        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "initView Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "initView graphicOverlay is null");
        }else {
            Log.d(TAG, "initView graphicOverlay is not null");
        }

        /**建立廣播過濾器*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyFilter01);
        /**註冊廣播*/
        registerReceiver(broadcast, intentFilter);
        txt_main_tip = (TextView)findViewById(R.id.txt_main_tip);
        aHandler = new Handler();
        aHandler.post(runnable);
        //////////////////////////
        rotationDegree = 0;

        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.stand);

        options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();

        /*options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();*/
        poseDetector = PoseDetection.getClient(options);

        image = InputImage.fromBitmap(bitmap, rotationDegree);

        Task<Pose> result =
                poseDetector.process(image).addOnSuccessListener(
                                new OnSuccessListener<Pose>() {
                                    @Override
                                    public void onSuccess(Pose pose) {
                                        // Task completed successfully
                                        // ...
                                        List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                                        /*double rightHipAngle;
                                        rightHipAngle = getAngle(
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_HIP),
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE));
                                        Log.d(TAG, "onSuccess: rightHipAngle1右腰 = "+rightHipAngle);

                                        rightHipAngle = getAngle(
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
                                                pose.getPoseLandmark(PoseLandmark.RIGHT_HIP));
                                        Log.d(TAG, "onSuccess: rightHipAngle2右肩 = "+rightHipAngle);*/
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        ///////////////////////////////////
        // 获取抽屉布局控件
        /*drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);// 显示导航按钮
            actionBar.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground);// 修改默认图标
        }*/
        //////////////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //增加一个抽屉开关
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //将活动注册为导航视图的一个监听器
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*Fragment fragment = new HomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_frame, fragment);
        ft.commit();*/

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        Intent intent = null;

        if (id == R.id.nav_sport){
            Toast.makeText(getApplicationContext(),"動作選擇",Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            View v = getLayoutInflater().inflate(R.layout.set_custom_dialog_layout,null);
            alertDialog.setTitle("請選擇動作");
            alertDialog.setView(v);
            alertDialog.setPositiveButton("確定",(((dialog, which) -> {})));

            AlertDialog dialog = alertDialog.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v1 -> {
                Toast.makeText(getBaseContext(),"確定"+selectItem,Toast.LENGTH_SHORT).show();
                txt_main_tip.setText(selectItem+"姿勢調整");
                dialog.dismiss();
            }));

            TextView txt_dialog_word = v.findViewById(R.id.txt_dialog_word);
            Spinner spinner1 = v.findViewById(R.id.spinnerEX1);

            ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this
                    ,R.array.planets_array,android.R.layout.simple_dropdown_item_1line);
            spinner1.setAdapter(adapter1);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectItem = adapterView.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            txt_dialog_word.setText("123456789");

        } else if (id == R.id.nav_remind){
            //Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();
            //intent = new Intent(this, UserFeedBackActivity.class);
        } else if (id == R.id.nav_remindList){
            //Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_SHORT).show();
            //intent = new Intent(this, MyCatActivity.class);
        }

        //else if (id == R.id.nav_sent)fragment = new HomeFragment();
        //else if (id == R.id.nav_trash)fragment = new HomeFragment();
        //else if (id == R.id.nav_help)intent = new Intent(this, HelpActivity.class);
        //else if (id == R.id.nav_feedback)intent = new Intent(this, FeedbackActivity.class);
        //else fragment = new HomeFragment();
        /*else fragment = new MainFragment();

        //根据用户在抽屉中选择的选项，显示相应的片段和活动
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }else startActivity(intent);*/

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //用户单击某一项时关闭抽屉
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //后退时，关闭抽屉
    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// HomeAsUp按钮的id永远是android.R.id.home
                drawerLayout.openDrawer(GravityCompat.START);// 显示抽屉布局
                break;
            default:
                break;
        }
        return true;
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        createCameraSource(POSE_DETECTION);
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
        if (aHandler != null) {
            aHandler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result =
                Math.toDegrees(
                        atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                                lastPoint.getPosition().x - midPoint.getPosition().x)
                                - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                                firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result); // Angle should never be negative
        if (result > 180) {
            result = (360.0 - result); // Always get the acute representation of the angle
        }
        return result;
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void createCameraSource(String model) {
        Log.d(TAG, "createCameraSource: ");
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            Log.d(TAG, "createCameraSource: == null");
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case POSE_DETECTION:
                    PoseDetectorOptionsBase poseDetectorOptions =
                            PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
                    Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
                    boolean shouldShowInFrameLikelihood =
                            PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
                    boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
                    boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
                    boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
                    cameraSource.setMachineLearningFrameProcessor(
                            new PoseDetectorProcessor(
                                    this,
                                    poseDetectorOptions,
                                    shouldShowInFrameLikelihood,
                                    visualizeZ,
                                    rescaleZ,
                                    runClassification,
                                    /* isStreamMode = */ true));

                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(getApplicationContext(),
                            "Can not create image processor: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcast);
    }



    private class BroadcastResult extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**這邊接收來自Broadcast回傳*/
            String mAction = intent.getAction();
            assert mAction != null;

            String msg = "";
            switch (mAction) {
                //戰士二式
                case MyFilter01:
                    if(selectItem.equals("戰士一式")){
                        msg = warrior1(intent);
                    }else if(selectItem.equals("戰士二式")){
                        msg = warrior2(intent);
                    }else if(selectItem.equals("平板式")){
                        msg = tabletType(intent);
                    }
                    //msg = tabletType(intent);
                    //msg = warrior2(intent);
                    /*bodyRightELBOW = intent.getDoubleExtra(BODYRightELBOW,0);
                    //Log.d(TAG, "onReceive: bodyRightELBOW = "+bodyRightELBOW);
                    if(bodyRightELBOW >200 || bodyRightELBOW< 160){
                        msg += "請將右手伸直\n";
                    }else {
                        msg = msg.replaceAll("請將右手伸直\n","");
                    }
                    bodyLeftELBOW = intent.getDoubleExtra(BODYLeftELBOW,0);
                    //Log.d(TAG, "onReceive: bodyLeftELBOW = "+bodyLeftELBOW);
                    if(bodyLeftELBOW >200 || bodyLeftELBOW< 160){
                        msg += "請將左手伸直\n";
                    }else {
                        msg = msg.replaceAll("請將左手伸直\n","");
                    }
                    bodyRightShoulder = intent.getDoubleExtra(BODYRightShoulder,0);
                    //Log.d(TAG, "onReceive: bodyRightShoulder = "+bodyRightShoulder);
                    if(bodyRightShoulder > 110){
                        msg += "右肩太高請與身體包持垂直。\n";
                    }else if(bodyRightShoulder < 70){
                        msg += "右肩太低請與身體包持垂直。\n";
                    }else {
                        msg = msg.replaceAll("右肩太高請與身體包持垂直。\n","");
                        msg = msg.replaceAll("右肩太低請與身體包持垂直。\n","");
                    }
                    bodyLeftShoulder = intent.getDoubleExtra(BODYLeftShoulder,0);
                    //Log.d(TAG, "onReceive: bodyLeftShoulder = "+bodyLeftShoulder);
                    if(bodyLeftShoulder > 110){
                        msg += "左肩太高請與身體包持垂直。\n";
                    }else if(bodyLeftShoulder < 70){
                        msg += "左肩太低請與身體包持垂直。\n";
                    }else {
                        msg = msg.replaceAll("左肩太高請與身體包持垂直。\n","");
                        msg = msg.replaceAll("左肩太低請與身體包持垂直。\n","");
                    }
                    bodyRightHip = intent.getDoubleExtra(BODYRightHip,0);
                    //Log.d(TAG, "onReceive: bodyRightHip = "+bodyRightHip);
                    if(bodyRightHip >115 || bodyRightHip< 70){
                        msg += "請將右大腿與身體保持垂直\n";
                    }else {
                        msg = msg.replaceAll("請將右大腿與身體保持垂直\n","");
                    }
                    bodyRightKnee = intent.getDoubleExtra(BODYRightKnee,0);
                    //Log.d(TAG, "onReceive: bodyRightKnee = "+bodyRightKnee);
                    if(bodyRightKnee >115 || bodyRightKnee< 70){
                        msg += "請將右膝保持垂直\n";
                    }else {
                        msg = msg.replaceAll("請將右膝保持垂直\n","");
                    }
                    bodyLeftHip = intent.getDoubleExtra(BODYLeftHip,0);
                    //Log.d(TAG, "onReceive: bodyLeftHip = "+bodyLeftHip);
                    if(bodyLeftHip > 155){
                        msg += "請將左腳向外伸直\n";
                    }else if(bodyLeftHip < 115){
                        msg += "請將左腳往回收一點\n";
                    }else {
                        msg = msg.replaceAll("請將左腳向外伸直\n","");
                        msg = msg.replaceAll("請將左腳往回收一點\n","");
                    }
                    bodyLeftKnee = intent.getDoubleExtra(BODYLeftKnee,0);
                    //Log.d(TAG, "onReceive: bodyLeftKnee = "+bodyLeftKnee);
                    if(bodyLeftKnee >200 || bodyLeftKnee< 160){
                        msg += "請將左膝伸直\n";
                    }else {
                        msg = msg.replaceAll("請將左膝伸直\n","");
                    }

                    if(msg.equals("")){
                        msg = "動作良好，請繼續保持。";
                    }*/
                    msgCunt = msg;
                    txt_main_tip.setVisibility(View.VISIBLE);
                    txt_main_tip.setText(msg);

                    break;
                
            }
        }
    }
    private String warrior1(Intent intent){
        String msg = "";
        bodyRightELBOW = intent.getDoubleExtra(BODYRightELBOW,0);
        //Log.d(TAG, "onReceive: bodyRightELBOW = "+bodyRightELBOW);
        if(bodyRightELBOW >200 || bodyRightELBOW< 150){
            msg += "請將右手伸直\n";
        }else {
            msg = msg.replaceAll("請將右手伸直\n","");
        }
        bodyLeftELBOW = intent.getDoubleExtra(BODYLeftELBOW,0);
        //Log.d(TAG, "onReceive: bodyLeftELBOW = "+bodyLeftELBOW);
        if(bodyLeftELBOW >200 || bodyLeftELBOW< 150){
            msg += "請將左手伸直\n";
        }else {
            msg = msg.replaceAll("請將左手伸直\n","");
        }
        bodyRightShoulder = intent.getDoubleExtra(BODYRightShoulder,0);
        //Log.d(TAG, "onReceive: bodyRightShoulder = "+bodyRightShoulder);
        if(bodyRightShoulder > 210){
            msg += "請將右手打直舉高。\n";
        }else if(bodyRightShoulder < 150){
            msg += "請將右手打直舉高。\n";
        }else {
            msg = msg.replaceAll("請將右手打直舉高。\n","");
            //msg = msg.replaceAll("右肩太低請與身體包持垂直。\n","");
        }
        bodyLeftShoulder = intent.getDoubleExtra(BODYLeftShoulder,0);
        //Log.d(TAG, "onReceive: bodyLeftShoulder = "+bodyLeftShoulder);
        if(bodyLeftShoulder > 210){
            msg += "請將左手打直舉高。\n";
        }else if(bodyLeftShoulder < 150){
            msg += "請將左手打直舉高。\n";
        }else {
            msg = msg.replaceAll("請將左手打直舉高。\n","");
            //msg = msg.replaceAll("左肩太低請與身體包持垂直。\n","");
        }
        bodyRightHip = intent.getDoubleExtra(BODYRightHip,0);
        //Log.d(TAG, "onReceive: bodyRightHip = "+bodyRightHip);
        if(bodyRightHip >115 || bodyRightHip< 70){
            msg += "請將右大腿與身體保持垂直\n";
        }else {
            msg = msg.replaceAll("請將右大腿與身體保持垂直\n","");
        }
        bodyRightKnee = intent.getDoubleExtra(BODYRightKnee,0);
        //Log.d(TAG, "onReceive: bodyRightKnee = "+bodyRightKnee);
        if(bodyRightKnee >115 || bodyRightKnee< 70){
            msg += "請將右膝保持垂直\n";
        }else {
            msg = msg.replaceAll("請將右膝保持垂直\n","");
        }
        bodyLeftHip = intent.getDoubleExtra(BODYLeftHip,0);
        //Log.d(TAG, "onReceive: bodyLeftHip = "+bodyLeftHip);
        if(bodyLeftHip > 155){
            msg += "請將左腳向外伸直\n";
        }else if(bodyLeftHip < 115){
            msg += "請將左腳往回收一點\n";
        }else {
            msg = msg.replaceAll("請將左腳向外伸直\n","");
            msg = msg.replaceAll("請將左腳往回收一點\n","");
        }
        bodyLeftKnee = intent.getDoubleExtra(BODYLeftKnee,0);
        //Log.d(TAG, "onReceive: bodyLeftKnee = "+bodyLeftKnee);
        if(bodyLeftKnee >200 || bodyLeftKnee< 160){
            msg += "請將左膝伸直\n";
        }else {
            msg = msg.replaceAll("請將左膝伸直\n","");
        }

        if(msg.equals("")){
            msg = "戰士一式，動作良好，請繼續保持。";
        }
        return msg;
    }

    private String warrior2(Intent intent){
        String msg = "";
        bodyRightELBOW = intent.getDoubleExtra(BODYRightELBOW,0);
        //Log.d(TAG, "onReceive: bodyRightELBOW = "+bodyRightELBOW);
        if(bodyRightELBOW >200 || bodyRightELBOW< 160){
            msg += "請將右手伸直\n";
        }else {
            msg = msg.replaceAll("請將右手伸直\n","");
        }
        bodyLeftELBOW = intent.getDoubleExtra(BODYLeftELBOW,0);
        //Log.d(TAG, "onReceive: bodyLeftELBOW = "+bodyLeftELBOW);
        if(bodyLeftELBOW >200 || bodyLeftELBOW< 160){
            msg += "請將左手伸直\n";
        }else {
            msg = msg.replaceAll("請將左手伸直\n","");
        }
        bodyRightShoulder = intent.getDoubleExtra(BODYRightShoulder,0);
        //Log.d(TAG, "onReceive: bodyRightShoulder = "+bodyRightShoulder);
        if(bodyRightShoulder > 110){
            msg += "右肩太高請與身體包持垂直。\n";
        }else if(bodyRightShoulder < 70){
            msg += "右肩太低請與身體包持垂直。\n";
        }else {
            msg = msg.replaceAll("右肩太高請與身體包持垂直。\n","");
            msg = msg.replaceAll("右肩太低請與身體包持垂直。\n","");
        }
        bodyLeftShoulder = intent.getDoubleExtra(BODYLeftShoulder,0);
        //Log.d(TAG, "onReceive: bodyLeftShoulder = "+bodyLeftShoulder);
        if(bodyLeftShoulder > 110){
            msg += "左肩太高請與身體包持垂直。\n";
        }else if(bodyLeftShoulder < 70){
            msg += "左肩太低請與身體包持垂直。\n";
        }else {
            msg = msg.replaceAll("左肩太高請與身體包持垂直。\n","");
            msg = msg.replaceAll("左肩太低請與身體包持垂直。\n","");
        }
        bodyRightHip = intent.getDoubleExtra(BODYRightHip,0);
        //Log.d(TAG, "onReceive: bodyRightHip = "+bodyRightHip);
        if(bodyRightHip >115 || bodyRightHip< 70){
            msg += "請將右大腿與身體保持垂直\n";
        }else {
            msg = msg.replaceAll("請將右大腿與身體保持垂直\n","");
        }
        bodyRightKnee = intent.getDoubleExtra(BODYRightKnee,0);
        //Log.d(TAG, "onReceive: bodyRightKnee = "+bodyRightKnee);
        if(bodyRightKnee >115 || bodyRightKnee< 70){
            msg += "請將右膝保持垂直\n";
        }else {
            msg = msg.replaceAll("請將右膝保持垂直\n","");
        }
        bodyLeftHip = intent.getDoubleExtra(BODYLeftHip,0);
        //Log.d(TAG, "onReceive: bodyLeftHip = "+bodyLeftHip);
        if(bodyLeftHip > 155){
            msg += "請將左腳向外伸直\n";
        }else if(bodyLeftHip < 115){
            msg += "請將左腳往回收一點\n";
        }else {
            msg = msg.replaceAll("請將左腳向外伸直\n","");
            msg = msg.replaceAll("請將左腳往回收一點\n","");
        }
        bodyLeftKnee = intent.getDoubleExtra(BODYLeftKnee,0);
        //Log.d(TAG, "onReceive: bodyLeftKnee = "+bodyLeftKnee);
        if(bodyLeftKnee >200 || bodyLeftKnee< 160){
            msg += "請將左膝伸直\n";
        }else {
            msg = msg.replaceAll("請將左膝伸直\n","");
        }

        if(msg.equals("")){
            msg = "戰士二式，動作良好，請繼續保持。";
        }
        return msg;
    }

    private String tabletType(Intent intent){
        String msg = "";
        bodyRightELBOW = intent.getDoubleExtra(BODYRightELBOW,0);
        //Log.d(TAG, "onReceive: bodyRightELBOW = "+bodyRightELBOW);
        if(bodyRightELBOW >200 || bodyRightELBOW< 160){
            msg += "請將右手伸直\n";
        }else {
            msg = msg.replaceAll("請將右手伸直\n","");
        }
        bodyLeftELBOW = intent.getDoubleExtra(BODYLeftELBOW,0);
        //Log.d(TAG, "onReceive: bodyLeftELBOW = "+bodyLeftELBOW);
        if(bodyLeftELBOW >200 || bodyLeftELBOW< 160){
            msg += "請將左手伸直\n";
        }else {
            msg = msg.replaceAll("請將左手伸直\n","");
        }
        bodyRightShoulder = intent.getDoubleExtra(BODYRightShoulder,0);
        //Log.d(TAG, "onReceive: bodyRightShoulder = "+bodyRightShoulder);
        if(bodyRightShoulder > 110){
            msg += "右肩太高請與身體包持垂直。\n";
        }else if(bodyRightShoulder < 70){
            msg += "右肩太低請與身體包持垂直。\n";
        }else {
            msg = msg.replaceAll("右肩太高請與身體包持垂直。\n","");
            msg = msg.replaceAll("右肩太低請與身體包持垂直。\n","");
        }
        bodyLeftShoulder = intent.getDoubleExtra(BODYLeftShoulder,0);
        //Log.d(TAG, "onReceive: bodyLeftShoulder = "+bodyLeftShoulder);
        if(bodyLeftShoulder > 110){
            msg += "左肩太高請與身體包持垂直。\n";
        }else if(bodyLeftShoulder < 70){
            msg += "左肩太低請與身體包持垂直。\n";
        }else {
            msg = msg.replaceAll("左肩太高請與身體包持垂直。\n","");
            msg = msg.replaceAll("左肩太低請與身體包持垂直。\n","");
        }
        bodyRightHip = intent.getDoubleExtra(BODYRightHip,0);
        //Log.d(TAG, "onReceive: bodyRightHip = "+bodyRightHip);
        if(bodyRightHip >200 || bodyRightHip< 160){
            msg += "請將右大腿與身體保持一直線\n";
        }else {
            msg = msg.replaceAll("請將右大腿與身體保持一直線\n","");
        }
        bodyLeftHip = intent.getDoubleExtra(BODYLeftHip,0);
        //Log.d(TAG, "onReceive: bodyLeftHip = "+bodyLeftHip);
        if(bodyLeftHip >200 || bodyLeftHip< 160){
            msg += "請將左大腿與身體保持一直線\n";
        }else {
            msg = msg.replaceAll("請將左大腿與身體保持一直線\n","");
        }
        bodyRightKnee = intent.getDoubleExtra(BODYRightKnee,0);
        //Log.d(TAG, "onReceive: bodyRightKnee = "+bodyRightKnee);
        if(bodyRightKnee >200 || bodyRightKnee< 160){
            msg += "請將右膝伸直\n";
        }else {
            msg = msg.replaceAll("請將右膝伸直\n","");
        }
        bodyLeftKnee = intent.getDoubleExtra(BODYLeftKnee,0);
        //Log.d(TAG, "onReceive: bodyLeftKnee = "+bodyLeftKnee);
        if(bodyLeftKnee >200 || bodyLeftKnee< 160){
            msg += "請將左膝伸直\n";
        }else {
            msg = msg.replaceAll("請將左膝伸直\n","");
        }


        if(msg.equals("")){
            msg = "平板式，動作良好，請繼續保持。";
        }
        return msg;
    }

    final Runnable runnable = new Runnable() {
        public void run() {
    // TODO Auto-generated method stub
            String tempMsg;
            tempMsg = msgCunt;
            count++;
            aHandler.postDelayed(runnable, 1000);
            if(count%7 == 0 && tempMsg.equals(msgCunt)){
                //txt_main_tip.setText("戰士二式姿勢調整");
                txt_main_tip.setText(selectItem+"姿勢調整");
            }
        }
    };

}