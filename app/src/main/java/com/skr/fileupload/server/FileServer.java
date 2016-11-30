package com.skr.fileupload.server;

import android.os.Environment;

import com.skr.fileupload.utils.StreamTool;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer {
    private static final String LOG_TAG = "FileServer";
    private ExecutorService executorService;
    private int port;//监听端口
    private boolean quit = false;//退出
    private ServerSocket server;
    private Map<Long, FileLog> datas = new HashMap<>();// 存放断点数据，需要改为数据库存放

    public FileServer(int port) {
        this.port = port;
        //创建线程池，池中具有(cpu个数*50)条线程
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);
    }

    /**
     * 启动服务
     *
     * @throws Exception
     */
    public void start() throws Exception {
        server = new ServerSocket(port);
        while (!quit) {
            try {
                Socket socket = server.accept();
                //为支持多用户并发访问，采用线程池管理每一个用户的连接请求
                executorService.execute(new SocketTask(socket));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final class SocketTask implements Runnable {
        private Socket socket = null;

         SocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                System.out.println("accepted connection " + socket.getInetAddress() + ":" + socket.getPort());

                //创建回退流对象，将拆解的字节数组流传入
                //PushbackInputStream类继承了FilterInputStream类是iputStream类的修饰者。
                //提供可以将数据插入到输入流前端的能力。能够插入的最大字节数与推回缓冲区的大小相关。
                PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());

                //得到客户端发来的第一行协议数据：Content-Length=143253434;fileName=xxx.3gp;sourceId=
                //如果用户初次上传文件，sourceId的值为空。
                String head = StreamTool.readLine(inStream);
                System.out.println(head);

                //黑客假如注入很长的第一行数据，那么服务器肯定会由于内存溢出而崩溃
                //实际项目中这里要判断第一行协议内容不能超过多长，设置一个长度上线，防止内存溢出异常。
                if (head != null) {
                    //下面从协议数据中提取各项参数值
                    String[] items = head.split(";");
                    String filelength = items[0].substring(items[0].indexOf("=") + 1);
                    String filename = items[1].substring(items[1].indexOf("=") + 1);
                    filename = URLDecoder.decode(filename, "UTF-8");
                    String sourceid = items[2].substring(items[2].indexOf("=") + 1);
                    long id = System.currentTimeMillis();//生产资源id，如果需要唯一性，可以采用UUID
                    FileLog log = null;
                    if (!"".equals(sourceid)) {
                        id = Long.valueOf(sourceid);
                        log = find(id);//查找上传的文件是否存在上传记录
                    }
                    File file;
                    int position = 0;

                    if (log == null) {//如果不存在上传记录,为文件添加跟踪记录
                        String path = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(new Date());
                        File dir = new File(Environment.getExternalStorageDirectory() + "/file/" + path);
                        if (!dir.exists()) dir.mkdirs();
                        file = new File(dir, filename);
                        if (file.exists()) {//如果上传的文件发生重名，然后进行改名
                            filename = filename.substring(0, filename.indexOf(".") - 1) +
                                    dir.listFiles().length + filename.substring(filename.indexOf("."));
                            file = new File(dir, filename);
                        }
                        save(id, file);
                    } else {// 如果存在上传记录,读取已经上传的数据长度
                        file = new File(log.getPath());//从上传记录中得到文件的路径
                        if (file.exists()) {
                            File logFile = new File(file.getParentFile(), file.getName() + ".log");
                            if (logFile.exists()) {
                                Properties properties = new Properties();
                                FileInputStream fileInputStream = new FileInputStream(logFile);
                                properties.load(fileInputStream);
                                position = Integer.valueOf(properties.getProperty("length"));//读取已经上传的数据长度
                                fileInputStream.close();
                            }
                        }
                    }

                    OutputStream outStream = socket.getOutputStream();
                    String response = "sourceId=" + id + ";position=" + position + "\r\n";
                    KLog.w(LOG_TAG, "position" + position);
                    //服务器收到客户端的请求信息后，给客户端返回响应信息：sourceId=1274773833264;position=0
                    //sourceid由服务器端生成，唯一标识上传的文件，position指示客户端从文件的什么位置开始上传
                    outStream.write(response.getBytes("UTF-8"));

                    RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");
                    if (position == 0) fileOutStream.setLength(Integer.valueOf(filelength));//设置文件长度
                    fileOutStream.seek(position);//指定从文件的特定位置开始写入数据
                    byte[] buffer = new byte[1024];
                    int len;
                    int length = position;
                    while ((len = inStream.read(buffer)) != -1) {//从输入流中读取数据写入到文件中
                        fileOutStream.write(buffer, 0, len);
                        length += len;
                        Properties properties = new Properties();
                        properties.put("length", String.valueOf(length));
                        FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName() + ".log"));
                        properties.store(logFile, null);//实时记录已经接收的文件长度
                        logFile.close();
                    }

                    KLog.w(LOG_TAG, "uploaded file length: " + length);

                    if (length == fileOutStream.length()) delete(id);

                    fileOutStream.close();
                    inStream.close();
                    outStream.close();

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private FileLog find(Long sourceid) {
        return datas.get(sourceid);
    }

    //保存上传记录
    private void save(Long id, File saveFile) {
        //日后可以改成通过数据库存放
        datas.put(id, new FileLog(id, saveFile.getAbsolutePath()));
    }

    //当文件上传完毕，删除记录
    private void delete(long sourceid) {
        if (datas.containsKey(sourceid)) datas.remove(sourceid);
    }

    private class FileLog {
        private Long id;
        private String path;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        private String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        private FileLog(Long id, String path) {
            this.id = id;
            this.path = path;
        }
    }

}
