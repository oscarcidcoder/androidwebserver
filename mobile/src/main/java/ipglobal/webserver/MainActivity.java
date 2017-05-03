package ipglobal.webserver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import ipglobal.Services.WebServerService;
import ipglobal.Utils.StreamReaderClear;

public class MainActivity extends AppCompatActivity {

    HttpServiceThread httpServiceThread;

    TextView infoIp;

    private static final String SYSDATA_PATTERN = "/sysdata";
    private static final String DATA_PATTERN = "/data";
    private static final String CACHE_PATTERN = "/cache";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoIp = (TextView) findViewById(R.id.infoip);
        infoIp.setText(getIpAddress() + ":"
                + HttpServiceThread.HttpServerPORT + "\n");

        //No dormir nunca la CPU
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WebServiceWakelockTag");
        wakeLock.acquire();


        this.startService(new Intent(this, WebServerService.class));
        //httpServiceThread = new HttpServiceThread();
        //httpServiceThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //httpServiceThread.stopServer();
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    private class HttpServiceThread extends Thread {

        ServerSocket serverSocket;
        Socket socket;
        HttpService httpService;
        BasicHttpContext basicHttpContext;
        static final int HttpServerPORT = 8080;
        boolean RUNNING = false;

        HttpServiceThread() {
            RUNNING = true;
            startHttpService();
        }

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(HttpServerPORT);
                serverSocket.setReuseAddress(true);

                while (RUNNING) {
                    socket = serverSocket.accept();
                    DefaultHttpServerConnection httpServerConnection = new DefaultHttpServerConnection();
                    httpServerConnection.bind(socket, new BasicHttpParams());
                    httpService.handleRequest(httpServerConnection,
                            basicHttpContext);
                    httpServerConnection.shutdown();
                }
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (HttpException e) {
                e.printStackTrace();
            }
        }

        private synchronized void startHttpService() {
            BasicHttpProcessor basicHttpProcessor = new BasicHttpProcessor();
            basicHttpContext = new BasicHttpContext();

            basicHttpProcessor.addInterceptor(new ResponseDate());
            basicHttpProcessor.addInterceptor(new ResponseServer());
            basicHttpProcessor.addInterceptor(new ResponseContent());
            basicHttpProcessor.addInterceptor(new ResponseConnControl());

            httpService = new HttpService(basicHttpProcessor,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory());

            HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
            registry.register("/", new HomeCommandHandler());
            registry.register(SYSDATA_PATTERN, new RoiCommandHandler());
            registry.register(DATA_PATTERN, new SergioCommandHandler());
            registry.register(CACHE_PATTERN, new CacheCommandHandler());
            httpService.setHandlerResolver(registry);
        }

        public synchronized void stopServer() {
            RUNNING = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        class HomeCommandHandler implements HttpRequestHandler {

            @Override
            public void handle(HttpRequest request, HttpResponse response,
                               HttpContext httpContext) throws HttpException, IOException {

                HttpEntity httpEntity = new EntityTemplate(
                        new ContentProducer() {
                            @Override
                            public void writeTo(final OutputStream outputStream) throws IOException {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                        outputStream, "UTF-8");
                                String response = "<html><head></head><body><h1>Hello HttpService, from Android-er<h1></body></html>";

                                outputStreamWriter.write(response);
                                outputStreamWriter.flush();
                            }
                        });
                response.setHeader("Content-Type", "text/html");
                response.setEntity(httpEntity);
            }

        }

        class RoiCommandHandler implements HttpRequestHandler {

            @Override
            public void handle(HttpRequest request, HttpResponse response,
                               HttpContext httpContext) throws HttpException, IOException {

                clearData();
                HttpEntity httpEntity = new EntityTemplate(
                        new ContentProducer() {
                            @Override
                            public void writeTo(final OutputStream outputStream) throws IOException {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                        outputStream, "UTF-8");
                                String response = "<html><head></head><body><h1>Hello World, from Android-er<h1></br>" +
                                        "<h1>Clear Data like SystemApp</h1></body></html>";

                                outputStreamWriter.write(response);
                                outputStreamWriter.flush();
                            }
                        });
                response.setHeader("Content-Type", "text/html");
                response.setEntity(httpEntity);
            }

        }

        class SergioCommandHandler implements HttpRequestHandler {

            @Override
            public void handle(HttpRequest request, HttpResponse response,
                               HttpContext httpContext) throws HttpException, IOException {
                clearData2();
                HttpEntity httpEntity = new EntityTemplate(
                        new ContentProducer() {
                            @Override
                            public void writeTo(final OutputStream outputStream) throws IOException {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                        outputStream, "UTF-8");
                                String response = "<html><head></head><body><h1>Hello World, from Android-er<h1></br>" +
                                        "<h1>Clear SuiteApp Data - Tested - Rooted</h1></body></html>";

                                outputStreamWriter.write(response);
                                outputStreamWriter.flush();
                            }
                        });
                response.setHeader("Content-Type", "text/html");
                response.setEntity(httpEntity);
            }

        }

        class CacheCommandHandler implements HttpRequestHandler {

            @Override
            public void handle(HttpRequest request, HttpResponse response,
                               HttpContext httpContext) throws HttpException, IOException {

                Intent receiverIntent = new Intent("ipglobal.suiteapp.offline.DELETE_DATA");
                receiverIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                receiverIntent.setFlags(Intent.FLAG_RECEIVER_NO_ABORT);
                sendBroadcast(receiverIntent);

                //deleteAllCache();
                HttpEntity httpEntity = new EntityTemplate(
                        new ContentProducer() {
                            @Override
                            public void writeTo(final OutputStream outputStream) throws IOException {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                        outputStream, "UTF-8");
                                String response = "<html><head></head><body><h1>Hello World, from Android-er<h1></br>" +
                                        "<h1>Test clear 2GB All Cache</h1></body></html>";

                                outputStreamWriter.write(response);
                                outputStreamWriter.flush();
                            }
                        });
                response.setHeader("Content-Type", "text/html");
                response.setEntity(httpEntity);
            }

        }

    }

    public void clearData(){
        try {
            // clearing app data
            Runtime rt = Runtime.getRuntime();
            rt.exec("pm clear ipglobal.suiteapp");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAllCache(){
        PackageManager pm = getPackageManager();
        Method[] methods = pm.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals("freeStorage")) {
                try {
                    long desiredFreeStorage = 2 * 1024 * 1024 * 1024; // Request for 2GB of free space
                    m.invoke(pm, desiredFreeStorage , null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void clearData2(){
        try {
            // clearing app data
            final String CHARSET_NAME = "UTF-8";
            String cmd = "pm clear ipglobal.suiteapp";
            OutputStream out;
            ProcessBuilder pb = new ProcessBuilder().redirectErrorStream(true).command("su");
            Process p = pb.start();

            StreamReaderClear stdoutReader = new StreamReaderClear(p.getInputStream(), CHARSET_NAME);
            stdoutReader.start();

            out = p.getOutputStream();
            out.write((cmd + "\n").getBytes(CHARSET_NAME));
            out.write(("exit" + "\n").getBytes(CHARSET_NAME));
            out.flush();

            p.waitFor();
            String result = stdoutReader.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}