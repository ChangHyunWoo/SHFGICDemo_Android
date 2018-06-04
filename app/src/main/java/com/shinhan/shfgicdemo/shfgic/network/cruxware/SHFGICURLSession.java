package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import com.shinhan.shfgicdemo.shfgic.SHFGICProperty;
import com.shinhan.shfgicdemo.util.LogUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SHFGICURLSession extends SHFGICSession {
    static private boolean          __testModeEnabled = true;
    public static void testModeEnabled() { __testModeEnabled = true;}

    private String _method = "POST";
    private int				        _timeout;
    private String _targetCharset = "utf-8";
    private String _localCharset = "utf-8";

    public void setTimeout(int timeout) { _timeout = timeout; }
    public int timeout() { return  _timeout; }

    public void setMethod(String method) { _method = method; }
    public String method() { return _method; }

    public void setCharset(String charSet) { _targetCharset = charSet; _localCharset = charSet; }

    public void setTargetCharset(String charSet) { _targetCharset = charSet; }
    public String targetCharset() { return _targetCharset; }

    public void setLocalCharset(String charSet) { _localCharset = charSet; }
    public String localCharset() { return _localCharset; }

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

    @Override
    protected SendType doSending(SHFGICTransaction transaction) throws Exception {
        //LogUtil.d("Cookie", "==> doSending(" + transaction.toString() + ")");
        String target           = target();
        String method           = _method;
        String targetCharset    = _targetCharset;
        String localCharset     = _localCharset;

        if( transaction instanceof SHFGICURLTransaction) {
            SHFGICURLTransaction urlTransaction = (SHFGICURLTransaction)transaction;

            if( urlTransaction.target() != null )
                target = ((SHFGICURLTransaction)transaction).target();

            if( urlTransaction.targetCharset() == null )
                urlTransaction.setTargetCharset(_targetCharset);

            if( urlTransaction.localCharset() == null )
                urlTransaction.setLocalCharset(_localCharset);

            targetCharset = urlTransaction.targetCharset();
            localCharset  = urlTransaction.localCharset();
        }

        URL url = new URL(target);
        //LogUtil.d("Cookie", "=========>>>>>>>>>>>>>>> target : " + target);

        if( __testModeEnabled )
            trustAllHosts();

        try {
            CookieManager manager = SHFGICProperty.getCookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
            CookieHandler.setDefault(manager);

            HttpURLConnection connection = null;

            if("https".equals(url.getProtocol().toLowerCase())) {
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

                https.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });

                connection = https;
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestProperty("Cookie", m_cookies);
            //LogUtil.d("Cookie", "----- Set Cookie " + connection.getRequestProperty("Cookie"));

            connection.setReadTimeout(transaction.timeout());
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setDoInput(true);

            byte [] stream = transaction.bytes();

            if( transaction instanceof SHFGICURLTransaction) {
                SHFGICURLTransaction urlTransaction = (SHFGICURLTransaction)transaction;

                HashMap<String, String> httpHeader = urlTransaction.httpHeader();

                Set<String> keys = httpHeader.keySet();

                for(String key : keys ) {
                    String value = httpHeader.get(key);
                    connection.setRequestProperty(key, value);
                }

                if( urlTransaction.method() != null )
                    method = urlTransaction.method();
            }

            // TODO: Kalce Cookie process. !!
            connection.setRequestMethod(method);

            if( method.equals("GET") ) {

            } else {
                connection.setDoOutput(true);
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.write(stream);
                os.flush();
                os.close();
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            LogUtil.d("Network", "=========>>>>>>>>>>>>>>> responseCode : " + responseCode);
            LogUtil.d("Network", "=========>>>>>>>>>>>>>>> responseMessage : " + responseMessage);

            // Session + Cookie
            saveCookie(connection);

//            LogUtil.d("CHW", "getRequest cookie = " + m_cookies);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new SHFGICTransactionException(SHFGICTransactionException.REQUEST_FAILED, "" + responseCode + " : " + responseMessage);
            }

            InputStream is = null;
            if ("gzip".equals(connection.getContentEncoding()))
                is = new GZIPInputStream(connection.getInputStream());
            else {
                is = connection.getInputStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, localCharset));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String string = response.toString();
            byte[] received = string.getBytes(localCharset);

//            CookieStore cookieJar = manager.getCookieStore();
//            List<HttpCookie> cookies = cookieJar.getCookies();
//            LogUtil.d("CHW", "cookies size = " + cookies.size());
//            for (HttpCookie c: cookies) {
//                LogUtil.d("CHW", "cookies = " + c);
//            }

            incomingResponse(received, transaction.identifier());
        } catch (SocketTimeoutException timeout) {
            throw new SHFGICTransactionException(SHFGICTransactionException.TIMEOUT, timeout.getLocalizedMessage());
        } catch (Exception e) {
            throw e;
        }

        return SendType.SYNC;
    }

    protected boolean onTransmit(SHFGICTransaction transaction) throws Exception {
        return true;
    }

    protected boolean onResponse(SHFGICTransaction transaction) throws Exception {
        return  true;
    }

    protected void onTransmitException(SHFGICTransaction transaction, SHFGICTransactionException exception) {
    }

    // Session + Cookie
    public static String m_cookies = "";

    public void saveCookie(HttpURLConnection conn) {
        Map<String, List<String>> imap = conn.getHeaderFields();

        //LogUtil.d("Cookie", ">>>>> containsKey(Set-Cookie) ? " + imap.containsKey("Set-Cookie"));

        if(imap.containsKey("Set-Cookie")) {
            Collection collection = (Collection) imap.get("Set-Cookie");

            // 서버에서 내려준 JSESSIONID로 쿠키값을 세팅해서 보내도, 서버에서 새로운 값을 세팅하는 경우가 있다.
            // 새로운 값이 내려오면 기존값을 버리고, 새로운 값으로 변경한다.
            // 서버에서 세션이 날아가는 문제 수정 필수!!
            m_cookies = "";

            for(Iterator i = collection.iterator(); i.hasNext();) {
                String nextCookie = (String) i.next();
                //LogUtil.d("Cookie", "Response Cookie : " + nextCookie);
                m_cookies += nextCookie;
            }
        }
    }
}
