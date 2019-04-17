package ipglobal.webserver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import ipglobal.Utils.StreamReaderClear;

/**
 * Created by oscarcid on 24/04/2017.
 */

public class WebServerClass {

    public static boolean RUNNING = false;
    public static int serverPort = 8088;

    private static final String SYSDATA_PATTERN = "/sysdata";
    private static final String DATA_PATTERN = "/data";
    private static final String CACHE_PATTERN = "/cache";
    private static final String QR_PATTERN = "/qr";

    private Context context = null;
    private ServerSocket serverSocket;
    private SSLServerSocket serverSocketSSL;
    private BasicHttpProcessor httpproc = null;
    private BasicHttpContext httpContext = null;
    private HttpService httpService = null;
    private HttpRequestHandlerRegistry registry = null;

    public WebServerClass(Context context){
        this.context = context;
        httpproc = new BasicHttpProcessor();
        httpContext = new BasicHttpContext();

        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        httpService = new HttpService(httpproc,
                new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

        registry = new HttpRequestHandlerRegistry();

        registry.register("/", new HomeCommandHandler());
        registry.register(SYSDATA_PATTERN, new RoiCommandHandler());
        registry.register(DATA_PATTERN, new SergioCommandHandler());
        registry.register(CACHE_PATTERN, new CacheCommandHandler());
        registry.register(QR_PATTERN, new QrCommandHandler());

        httpService.setHandlerResolver(registry);
    }

    public void runServer() {
        try {
            /*SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocketSSL = (SSLServerSocket) factory.createServerSocket(serverPort);

            serverSocketSSL.setReuseAddress(true);*/
            serverSocket = new ServerSocket(serverPort);

            serverSocket.setReuseAddress(true);

            while (RUNNING) {
                try {
                    final Socket socket = serverSocket.accept();
                   //final Socket socket = serverSocketSSL.accept();

                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

                    serverConnection.bind(socket, new BasicHttpParams());

                    httpService.handleRequest(serverConnection, httpContext);

                    serverConnection.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (HttpException e) {
                    e.printStackTrace();
                }
            }

            serverSocket.close();
            //serverSocketSSL.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception er){
            er.printStackTrace();
        }

        RUNNING = false;
    }

    public synchronized void startServer() {
        RUNNING = true;
        runServer();
    }

    public synchronized void stopServer() {
        RUNNING = false;
        /*if (serverSocketSSL != null) {
            try {
                serverSocketSSL.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
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
                            String response = "<html><head></head><body><h1>Hello Android-TV MAIN, from SuiteAppTV<h1></body></html>";

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
                            String response = "<html><head></head><body><h1>Hello Android-TV MAIN, from SuiteAppTV<h1></br>" +
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
                            String response = "<html><head></head><body><h1>Hello Android-TV MAIN, from SuiteAppTV<h1></br>" +
                                    "<h1>Test DroidLogic RESET</h1></body></html>";
                                    //"<h1>Clear SuiteApp Data - Tested - Rooted</h1></body></html>";

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
            context.sendBroadcast(receiverIntent);

            //deleteAllCache();
            HttpEntity httpEntity = new EntityTemplate(
                    new ContentProducer() {
                        @Override
                        public void writeTo(final OutputStream outputStream) throws IOException {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                    outputStream, "UTF-8");
                            String response = "<html><head></head><body><h1>Hello Android-TV MAIN, from SuiteAppTV<h1></br>" +
                                    "<h1>Test clear 2GB All Cache</h1></body></html>";

                            outputStreamWriter.write(response);
                            outputStreamWriter.flush();
                        }
                    });
            response.setHeader("Content-Type", "text/html");
            response.setEntity(httpEntity);
        }

    }

    class QrCommandHandler implements HttpRequestHandler {

        @Override
        public void handle(HttpRequest request, HttpResponse response,
                           HttpContext httpContext) throws HttpException, IOException {

            Intent qrIntent = new Intent("launcher.suiteapp.qrcode");
            qrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //receiverIntent.putExtra("nombre_customer", "");
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(qrIntent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if(isIntentSafe){
                context.startActivity(qrIntent);
            }

            //deleteAllCache();
            HttpEntity httpEntity = new EntityTemplate(
                    new ContentProducer() {
                        @Override
                        public void writeTo(final OutputStream outputStream) throws IOException {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                    outputStream, "UTF-8");
                            String response = "<html><head></head><body><h1>Hello Android-TV MAIN, from SuiteAppTV<h1></br>" +
                                    "<h1 align=\"center\">QR Code displayed in SuiteApp TV</h1></body></html>";

                            outputStreamWriter.write(response);
                            outputStreamWriter.flush();
                        }
                    });
            response.setHeader("Content-Type", "text/html");
            response.setEntity(httpEntity);
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
        PackageManager pm = this.context.getPackageManager();
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
            //String cmd = "pm clear com.droidlogic";
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
