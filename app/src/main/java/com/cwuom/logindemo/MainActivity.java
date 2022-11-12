package com.cwuom.logindemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mmin18.widget.RealtimeBlurView;
import com.jem.fliptabs.FlipTab;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.style.IOSStyle;
import com.kongzue.dialogx.style.MIUIStyle;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.ncorti.slidetoact.SlideToActView;
import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;

import io.ghyeok.stickyswitch.widget.StickySwitch;

/**
 * @author cwuom
 * MainActivity > 2022.10.28
 * >没错，这就是程序的开始，屎山由此发展！<
 * */

/**
 * 食用说明：登录账户和密码限于本地，请自行使用自己服务器的MYSQL进行替换！
 * 跑路交流群：893018099
 * 仅供参考，欢迎关注我(im-cwuom)
 * */

/**
 * 请勿利用此项目到正式项目中，仅供娱乐，或者你可以自行优化代码！
 * */

public class MainActivity extends AppCompatActivity {

    private final MyHandler mHandler = new MyHandler(this);

    private CardView mCvStartBlur; // 开始的那个高斯模糊小圆点
    private CardView mCvLogin; // 登录用的CARDVIEW(填写账户密码)
    private CardView mCvReg; // 注册用的CARDVIEW(填写账户密码邮箱等信息)
    private RealtimeBlurView mRBackground; // 背景模糊
    private ImageView mIvBackground; // 背景图片

    private int rX = 0; // 点击后圆点扩散的X
    private int rY = 0; // ****** 的Y

    private TextView mTvStart; // 就是小圆圈中间的箭头
    private TextView mTvTouchStart; // "轻触开始"

    private FlipTab flipTab; // 没错，就是上面切换登录注册的TAB
    private SlideToActView sta; // 滑动模块(登录)
    private SlideToActView sta_reg; // 滑动注册
    private StickySwitch stickySwitch; // 选择是否记住账户密码 - 监听请看>https://github.com/GwonHyeok/StickySwitch


    /*
    * Reg表注册
    * Login表登录
    * */
    private EditText mEtUsernameReg; // 输入用户名
    private EditText mEtEmailReg; // 输入邮箱
    private EditText mEtPasswordReg; // 输入密码

    private EditText mEtUsernameLogin; // 同上
    private EditText mEtPasswordLogin;

    private RelativeLayout mRlMain; // RelativeLayout 包含登录注册等控件
    private RelativeLayout mRlRule; // RelativeLayout 登录成功后会显示协议内容和其他控件

    private CustomScrollView scrollView; // 可滑动


    private SlideToActView sta_start; // 阅读完协议以后滑动开始使用

    private LoadingPopupView loadingPopupView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DialogX.init(this); // 初始化 删掉这行就会闪退



        mCvStartBlur = findViewById(R.id.cardview_2);
        mRBackground = findViewById(R.id.blur_bg_loading);
        mIvBackground = findViewById(R.id.main_bg);
        mTvTouchStart = findViewById(R.id.tv_touchStart);



        stickySwitch = findViewById(R.id.sticky_switch);
        stickySwitch.setAnimationDuration(300);
        sta = findViewById(R.id.login_now);
        flipTab = findViewById(R.id.flip);
        mCvLogin = findViewById(R.id.card_view);
        sta_reg = findViewById(R.id.reg_now);
        mCvReg = findViewById(R.id.card_view_reg);

        mEtPasswordReg = findViewById(R.id.et_password_r);
        mEtEmailReg = findViewById(R.id.et_email_r);
        mEtUsernameReg = findViewById(R.id.et_username_r);

        mEtPasswordLogin = findViewById(R.id.login_password);
        mEtUsernameLogin = findViewById(R.id.et_username_l);

        mRlMain = findViewById(R.id.rl_main);



        // 设置CardView透明背景 (开始的圆点)
        mCvStartBlur.setCardBackgroundColor(Color.TRANSPARENT);
        mCvStartBlur.setCardElevation(0);

        mTvStart = findViewById(R.id.tv_start);


        // 设置所有控件不可见
        mTvStart.setVisibility(View.VISIBLE);
        mCvStartBlur.setVisibility(View.VISIBLE);
        mTvTouchStart.setVisibility(View.VISIBLE);

