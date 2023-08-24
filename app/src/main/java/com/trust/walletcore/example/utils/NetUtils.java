package com.trust.walletcore.example.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class NetUtils {
    public static String doGet(String httpUrl){
        //链接
        HttpsURLConnection connection = null;
        InputStream is = null;
        String result = null;
        try {
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpsURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            //开始连接
            connection.connect();
            //获取响应数据
            //if (connection.getResponseCode() == 200) {
            //获取返回的数据
            is = connection.getInputStream();
            if (null != is) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[32*1024];
                while ((len = is.read(bytes)) != -1) {
                    byteArrayOutputStream.write(bytes, 0, len);
                }
                result = byteArrayOutputStream.toString();
                byteArrayOutputStream.close();
            }
            //}
        } catch (Exception e) {
            result = e.toString();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
                //connection = null;
            }
        }
        return result;
    }
    public static String doGetK(String httpUrl){
        //链接
        HttpsURLConnection connection = null;
        InputStream is = null;
        String result = null;
        try {
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpsURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            //开始连接
            connection.connect();
            //获取响应数据
            //if (connection.getResponseCode() == 200) {
            //获取返回的数据
            is = connection.getInputStream();
            if (null != is) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                result = bufferedReader.readLine();
            }
            //}
        } catch (Exception e) {
            result = e.toString();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
                //connection = null;
            }
        }
        return result;
    }
    public static String doGetZ(String httpUrl){
        //链接
        HttpsURLConnection connection = null;
        InputStream is = null;
        String result = null;
        try {
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpsURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            //开始连接
            connection.connect();
            //获取响应数据
            //if (connection.getResponseCode() == 200) {
            //获取返回的数据
            is = connection.getInputStream();
            if (null != is) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[32*1024];
                while ((len = bufferedInputStream.read(bytes)) != -1) {
                    byteArrayOutputStream.write(bytes, 0, len);
                }
                result = byteArrayOutputStream.toString();
                byteArrayOutputStream.close();
                bufferedInputStream.close();
            }
            //}
        } catch (Exception e) {
            result = e.toString();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
                //connection = null;
            }
        }
        return result;
    }

    public static String doPost(String url, String data){

        //String requestBody = "[{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"eth_getBalance\", \"params\": [\"0xCFcb12D849569A3d1Cf971721665B2dCe0279A91\",\"latest\"]}]";
        String result = null;
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // 启用输出流，用于发送请求体数据
            connection.setDoOutput(true);

            // 写入请求体数据
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();

            // 读取响应内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            result = response.toString();
            connection.disconnect();
        } catch (IOException e) {
            result = e.toString();
            e.printStackTrace();
        }
        return result;
    }
}
