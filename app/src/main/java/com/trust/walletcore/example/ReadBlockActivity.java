package com.trust.walletcore.example;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.trust.walletcore.example.data.DatabaseHelper;
import com.trust.walletcore.example.utils.FileUtils;
import com.trust.walletcore.example.utils.Keys;
import com.trust.walletcore.example.utils.NetUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadBlockActivity extends AppCompatActivity {

    private String baseUrl = "https://api-cn.etherscan.com/api?module=proxy&action=eth_getBlockByNumber&boolean=true&tag=RWOEIUJGIF&apikey=MYKEYKEY";

    private static final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private static final int WORK_ERROR = 1;
    private static final int WORK_DONE = 2;
    private static final int WORK_UPDATE = 3;

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private static final String scussfulStatus = "\"transactions\":";
    private DatabaseHelper databaseHelper;
    private long[] keyUseTime;
    TextView cmd;
    TextView speed;
    TextView averageSpeed;
    boolean isStart = false;
    Gson gson;
    int currentBlock = 0;
    long errorBlockCount = 0;
    long failBlockCount = 0;
    //处理完发送事件的总数
    int cloudReturnBlockCount = 0;
    //发送的总数
    long requestCount = 0;
    String resultLog = "";

    private Timer timer;
    private Timer timerData;
    private TextView mCloudReturnBlockCountView;
    private TextView mErrorBlockCountView;
    private TextView netResult;
    private TextView mSendBlockCountView;
    private TextView mCurrentBlockView;
    private TextView mFaillockCountView;
    private TextView allAddressCount;
    private EditText xianchengCount;
    private ThreadPoolExecutor scheduledExecutorService;
    private View layoutBg;
    private long startTime;
    private long previousTime;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String handleMsg = (String) msg.obj;
            switch (msg.what) {
                case WORK_ERROR:
                    showText(cmd, handleMsg);
                    errorBlockCount++;
                    break;
                case WORK_DONE://返回结果
                    cloudReturnBlockCount++;
                    if (handleMsg != null && !"".equals(handleMsg)) {
                        resultLog = handleMsg;
                        failBlockCount++;
                    }
                    break;
                case WORK_UPDATE:
                    mSendBlockCountView.setText("发送区块：" + requestCount);
                    mCloudReturnBlockCountView.setText("返回区块：" + cloudReturnBlockCount);
                    mFaillockCountView.setText("失败区块：" + failBlockCount);
                    mErrorBlockCountView.setText("错误区块：" + errorBlockCount);
                    mCurrentBlockView.setText("当前区块：" + currentBlock);
                    netResult.setText(resultLog);
                    if (handleMsg != null) {
                        String[] split = handleMsg.split(":");
                        speed.setText(split[0] + "个/秒");
                        averageSpeed.setText(split[1] + "个/秒");
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        setContentView(R.layout.activity_readblock_layout);
        databaseHelper = new DatabaseHelper(this);
        cmd = findViewById(R.id.zhuangtai);
        speed = findViewById(R.id.speed);
        averageSpeed = findViewById(R.id.tv_average_speed);
        mCloudReturnBlockCountView = findViewById(R.id.tv_cloud_return_block);
        mErrorBlockCountView = findViewById(R.id.tv_error_block);
        netResult = findViewById(R.id.net_result);
        mSendBlockCountView = findViewById(R.id.tv_send_block);
        mCurrentBlockView = findViewById(R.id.tv_current_block);
        mFaillockCountView = findViewById(R.id.tv_fail_block);
        xianchengCount = findViewById(R.id.xiancheng_count);
        allAddressCount = findViewById(R.id.tv_all_address_count);
        layoutBg = findViewById(R.id.layout_bg);
        layoutBg.setPadding(0, 90, 0, 0);
        gson = new Gson();
        keyUseTime = new long[Keys.KEYS.length];
        currentBlock = databaseHelper.selectCurrentBlock();
        sendHandler(WORK_UPDATE, null);
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
            }
        }

    }

    public void imputWall(View view) {
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
            scheduledExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(count);

        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (requestCount > cloudReturnBlockCount + 1000) {
                    return;
                }
                requestCount++;
                scheduledExecutorService.execute(new MyRunnable());
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
                long counts = cloudReturnBlockCount - oneMC;
                oneMC = cloudReturnBlockCount;
                double miao = l * 1.0 / 1000;
                double speedNum = counts / miao;
                long l1 = currentTime - startTime;
                double miao1 = l1 * 1.0 / 1000;
                double speedNum1 = cloudReturnBlockCount / miao1;
                String format = String.format("%.0f", speedNum);
                String format1 = String.format("%.0f", speedNum1);
                sendHandler(WORK_UPDATE, format + ":" + format1);
            }
        }, 500, 1000);
    }

    long oneMC = 0;

    private synchronized int[] getBlockAndKeyIndex() {
        long min = Long.MAX_VALUE;
        int indexMin = -1;
        for (int i = 0; i < keyUseTime.length; i++) {
            if (keyUseTime[i] < min) {
                min = keyUseTime[i];
                indexMin = i;
            }
        }
        keyUseTime[indexMin] = System.currentTimeMillis();
        return new int[]{indexMin, currentBlock++};
    }

    class MyRunnable implements Runnable {
        private int blockNum;

        @Override
        public void run() {
            String results = null;
            try {
                int[] blockAndKeyIndex = getBlockAndKeyIndex();
                String key = Keys.KEYS[blockAndKeyIndex[0]];
                blockNum = blockAndKeyIndex[1];
                String blockNumStr = Integer.toHexString(blockNum);
                String url = baseUrl.replaceAll("RWOEIUJGIF", blockNumStr);
                url = url.replaceAll("MYKEYKEY", key);
                long time1 = System.currentTimeMillis();
                String resultStr = NetUtils.doGet(url);
                long time2 = System.currentTimeMillis();
                if (resultStr != null && !"".equals(resultStr) && resultStr.contains(scussfulStatus)) {
                    String regex = "0x[0-9a-f]{40}(?=\")";
                    HashSet<String> hashSet = new HashSet<>();
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(resultStr);
                    StringBuilder stringBuilder = new StringBuilder();
                    while (matcher.find()) {
                        String group = matcher.group();
                        boolean add = hashSet.add(group);
                        if (add) {
                            stringBuilder.append("(\"" + group + "\",\"" + blockNum + "\"),");
                        }
                    }
                    String datas = stringBuilder.toString();
                    if (datas.endsWith(",")) {
                        datas = datas.substring(0, datas.length() - 1);
                    }
                    long time3 = System.currentTimeMillis();
                    databaseHelper.insertDatas(datas);
                    long time4 = System.currentTimeMillis();
                    Log.i("renyukai", "请求网络：" + (time2 - time1) + "    写入：" + (time4 - time3) + "        处理数据：" + (time3 - time2));
                } else {
                    results = resultStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
                results = e.toString();
            }
            if (results != null) {
                boolean b = false;
                try {
                    b = databaseHelper.insertFailBlock(blockNum);
                } catch (Exception e) {
                }
                if (!b) {
                    String fileName = Environment.getExternalStorageDirectory() + "/AAeth/error_blocks.txt";
                    FileUtils.appendStringIntoFile(fileName, blockNum + "\n");
                    sendHandler(WORK_ERROR, blockNum + " : " + results);
                }
                sendHandler(WORK_DONE, blockNum + " : " + results);
            } else {
                sendHandler(WORK_DONE, null);
            }

        }

    }


    public void getCount(View view) {
        int dataCount = databaseHelper.getDataCount();
        allAddressCount.setText("总地址数："+dataCount);
    }
    private void sendHandler(int what, Object obj) {
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


}
