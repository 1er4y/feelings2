package com.sloth.feelings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class ShareToServer {

   // private String result = "";
    private boolean result = false;
    private String msg;


    public boolean shareRegIdWithAppServer(final Context context, final String regId, final String currentUserId, final String secondUserId) throws IOException {

        //String urlParameters = "regId" + "=" + regId + "&" + "currentUserId" + "=" + currentUserId + "&" + "secondUserId" + "=" + secondUserId;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://androidserver-jxbj8qesua.elasticbeanstalk.com/DatabaseServlet");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action","registration"));
        params.add(new BasicNameValuePair("regId", regId));
        params.add(new BasicNameValuePair("currentUserId", currentUserId));
        params.add(new BasicNameValuePair("secondUserId", secondUserId));
        HttpResponse response = null;
        Scanner in = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(post);
            // System.out.println(response.getStatusLine());
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                result = true;
            } else {
               result = false;
            }
            HttpEntity entity = response.getEntity();
            in = new Scanner(entity.getContent());
            while (in.hasNext()) {
                System.out.println(in.next());

            }
            //EntityUtils.consume(entity);
        } finally {
            in.close();
            //response.;
        }
        return result;
    }

    public boolean sendMessage(final String currentUserId,
                              final String secondUserId, final String messageToSend) {
        boolean resp = false;
        String result = "";
        try {
            msg =  URLEncoder.encode(messageToSend, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Map<String, String> paramsMap = new HashMap<String, String>();
        List<NameValuePair> paramsMap = new ArrayList<>();
        paramsMap.add(new BasicNameValuePair("action", "sendMessage"));
        paramsMap.add(new BasicNameValuePair(Config.REGISTER_ID, currentUserId));
        paramsMap.add(new BasicNameValuePair(Config.TO_ID, secondUserId));
        paramsMap.add(new BasicNameValuePair(Config.MESSAGE_KEY, msg));
        try {
            resp = request(paramsMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ("success".equalsIgnoreCase(result)) {
            result = "Message " + messageToSend + " sent to user " + secondUserId
                    + " successfully.";
        }
        Log.d("ShareExternalServer", "Result: " + result);
        return resp;
    }

    public boolean request(List<NameValuePair> paramsMap) throws IOException {

        boolean resp = false;

        HttpClient httpClient2 = new DefaultHttpClient();
        HttpPost post2 = new HttpPost("http://androidserver-jxbj8qesua.elasticbeanstalk.com/DatabaseServlet");


        HttpResponse response = null;
        Scanner in = null;
        try {
            post2.setEntity(new UrlEncodedFormEntity(paramsMap));
            response = httpClient2.execute(post2);
            // System.out.println(response.getStatusLine());

            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                result = true;
            } else {
                result = false;
            }

            HttpEntity entity = response.getEntity();

            in = new Scanner(entity.getContent());
            while (in.hasNext()) {
                System.out.println(in.next());

            }
            //EntityUtils.consume(entity);
        } finally {
            in.close();
            //response.;
        }

        return resp;
    }

    public boolean cancelSynchronization (final String currentUserId)

    {
        boolean resp = false;
        String result = "";
        //Map<String, String> paramsMap = new HashMap<String, String>();
        List<NameValuePair> paramsMap = new ArrayList<>();
        paramsMap.add(new BasicNameValuePair("action", "cancelSync"));
        paramsMap.add(new BasicNameValuePair(Config.REGISTER_ID, currentUserId));
       // paramsMap.add(new BasicNameValuePair(Config.TO_ID, secondUserId));
        //paramsMap.add(new BasicNameValuePair(Config.MESSAGE_KEY, messageToSend));
        try {
            resp = request(paramsMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("ShareExternalServer", "Cancel Sync: " + result);
        return resp;

    }
        /*
    public String shareRegIdWithAppServer(final Context context,
                                          final String regId) {

        String result = "";
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("regId", regId);
        try {
            URL serverUrl = null;
            try {
                serverUrl = new URL(Config.APP_SERVER_URL);
            } catch (MalformedURLException e) {
                Log.e("AppUtil", "URL Connection Error: "
                        + Config.APP_SERVER_URL, e);
                result = "Invalid URL: " + Config.APP_SERVER_URL;
            }

            StringBuilder postBody = new StringBuilder();
            Iterator<Entry<String, String>> iterator = paramsMap.entrySet()
                    .iterator();

            while (iterator.hasNext()) {
                Entry<String, String> param = iterator.next();
                postBody.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            byte[] bytes = body.getBytes();
            HttpURLConnection httpCon = null;
            try {
                httpCon = (HttpURLConnection) serverUrl.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setUseCaches(false);
                httpCon.setFixedLengthStreamingMode(bytes.length);
                httpCon.setRequestMethod("POST");
                httpCon.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                OutputStream out = httpCon.getOutputStream();
                out.write(bytes);
                out.close();

                int status = httpCon.getResponseCode();
                if (status == 200) {
                    result = "RegId shared with Application Server. RegId: "
                            + regId;
                } else {
                    result = "Post Failure." + " Status: " + status;
                }
            } finally {
                if (httpCon != null) {
                    httpCon.disconnect();
                }
            }

        } catch (IOException e) {
            result = "Post Failure. Error in sharing with App Server.";
            Log.e("AppUtil", "Error in sharing with App Server: " + e);
        }
        return result;
    }*/
}