        stickySwitch.setVisibility(View.INVISIBLE);
        sta.setVisibility(View.INVISIBLE);
        flipTab.setVisibility(View.INVISIBLE);
        mCvLogin.setVisibility(View.INVISIBLE);

        sta_reg.setVisibility(View.INVISIBLE);
        mCvReg.setVisibility(View.INVISIBLE);



        /*
        * SQLLITE
        * */
        DBOpenHelper dbsqLiteOpenHelper = new DBOpenHelper(MainActivity.this,"users.db",null,1);
        SQLiteDatabase db = dbsqLiteOpenHelper.getWritableDatabase();

        //创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();

//        mEtUsernameLogin.setText("233");
//        mEtPasswordLogin.setText("123456");
        mRlMain.setVisibility(View.VISIBLE);

        mRlRule = findViewById(R.id.rl_login_success);
        mRlRule.setVisibility(View.INVISIBLE);

        scrollView = findViewById(R.id.scrollView2);

        sta_start = findViewById(R.id.start);


        loadingPopupView = new XPopup.Builder(MainActivity.this)
                .asLoading("Loading...");




        scrollView.setOnScrollChangeListener(new CustomScrollView.OnScrollChangeListener() { // 检测你是否看完协议（滑动到底部）
            @Override
            public void onScrollToStart() {

            }

            @Override
            public void onScrollToEnd() {
                sta_start.setLocked(false);
                sta_start.setText("开始使用");
                sta_start.setOuterColor(Color.parseColor("#336C35"));
                sta_start.setInnerColor(Color.parseColor("#629131"));

                //#336C35
//                app:inner_color="#9F4F4D"
//                app:outer_color="#6A3736"
//                app:slider_icon_color="#FFFFFF"
            }
        });


