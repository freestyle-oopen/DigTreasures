package com.trust.walletcore.example;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.trust.walletcore.example.bean.Bean;
import com.trust.walletcore.example.bean.Result;
import com.trust.walletcore.example.data.DatabaseHelper;
import com.trust.walletcore.example.utils.FileUtils;
import com.trust.walletcore.example.utils.NetUtils;

import org.bouncycastle.pqc.jcajce.provider.util.KeyUtil;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jnr.ffi.Struct;
import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;
import wallet.core.jni.PrivateKey;

public class MainActivity extends AppCompatActivity {
    //https://api.etherscan.io/api?module=proxy&action=eth_getBlockByNumber&tag=0xd50011&boolean=true&apikey=D89Q7WEI4VEAF6HX33T5X58BP17ENDYAU4
    //https://api-cn.etherscan.com/api?module=proxy&action=eth_getBlockByNumber&tag=0xd50011&boolean=true&apikey=D89Q7WEI4VEAF6HX33T5X58BP17ENDYAU4
    //private String baseUrl ="https://api.etherscan.io/api?module=account&action=balancemulti&address=RWOEIUJGIF&tag=latest&apikey=237QGBE9MHKQGKR2Z5H2WHADET31FUQAE2";

    //String projectId = "35cae4735c90460db1baf450f16306ce";
    //String url = "https://mainnet.infura.io/v3/" + projectId;
    private String baseUrl = "https://api-cn.etherscan.com/api?module=account&action=balancemulti&address=RWOEIUJGIF&tag=latest&apikey=MYKEYKEY";

    private static final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private static final String scussfulStatus = "\"status\":\"1\"";
    private DatabaseHelper databaseHelper;
    TextView cmd;
    TextView speed;
    TextView averageSpeed;
    boolean isStart = false;
    Gson gson;
    //挖到宝贝的总数
    long havemoneyCount = 0;
    //处理完发送事件的总数
    long resultCount = 0;
    //发送的总数
    long requestCount = 0;
    String result = "";

