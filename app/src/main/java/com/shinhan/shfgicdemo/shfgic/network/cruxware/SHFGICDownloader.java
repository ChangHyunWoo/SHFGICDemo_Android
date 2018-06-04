package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SHFGICDownloader {
    static private boolean          __testModeEnabled = false;
    public static void testModeEnabled() { __testModeEnabled = true;}

    private String _tag;
    private String _path;
    private HashMap<String, Object> _userInfo = new HashMap<String, Object>();

    public SHFGICDownloader() {

    }

    public SHFGICDownloader(String tag) {
        _tag = tag;
    }

    public void setTag(String tag) {
        _tag = tag;
    }

    public String tag() {
        return _tag;
    }

    public void putUserInfo(String key, Object object) {
        if(object == null) return;

        _userInfo.put(key, object);
    }

    public void removeUsetInfo(String key) {
        if( !_userInfo.containsKey(key) )
            return;;

        _userInfo.remove(key);
    }

    public Object usetInfo(String key) {
        if( !_userInfo.containsKey(key) )
            return null;

        return _userInfo.get(key);
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            //TODO TRACE
        }
    }


    public void download(String path, ResultListener listener) {
        _path           = path;
        _resultListener = listener;

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] stream = null;
                String[] result = new String[5];

                HttpURLConnection connection = null;
                InputStream is = null;
                BufferedInputStream bis = null;

                Uri uri = Uri.parse(_path);
                String scheme = uri.getScheme();
                try {
                    if ("http".equals(scheme)) {
                        connection = (HttpURLConnection) new URL(_path).openConnection();
                    } else if ("https".equals(scheme)) {
                        connection = (HttpsURLConnection) new URL(_path).openConnection();

                        if( __testModeEnabled ) {
                            trustAllHosts();
                            ((HttpsURLConnection)connection).setHostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String s, SSLSession sslSession) {
                                    return true;
                                }
                            });
                        }
                    }

                    connection.setReadTimeout(60000);
                    connection.setConnectTimeout(60000);
                    connection.addRequestProperty("Connection", "close");

                    int responseCode = connection.getResponseCode();
                    String responseMessage = connection.getResponseMessage();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new Exception("" + responseCode + " : " + responseMessage);
                    }

                    if ("gzip".equals(connection.getContentEncoding()))
                        is = new GZIPInputStream(connection.getInputStream());
                    else {
                        is = connection.getInputStream();
                    }

//                    bis = new BufferedInputStream(is, 8192);
//
//                    byte[] buffer = new byte[8192];
//                    int read = 0;
//                    int total = 0;
//                    int length = connection.getContentLength();
//
//                    int accmulate = 0;
//                    while ((read = bis.read(buffer, 0, 8192)) > 0) {
//                        if( stream != null ) {
//                            byte[] temp = stream;
//                            stream = new byte[accmulate + read];
//                            System.arraycopy(temp, 0, stream, 0, accmulate);
//                            System.arraycopy(buffer, 0, stream, accmulate, read);
//                            accmulate += read;
//                        } else {
//                            stream = new byte[read];
//                            System.arraycopy(buffer, 0, stream, 0, read);
//                            accmulate += read;
//                        }
//                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    String string = response.toString();
                    stream = string.getBytes("utf-8");

                    // 2017.01.12 Lee.Jungin callback 처리 순서 변경(강성욱C 요청사항)
                    if( _resultListener != null )
                        _resultListener.onDownloadFinished(SHFGICDownloader.this, stream);
                } catch (Exception e) {
                    if( _resultListener != null )
                        _resultListener.onDownloadFailed(SHFGICDownloader.this, e);
                } finally {

                }
            }
        }).start();
    }

    ResultListener _resultListener = null;
    public interface ResultListener {
        void onDownloadFailed(SHFGICDownloader downloader, Exception exception);
        void onDownloadFinished(SHFGICDownloader downloader, byte[] stream);
    }

    public void setResultListener(ResultListener listener) {
        _resultListener = listener;
    }

    public ResultListener resultListener() {
        return _resultListener;
    }

}