        sta_start.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                MessageDialog.show("演示结束", "您可以在此处跳转到主服务，我们只能演示这么多了", "确定");
                /*
                * 你登录成功后要做什么可以在这里添加
                * */
            }
        });


        /*
        * 填完信息后滑动校验登录信息
        * */
        sta.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                slideToActView.resetSlider(); // 自动重置滑动状态

                if (!mEtUsernameLogin.getText().toString().equals("") && !mEtPasswordLogin.getText().toString().equals("")){
                    if(Validator.isEmail(mEtUsernameLogin.getText().toString())){
                        Boolean b = false; // 判断是否成功匹配账户密码

                        //创建游标对象
                        Log.e("debug", mEtUsernameLogin.getText().toString());
                        Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "email=?", new String[]{mEtUsernameLogin.getText().toString()}, null, null, null);
                        //利用游标遍历所有数据对象
                        if (cursor.getCount() != 0){
                            while(cursor.moveToNext()){
                                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                                @SuppressLint("Range") String username_ = cursor.getString(cursor.getColumnIndex("username"));
                                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                                Log.e("login","result: pwd="  + password +" username: " + username_);
                                if (md5(mEtPasswordLogin.getText().toString()).equals(password)){ // 匹配成功
                                    mHandler.sendEmptyMessage(100); // 跳转到协议界面
                                    b = true;
                                }
                            }

                            if (!b){
                                BottomDialog.show("身份验证失败", "邮箱或密码错误，请再试一次！").setOkButton("重试").setTitleIcon(R.drawable.no_2);
                                mCvLogin.animate().scaleX(0.8F).scaleY(0.8F).setDuration(300);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(300);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        mHandler.sendEmptyMessage(31);
                                    }
                                }).start();
                            }
                        }else{
                            BottomDialog.show("查无此人", "找不到此注册信息，请您先注册后使用").setOkButton("重试").setTitleIcon(R.drawable.no_2);
                        }

                        // 关闭游标，释放资源
                        cursor.close();

                    }else{
                        Boolean b = false; // 判断是否成功匹配账户密码

                        //创建游标对象
                        Log.e("debug", mEtUsernameLogin.getText().toString());
                        Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "username=?", new String[]{mEtUsernameLogin.getText().toString()}, null, null, null);
                        //利用游标遍历所有数据对象
                        if (cursor.getCount() != 0){
                            while(cursor.moveToNext()){
                                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                                @SuppressLint("Range") String username_ = cursor.getString(cursor.getColumnIndex("username"));
                                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                                Log.e("login","result: pwd="  + password +" username: " + username_);
                                if (md5(mEtPasswordLogin.getText().toString()).equals(password)){ // 匹配成功
                                    mHandler.sendEmptyMessage(100); // 跳转到协议界面
                                    b = true;
                                }
                            }

                            if (!b){
                                BottomDialog.show("身份验证失败", "账号或密码错误，请再试一次！").setOkButton("重试").setTitleIcon(R.drawable.no_2);
                                mCvLogin.animate().scaleX(0.8F).scaleY(0.8F).setDuration(300);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(300);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        mHandler.sendEmptyMessage(31);
                                    }
                                }).start();
                            }
                        }else{
                            BottomDialog.show("查无此人", "找不到此注册信息，请您先注册后使用").setOkButton("重试").setTitleIcon(R.drawable.no_2);
                        }

                        // 关闭游标，释放资源
                        cursor.close();

                    }
                }else{
                    BottomDialog.show("信息缺失", "抱歉，请将信息填写完整！").setOkButton("好").setTitleIcon(R.drawable.no_2);
                }
            }
        });


        /*
         * 我预判了你
         * 当控件被滑动时校验信息并替换ICON
         * */
        sta.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE: // 滑动事件
                        if (!mEtUsernameLogin.getText().toString().equals("") && !mEtPasswordLogin.getText().toString().equals("")){
                            if(Validator.isEmail(mEtUsernameLogin.getText().toString())){
                                Boolean b = false; // 判断是否成功匹配账户密码

                                //创建游标对象
                                Log.e("debug", mEtUsernameLogin.getText().toString());
                                Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "email=?", new String[]{mEtUsernameLogin.getText().toString()}, null, null, null);
                                //利用游标遍历所有数据对象
                                if (cursor.getCount() != 0){
                                    while(cursor.moveToNext()){
                                        @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                                        @SuppressLint("Range") String username_ = cursor.getString(cursor.getColumnIndex("username"));
                                        @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                                        Log.e("login","result: pwd="  + password +" username: " + username_);
                                        if (md5(mEtPasswordLogin.getText().toString()).equals(password)){ // 匹配成功
                                            sta.setCompleteIcon(R.drawable.ok_a); // 替换图标(校验成功)
                                            b = true;
                                        }
                                    }

                                    if (!b){
                                        sta.setCompleteIcon(R.drawable.error); // 校验失败
                                    }
                                }else{
                                    sta.setCompleteIcon(R.drawable.error); // 校验失败
                                }

                                // 关闭游标，释放资源
                                cursor.close();

                            }else{
                                Boolean b = false; // 判断是否成功匹配账户密码

                                //创建游标对象
                                Log.e("debug", mEtUsernameLogin.getText().toString());
                                Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "username=?", new String[]{mEtUsernameLogin.getText().toString()}, null, null, null);
                                //利用游标遍历所有数据对象
                                if (cursor.getCount() != 0){
                                    while(cursor.moveToNext()){
                                        @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                                        @SuppressLint("Range") String username_ = cursor.getString(cursor.getColumnIndex("username"));
                                        @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                                        Log.e("login","result: pwd="  + password +" username: " + username_);
                                        if (md5(mEtPasswordLogin.getText().toString()).equals(password)){ // 匹配成功
                                            sta.setCompleteIcon(R.drawable.ok_a); // 替换图标(校验成功)
                                            b = true;
                                        }
                                    }

                                    if (!b){
                                        sta.setCompleteIcon(R.drawable.error); // 校验失败
                                    }
                                }else{
                                    sta.setCompleteIcon(R.drawable.error); // 校验失败
                                }

                                // 关闭游标，释放资源
                                cursor.close();

                            }
                        }
                        break;
                    default:break;
                }
                return false;
            }
        });



        /*
         * 同上
         * 提前校验注册信息并替换滑块ICON
         * */
        sta_reg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