    int count = 20;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    String text = (String) msg.obj;
                    showText(cmd, text);
                    havemoneyCount++;
                    wabaoSuccessView.setText("成功挖宝：" + havemoneyCount);
                case 2://返回结果
                    String result1 = (String) msg.obj;
                    resultCount++;
                    if (result1 == null || "".equals(result1)) {
                        result1 = "null";
                    }
                    result=result1;
                    break;
                case 3:
                    wabaoCountView.setText("挖宝：" + resultCount);
                    fasongView.setText("发送：" + requestCount);
                    netResult.setText(result);
                    String speedNum = (String) msg.obj;
                    String[] split = speedNum.split(":");
                    speed.setText(split[0]+"个/秒");
                    averageSpeed.setText(split[1]+"个/秒");
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Timer timer;
    private Timer timerData;
    private TextView wabaoCountView;
    private TextView wabaoSuccessView;
    private TextView netResult;
    private TextView fasongView;
    private EditText xianchengCount;
    private ExecutorService scheduledExecutorService;
    private View layoutBg;
    private long startTime;
    private long previousTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);
        cmd = findViewById(R.id.zhuangtai);
        speed = findViewById(R.id.speed);
        averageSpeed = findViewById(R.id.tv_average_speed);
        wabaoCountView = findViewById(R.id.wabao_count);
        wabaoSuccessView = findViewById(R.id.wabaosuccess_count);
        netResult = findViewById(R.id.net_result);
        fasongView = findViewById(R.id.fasong_count);
        xianchengCount = findViewById(R.id.xiancheng_count);
        layoutBg = findViewById(R.id.layout_bg);
        layoutBg.setPadding(0, 90, 0, 0);
        gson = new Gson();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> pers = new ArrayList<>();
            for (String permission : permissions) {
                int result = checkSelfPermission(permission);
                if (result == -1) {
                    pers.add(permission);
                }
            }
            if (pers != null && pers.size() > 0) {
                this.requestPermissions(pers.toArray(new String[]{}), 1110001);
            } else {
                String fileName = Environment.getExternalStorageDirectory() + "/AAeth/havemoneyadd.txt";
                File file = new File(fileName);
                if (file.exists()) {
                    havemoneyCount++;
                    wabaoSuccessView.setText("成功挖宝：" + havemoneyCount);
                }
            }
        }

    }
    public void imputWall(View view) throws InterruptedException, NoSuchAlgorithmException {
        TextView textView = (TextView) view;
        if (isStart) {
            return;
        }
        startTime = System.currentTimeMillis() + 500;
        previousTime = startTime;
        textView.setText("停止");
        Toast.makeText(this, "开始挖宝", Toast.LENGTH_SHORT).show();
        isStart = true;
        showText(cmd, "正在挖宝...");
        if (scheduledExecutorService == null) {
            String trim = xianchengCount.getText().toString().trim();
            int count = 0;
            try {
                count = Integer.parseInt(trim);
            } catch (Exception e) {
            }
            if (count <= 0) {
                count = 20;
                xianchengCount.setText(count + "");
            }
            xianchengCount.setEnabled(false);
            scheduledExecutorService = Executors.newFixedThreadPool(count);
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (requestCount > resultCount + 1000) {
                    return;
                }
                requestCount++;
                String[] keys = com.trust.walletcore.example.utils.Keys.KEYS;
                int type = (int) (requestCount % keys.length);
                String userKey = keys[type];
                scheduledExecutorService.execute(new MyRunnable(userKey));
            }
        };
        timer = new Timer();
        timerData = new Timer();
        // 定义开始等待时间  --- 等待 5 秒
        // 1000ms = 1s
        long delay = 500;
        // 定义每次执行的间隔时间
        long intevalPeriod = 10;
        // schedules the task to be run in an interval
        // 安排任务在一段时间内运行
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);
        timerData.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long l = currentTime - previousTime;
                previousTime = currentTime;
                long counts = resultCount - oneMC;
                oneMC = resultCount;
                double miao = l*1.0/1000;
                long count = counts * 20;
                double speedNum =count / miao;
                long l1 = currentTime - startTime;
                double miao1 = l1*1.0/1000;
                double speedNum1 =(resultCount*20) / miao1;
                String format = String.format("%.0f", speedNum);
                String format1 = String.format("%.0f", speedNum1);
                sendHandler(3, format+":"+format1);
            }
        },500,1000);
    }
    long oneMC = 0;

    class MyRunnable implements Runnable {
        private String parem;

        public MyRunnable(String parem) {
            this.parem = parem;
        }

        @Override
        public void run() {
            String result = null;
            try {
                long old1 = System.currentTimeMillis();
                List<String> zjcs = new ArrayList<>();
                StringBuilder address = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                    BigInteger privateKey = ecKeyPair.getPrivateKey();
                    String privateKeyStr = String.format("0x%064x", privateKey);
                    String address1 = "0x"+Keys.getAddress(ecKeyPair);
                    /*HDWallet hdWallet = new HDWallet(128, "saaaaaaaaaa");
                    PrivateKey keyForCoin = hdWallet.getKeyForCoin(CoinType.ETHEREUM);
                    byte[] data = keyForCoin.data();
                    hdWallet.nativeDelete(hdWallet.nativeHandle);
                    String privateKeyStr = bytesToHex(data);
                    ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKeyStr));
                    Credentials credentials = Credentials.create(keyPair);
                    String address1 = credentials.getAddress();*/
                    address.append(address1).append(",");
                    zjcs.add(privateKeyStr);
                }
                String addresses = address.toString();
                addresses = addresses.substring(0, addresses.lastIndexOf(","));
                long old2 = System.currentTimeMillis();
                String url = baseUrl.replaceAll("RWOEIUJGIF", addresses);
                url = url.replaceAll("MYKEYKEY", parem);
                result = NetUtils.doGet(url);
                long newtime = System.currentTimeMillis();
                String name = Thread.currentThread().getName();
                Log.i("kkkkkddddd", name + "  " + (old2-old1) +" ----> " + (newtime-old2)+"    总："+(newtime-old1));
                if (result != null && !"".equals(result) && result.contains(scussfulStatus)) {
                    String[] split = result.split("\"balance\":\"0\"");
                    if (split.length > count) {
                        Bean bean = gson.fromJson(result, Bean.class);
                        if(bean != null && bean.getResult() != null){
                            int size = bean.getResult().size();
                            StringBuilder sb = new StringBuilder();
                            boolean isNeedShow = false;
                            for (int j=0; j<size; j++){
                                Result account = bean.getResult().get(j);
                                String balance = account.getBalance();
                                if(balance != null && balance.length() > (count - 19)){
                                    //databaseHelper.insertData(zjcs.get(j), balance);
                                    sb.append(zjcs.get(j)+":"+balance+"\n");
                                    if(balance.length() >= 0){
                                        isNeedShow = true;
                                    }
                                }
                            }
                            if(!TextUtils.isEmpty(sb.toString())){
                                sb.append("\n");
                                String content = sb.toString();
                                String fileName = Environment.getExternalStorageDirectory() + "/AAeth/havemoneyadd.txt";
                                FileUtils.appendStringIntoFile(fileName, content);
                                String showContent = "";
                                if(isNeedShow){
                                    showContent = content;
                                }
                                sendHandler(1, showContent);
                            }
                        }
                    }
                }else{
                    Log.i("renyukai","result="+result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = e.toString();
            }
            sendHandler(2, result);
        }
    }

    private void sendHandler(int what, Object obj){
        Message obtain = Message.obtain();
        obtain.what = what;
        obtain.obj = obj;
        handler.sendMessage(obtain);
    }
    private void showText(TextView textView, String content) {
        content = content + "\n";
        String trim = textView.getText().toString().trim();
        textView.setText(content + trim);
    }

    public void mode(View view) {
        TextView textView = (TextView) view;
        String trim = textView.getText().toString().trim();
        if ("夜".equals(trim)) {
            layoutBg.setBackgroundColor(getResources().getColor(R.color.blackcolor));
            textView.setText("日");
        } else {
            layoutBg.setBackgroundColor(getResources().getColor(R.color.baise));
            textView.setText("夜");
        }
    }

    @Override
    public void onBackPressed() {

    }

    public void deleteSuccessData(View view) {
        if (havemoneyCount <= 0) {
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);//通过AlertDialog.Builder创建出一个AlertDialog的实例

        dialog.setTitle("警告！");//设置对话框的标题
        dialog.setMessage("请谨慎删除，确定要删除已经挖到的币吗？");//设置对话框的内容
        dialog.setCancelable(false);//设置对话框是否可以取消
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {//确定按钮的点击事件

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = Environment.getExternalStorageDirectory() + "/AAeth/havemoneyadd.txt";
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                    havemoneyCount = 0;
                    wabaoSuccessView.setText("成功挖宝：" + havemoneyCount);
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {//取消按钮的点击事件
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();//显示对话框
    }
    public void test(View view) {
        if (count == 20) {
            showText(cmd, "开启测试");
            count = 21;
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showText(cmd, "关闭测试");
                    count = 20;
                }
            }, 5000);
        }
    }
}