//                        String username = sharedPreferences.getString("username"+mEtUsernameReg.getText().toString(), "");
//                        String e = sharedPreferences.getString("e"+mEtEmailReg.getText().toString(), "");
//                        if (!Objects.equals(e, "") && !Objects.equals(e, "") && !Objects.equals(username, "")){
//                            sta_reg.setCompleteIcon(R.drawable.error);
//                        }else{
//                            if (!mEtEmailReg.getText().toString().equals("") && !mEtUsernameReg.getText().toString().equals("")){
//                                if (Validator.isEmail(mEtEmailReg.getText().toString())){
//                                    sta_reg.setCompleteIcon(R.drawable.ok_a);
//                                }else{
//                                    sta_reg.setCompleteIcon(R.drawable.error);
//                                }
//                            }else{
//                                sta_reg.setCompleteIcon(R.drawable.error);
//                            }
//                        }

                        Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "username=? or email=?", new String[]{mEtUsernameReg.getText().toString(), mEtEmailReg.getText().toString()}, null, null, null);
                        if (cursor.getCount() != 0){ // 确保无前人注册
                            MessageDialog.show("已被注册", "抱歉，该cwuomID或邮箱已被注册！", "确定");
                        }else{
                            // 确保没有填写空信息
                            if (!mEtEmailReg.getText().toString().equals("") && !mEtUsernameReg.getText().toString().equals("") && !mEtPasswordReg.getText().toString().equals("")){
                                if (Validator.isEmail(mEtEmailReg.getText().toString())){ // 检测邮箱是否规范
                                    sta_reg.setCompleteIcon(R.drawable.ok_a);
                                }else{
                                    sta_reg.setCompleteIcon(R.drawable.error);
                                }
                            }else{
                                sta_reg.setCompleteIcon(R.drawable.error);
                            }
                        }
                        break;
                    default:break;
                }
                return false;
            }
        });



        /*
        * 滑动注册时的信息校验
        * */
        sta_reg.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                slideToActView.resetSlider();

                Cursor cursor = db.query("user", new String[]{"id","username","password", "email"}, "username=? or email=?", new String[]{mEtUsernameReg.getText().toString(), mEtEmailReg.getText().toString()}, null, null, null);
                if (cursor.getCount() != 0){ // 确保无前人注册
                    MessageDialog.show("已被注册", "抱歉，该cwuomID或邮箱已被注册！", "确定");
                }else{
                    // 确保没有填写空信息
                    if (!mEtEmailReg.getText().toString().equals("") && !mEtUsernameReg.getText().toString().equals("") && !mEtPasswordReg.getText().toString().equals("")){
                        if (Validator.isEmail(mEtEmailReg.getText().toString())){ // 检测邮箱是否规范
                            /*
                            * 全屏弹窗
                            * */
                            FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.verification_layout) {
                                @Override
                                public void onBind(FullScreenDialog dialog, View v) {
                                    TextView tvCancel = v.findViewById(R.id.cancel);
                                    TextView tvRsend = v.findViewById(R.id.rsend);
                                    CodeInputView codeInputView = v.findViewById(R.id.code);
                                    RelativeLayout layout_code = v.findViewById(R.id.rl_code);
                                    RelativeLayout layout_ok = v.findViewById(R.id.rl_ok);
                                    Button btn_ok = v.findViewById(R.id.btn_backLogin);

                                    layout_code.setVisibility(View.VISIBLE);
                                    layout_ok.setVisibility(View.INVISIBLE);

                                    codeInputView.addOnCompleteListener(new OnCodeCompleteListener() {
                                        @Override
                                        public void onCompleted(String code) {
                                            if (Objects.equals(code, "123456")){ // 校验邮箱验证码
                                                layout_code.animate().scaleX(0).scaleY(0).alpha(0).setDuration(400).start();
                                                layout_ok.setVisibility(View.VISIBLE);
                                                layout_ok.animate().alpha(0).setDuration(0).start();
                                                layout_ok.animate().alpha(1).setDuration(500).start();
                                                PopTip.build()
                                                        .setStyle(IOSStyle.style())
                                                        .iconSuccess()
                                                        .setMessage("您已完成注册！")
                                                        .show();


                                                values.put("username", mEtUsernameReg.getText().toString());
                                                values.put("password",md5(mEtPasswordReg.getText().toString()));
                                                values.put("email", mEtEmailReg.getText().toString());

                                                //数据库执行插入命令
                                                db.insert("user", null, values);

                                                mEtEmailReg.setText("");
                                                mEtPasswordReg.setText("");
                                                mEtUsernameReg.setText("");


                                            }else{
                                                codeInputView.setError("验证失败，请重试！");
                                                codeInputView.setEditable(true); // 重新给予编辑验证码权限
                                            }
                                        }
                                    });

                                    btn_ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    tvCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    tvRsend.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PopTip.build()
                                                    .setStyle(IOSStyle.style())
                                                    .iconSuccess()
                                                    .setMessage("发送成功")
                                                    .show();

                                            tvRsend.setTextColor(MainActivity.this.getResources().getColor(R.color.send));
                                            tvRsend.setClickable(false);
                                        }
                                    });
                                }
                            });
                        }else{
                            MessageDialog.show("校验错误", "请检查您的邮箱是否符合规范！", "确定");
                        }
                    }else{
                        BottomDialog.build()
                                .setStyle(MIUIStyle.style())
                                .setTitle("抱歉，未能处理您的请求")
                                .setMessage("请确保信息填写正确并无遗漏")
                                .show();
                    }
                }

            }
        });

        new Thread(new Runnable() { // 校验输入框里的数据是否规范 规范则更改边框样式(注册)
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mHandler.sendEmptyMessage(40);
                }
            }
        }).start();



        // 上方选项卡监听
        flipTab.setTabSelectedListener(new FlipTab.TabSelectedListener() {
            @Override
            public void onTabSelected(boolean b, @NonNull String s) {
                System.out.println(s);
                if (s.equals("Sign up")){ // 注册
                    mHandler.sendEmptyMessage(10);
                }else{ // 登录
                    mHandler.sendEmptyMessage(11);
                }
            }

            @Override
            public void onTabReselected(boolean b, @NonNull String s) {

            }
        });


        // 初始界面中心圆点点击监听
        mCvStartBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCvStartBlur.setClickable(false); // 设置为不可点击，避免重复点击出现动画BUG
                mIvBackground.animate().scaleX(1.5F).scaleY(1.5F).setDuration(1000).start(); // 放大背景
                final int[] addX = {0}; // 加速度初始值

                new Thread(new Runnable() { // W
                    @Override
                    public void run() {
                        for (int x = 200; x < 6000; x = x + addX[0]){
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            addX[0] = addX[0] + 1; // 加速度
                            rX = x;
                            mHandler.sendEmptyMessage(0); // 发送信息让cv更改长宽
                        }
                    }
                }).start();

                new Thread(new Runnable() { // H
                    @Override
                    public void run() {
                        for (int x = 200; x < 6000; x = x + addX[0]){
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            addX[0] = addX[0] + 1;
                            rY = x;
                            mHandler.sendEmptyMessage(0);
                        }

                    }
                }).start();

                // 全部缩小至消失
                mTvStart.animate().scaleX(0).scaleY(0).setDuration(1000).start();
                mTvTouchStart.animate().scaleX(0).scaleY(0).alpha(0.5F).setDuration(500).start();
                new Thread(new Runnable() { // 因为[onAnimationEnd]会执行两次，所以手动设置延迟器
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            mHandler.sendEmptyMessage(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });


    }


    private class MyHandler extends Handler {

        public MyHandler(MainActivity activity) {
            WeakReference<MainActivity> mTarget = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                // cardview(中心模糊圆点) 设置宽高(向外扩散)
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCvStartBlur.getLayoutParams();
                params.width = rX;
                params.height = rY;
                mCvStartBlur.setLayoutParams(params);
            }
            if (msg.what == 1){ // 点击按钮后延迟触发
                stickySwitch.animate().scaleX(0).scaleY(0).setDuration(0).start();
                sta.animate().scaleX(0).scaleY(0).setDuration(0).start();
                flipTab.animate().scaleX(0).scaleY(0).setDuration(0).start();
                mCvLogin.animate().scaleX(0).scaleY(0).setDuration(0).start();

                stickySwitch.setVisibility(View.VISIBLE);
                sta.setVisibility(View.VISIBLE);
                flipTab.setVisibility(View.VISIBLE);
                mCvLogin.setVisibility(View.VISIBLE);

                stickySwitch.animate().scaleX(1).scaleY(1).setInterpolator(new DecelerateInterpolator()).setDuration(500).start();
                sta.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
                flipTab.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
                mCvLogin.animate().scaleX(1).scaleY(1).setDuration(400).setInterpolator(new DecelerateInterpolator()).start();
            }

            if (msg.what == 10){ // Login
                stickySwitch.animate().scaleX(0).scaleY(0).setDuration(200).alpha(0).start();
                sta.animate().scaleX(0).scaleY(0).setDuration(300).alpha(0).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                            mHandler.sendEmptyMessage(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                mCvLogin.animate().scaleX(0).scaleY(0).setDuration(300).alpha(0).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                            mHandler.sendEmptyMessage(21);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
            if (msg.what == 11){ // Sign up
                sta_reg.animate().scaleX(0).scaleY(0).setDuration(200).start();
                mCvReg.animate().scaleX(0).scaleY(0).setDuration(300).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                            mHandler.sendEmptyMessage(22);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                            mHandler.sendEmptyMessage(23);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            if (msg.what == 40){ // 校验数据是否规范，规范更改绿色边框
                if (Validator.isEmail(mEtEmailReg.getText().toString())){
                    mEtEmailReg.setBackgroundResource(R.drawable.et_bg_ok);
                }else{
                    mEtEmailReg.setBackgroundResource(R.drawable.btn_ripple3);
                }

                if (!mEtUsernameReg.getText().toString().equals("")){
                    mEtUsernameReg.setBackgroundResource(R.drawable.et_bg_ok);
                }else{
                    mEtUsernameReg.setBackgroundResource(R.drawable.btn_ripple3);
                }

                if (!mEtPasswordReg.getText().toString().equals("")){
                    mEtPasswordReg.setBackgroundResource(R.drawable.et_bg_ok);
                }else{
                    mEtPasswordReg.setBackgroundResource(R.drawable.btn_ripple3);
                }
            }

            if (msg.what == 20){ // >msg.what == 10(注册选项)< 手动延迟200ms后调用
                sta_reg.animate().scaleX(0).scaleY(0).setDuration(0).start();
                mCvReg.animate().scaleX(0).scaleY(0).setDuration(0).start();

                sta_reg.setVisibility(View.VISIBLE);
                mCvReg.setVisibility(View.VISIBLE);

                sta_reg.animate().scaleX(1).scaleY(1).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                mCvReg.animate().scaleX(1).scaleY(1).setDuration(200).setInterpolator(new DecelerateInterpolator()).start();
            }
            if (msg.what == 21){ // >msg.what == 10< 手动延迟300ms后调用
                stickySwitch.setVisibility(View.INVISIBLE);
                sta.setVisibility(View.INVISIBLE);
                mCvLogin.setVisibility(View.INVISIBLE);
            }
            if (msg.what == 22){ // >msg.what == 11(登录选项)< 手动延迟200ms后调用
                stickySwitch.setVisibility(View.VISIBLE);
                sta.setVisibility(View.VISIBLE);
                mCvLogin.setVisibility(View.VISIBLE);

                stickySwitch.animate().scaleX(1).scaleY(1).alpha(1).setInterpolator(new DecelerateInterpolator()).setDuration(200).start();
                sta.animate().scaleX(1).scaleY(1).setDuration(200).alpha(1).setInterpolator(new DecelerateInterpolator()).start();
                mCvLogin.animate().scaleX(1).scaleY(1).setDuration(300).alpha(1).setInterpolator(new DecelerateInterpolator()).start();
            }
            if (msg.what == 23){ // >msg.what == 11< 手动延迟300ms后调用
                sta_reg.setVisibility(View.INVISIBLE);
                mCvReg.setVisibility(View.INVISIBLE);
            }


            /*
            * 3x -> 输入登录信息错误后晃动cardview
            * */
            if (msg.what == 31){
                mCvLogin.animate().translationX(30).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(32);
                    }
                }).start();
            }
            if (msg.what == 32){
                mCvLogin.animate().translationX(-30).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(33);
                    }
                }).start();
            }
            if (msg.what == 33){
                mCvLogin.animate().translationX(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(34);
                    }
                }).start();
            }
            if (msg.what == 34){
                mCvLogin.animate().scaleX(1).scaleY(1).setDuration(300);
            }


            /*
            * 模糊背景
            * */
            if (msg.what == 100){
                loadingPopupView.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int x = 0; x < 500; x++){
                            mRBackground.setBlurRadius(x);
                        }
                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(10086);
                    }
                }).start();
            }

            /*
             * 弹出协议等控件
             * */
            if (msg.what == 10086){
                mRBackground.setBlurRadius(0
                );
                loadingPopupView.dismiss();
                mRlMain.animate().scaleX(0).scaleY(0).alpha(0).setDuration(300).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(301);
                    }
                }).start();
            }


            if (msg.what == 301){
                mRlRule.animate().alpha(0).scaleX(0).scaleY(0).setDuration(0).start();
                mRlRule.setVisibility(View.VISIBLE);
                mRlRule.animate().alpha(1).scaleX(1).scaleY(1).setInterpolator(new DecelerateInterpolator()).setDuration(300).start();
            }
        }
    }


    /**
     * 验证器，来源网络
     * */
    public static class Validator {
        /**
         * 正则表达式：验证用户名
         */
        public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";

        /**
         * 正则表达式：验证密码
         */
        public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";

        /**
         * 移动手机号码的正则表达式。
         */
        private static final String REGEX_CHINA_MOBILE ="1(3[4-9]|4[7]|5[012789]|8[278])\\d{8}";

        /**
         * 联通手机号码的正则表达式。
         */
        private static final String REGEX_CHINA_UNICOM = "1(3[0-2]|5[56]|8[56])\\d{8}";

        /**
         * 电信手机号码的正则表达式。
         */
        private static final String REGEX_CHINA_TELECOM = "(?!00|015|013)(0\\d{9,11})|(1(33|53|80|89)\\d{8})";

        /**
         * 正则表达式：验证手机号
         */
        private static final String REGEX_PHONE_NUMBER = "^(0(10|2\\d|[3-9]\\d\\d)[- ]{0,3}\\d{7,8}|0?1[3584]\\d{9})$";

        /**
         * 正则表达式：验证邮箱
         */
        public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

        /**
         * 正则表达式：验证汉字
         */
        public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5],{0,}$";

        /**
         * 正则表达式：验证身份证
         */
        public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

        /**
         * 正则表达式：验证URL
         */
        public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

        /**
         * 正则表达式：验证IP地址
         */
        public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

        /**
         * 校验用户名
         *
         * @param username
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isUsername(String username) {
            return Pattern.matches(REGEX_USERNAME, username);
        }

        /**
         * 校验密码
         *
         * @param password
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isPassword(String password) {
            return Pattern.matches(REGEX_PASSWORD, password);
        }

        /**
         * 校验手机号
         *
         * @param mobile
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isMobile(String mobile) {
            return Pattern.matches(REGEX_PHONE_NUMBER, mobile);
        }

        /**
         * 校验邮箱
         *
         * @param email
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isEmail(String email) {
            return Pattern.matches(REGEX_EMAIL, email);
        }

        /**
         * 校验汉字
         *
         * @param chinese
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isChinese(String chinese) {
            return Pattern.matches(REGEX_CHINESE, chinese);
        }

        /**
         * 校验身份证
         *
         * @param idCard
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isIDCard(String idCard) {
            return Pattern.matches(REGEX_ID_CARD, idCard);
        }

        /**
         * 校验URL
         *
         * @param url
         * @return 校验通过返回true，否则返回false
         */
        public static boolean isUrl(String url) {
            return Pattern.matches(REGEX_URL, url);
        }

        /**
         * 校验IP地址
         *
         * @param ipAddr
         * @return
         */
        public static boolean isIPAddr(String ipAddr) {
            return Pattern.matches(REGEX_IP_ADDR, ipAddr);
        }

    }

    /**
     * 将数据进行 MD5 加密，并以16进制字符串格式输出
     * @param data
     * @return
     */
    public static String md5(String data) {
        try {
            byte[] md5 = md5(data.getBytes("utf-8"));
            return toHexString(md5);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将字节数组进行 MD5 加密
     * @param data
     * @return
     */
    public static byte[] md5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    /**
     * 将加密后的字节数组，转换成16进制的字符串
     * @param md5
     * @return
     */
    private static String toHexString(byte[] md5) {
        StringBuilder sb = new StringBuilder();
        System.out.println("md5.length: " + md5.length);
        for (byte b : md5) {
            sb.append(Integer.toHexString(b & 0xff));
        }
        return sb.toString();
    }
}