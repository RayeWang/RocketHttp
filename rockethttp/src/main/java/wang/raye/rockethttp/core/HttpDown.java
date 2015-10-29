package wang.raye.rockethttp.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import com.ray.viewsdemo.db.DBHelp;

import android.content.Context;
import android.os.Handler;

/**
 * 多线程下载文件的工具类
 * 
 * @author Ray
 * @date 2015年7月31日14:31:46
 * @version 1.0
 */
public class HttpDown {

	/** 文件保存的位置 */
	private String path;
	/** 默认的线程数 */
	private int threadSize = 5;
	/** 下载完成后的文件 */
	private File saveFile;
	/** 下载的临时文件*/
	private File tempFile;
	private long fileLenth;
	private long block;
	private RandomAccessFile accessFile;
	private DBHelp dbHelp;
	/** 下载地址 */
	private String url;
	/** 是否支持断点下载 */
	private boolean isRange = true;
	/** 进度监听*/
	private OnDownProgress onDown;
	private long nowDown;
	/** 获取文件大小相关的线程*/
	private Thread beginDwon;
	
	private long speed = 0l;
	private boolean isFinish = false;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(onDown != null){
				switch (msg.what) {
					case 1:
						onDown.onAllLength(fileLenth);
						break;
					case 2:
						onDown.onChange(nowDown);
						break;
					case 3:
						isFinish = true;
						tempFile.renameTo(saveFile);
						onDown.onFinish();
						break;
					case 4:
						long t = nowDown;
						long temp = t - speed;
						speed = t;
						onDown.onSpeed(temp);
						if(!isFinish){
							handler.sendEmptyMessageDelayed(4, 1000);
						}
						break;
					default:
						break;
				}
			}
		};
	};

	public HttpDown(Context context, String url, String path, OnDownProgress downPre) {
		dbHelp = new DBHelp(context);
		this.onDown = downPre;
		this.path = path;
		this.url = url;
	}
	
	public void down(){
		down(url.substring(url.lastIndexOf("/")+1));
	}

	/**
	 * 下载方法,最终下载的方法
	 * @param fileName 文件保存名
	 */
	public void down(final String fileName){
		this.threadSize = threadSize;
		if(beginDwon == null){
			beginDwon = new Thread(){
				public void run() {
					try {
						begin(path, fileName, threadSize);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			beginDwon.start();
		}
	}
	
	private void begin(String path,String fileName,int threadSize)throws Exception{
		
		saveFile = new File(path,fileName);
		tempFile = new File(path, fileName+".tmp");
		if(tempFile.exists() && !tempFile.canWrite()){
			throw new Exception("文件不能写，请判断是否有权限或已在下载");
		}
		//判断文件夹是否存在
		File pathFile = new File(path);
		if(!pathFile.exists()){
			pathFile.mkdirs();
		}
		if(!pathFile.canWrite()){
			throw new Exception("目录不可写，请判断是否有权限");
		}
		if(saveFile.exists()){
			if(!saveFile.canWrite()){
				throw new Exception("文件不可写，无法下载");
			}
			if(saveFile.exists()){
				//删除之前的文件
				saveFile.delete();
			}
		}
		URL downUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) downUrl.openConnection();
//		conn.connect();
		//获取文件长度
		String contentLength = conn.getHeaderField("Content-Length");
		fileLenth = Long.parseLong(contentLength);
//		fileLenth = conn.getContentLength();
		handler.sendEmptyMessage(1);
		if(fileLenth <= 0){
			//服务器不能换回大小，使用一个线程下载
			threadSize = 1;
		}
		if(conn.getHeaderField("Content-Range") == null){  
			
            //不支持断点下载
			isRange = false;
			threadSize = 1;  
        }
		if(fileLenth > 0 && pathFile.getFreeSpace() < fileLenth){
			throw new Exception("空间不足，无法保存");
		}
		//计算每个线程需要下载多少东西
		if(threadSize > 1){
			block = fileLenth / threadSize;
			if(fileLenth % threadSize > 0){
				block ++;
			}
		}else if(fileLenth <= 0){
			block = 1;
		}else{
			block = fileLenth;
		}
		
		if(tempFile.exists()){
			//文件已存在，判断是否有记录，有记录就进行断点下载，没有就重新开始
			data = dbHelp.findLog(url);
			if(data == null){
				//记录不存在了，需要重新下载
				tempFile.delete();
				tempFile.createNewFile();
			}else if(data.keySet().size() != threadSize){
				//线程数不对等，重新下载
				tempFile.delete();
				tempFile.createNewFile();
				data = null;
			}
		}else{
			tempFile.createNewFile();
		}
		//判断下载记录是否为空，为空就初始化
		if(data == null){
			data = new ConcurrentHashMap<Integer, Long>();
			for(int i = 0;i < threadSize;i++){
				data.put(i, 0l);
			}
		}
		
		//开始下载
		threads = new DownThread[threadSize];
		dbHelp.iniDown(url, threadSize);
		accessFile = new RandomAccessFile(tempFile, "rw");
		for(int i = 0;i < threadSize;i++){
			nowDown += data.get(i);
			
			threads[i] = new DownThread(i, data.get(i),downUrl);
			threads[i].start();
		}
		speed = nowDown;
		handler.sendEmptyMessage(2);
		handler.sendEmptyMessageDelayed(4, 1000);
	}

	/**
	 * 写入数据到文件
	 * 
	 * @param buff
	 * @param startPos
	 *            开始位置
	 * @param length
	 *            长度
	 * @return
	 */
	public synchronized int write(byte[] buff, long startPos,int length, int threadId) {
		
		int i = -1;
		try {
			nowDown += length;
			long where = data.get(threadId);
			accessFile.seek(startPos);
			accessFile.write(buff, 0, length);
			i = length;
			data.put(threadId, where+length);
			if (isRange) {
				// 是断点下载才打开记录
				dbHelp.update(url, threadId, where+length);
			}
			handler.sendEmptyMessage(2);
			if(nowDown >= fileLenth){
				dbHelp.deleteByUrl(url);
				handler.sendEmptyMessage(3);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	

	private class DownThread extends Thread {
		/** 是否继续下载 */
		private boolean isContinue = true;

		private int threadId;
		/** 下载进度 */
		private long where;

		private URL downUrl;

		public DownThread(int threadId, long where, URL downUrl) {
			super();
			this.threadId = threadId;
			this.where = where;
			this.downUrl = downUrl;
		}

		public void run() {
			if (where < block) {// 未下载完成
				try {
					HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
					http.setConnectTimeout(5 * 1000);
					http.setRequestMethod("GET");
					http.setRequestProperty("Accept",
							"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
					http.setRequestProperty("Accept-Language", "zh-CN");
					http.setRequestProperty("Referer", downUrl.toString());
					http.setRequestProperty("Charset", "UTF-8");
					long startPos = block * threadId + where;// 开始位置
					long endPos = block * (threadId + 1)-1;// 结束位置
					
					http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// 设置获取实体数据的范围
					http.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
					http.setRequestProperty("Connection", "Keep-Alive");
					int code = http.getResponseCode();
					if(code == 200 || code == 206){
						InputStream inStream = http.getInputStream();
						byte[] buffer = new byte[1024];
						int offset = 0;
	
						while (isContinue && (offset = inStream.read(buffer, 0, 1024)) != -1) {
							write(buffer, startPos, offset, threadId);
 							startPos += offset;
						}
	
						inStream.close();
					}
				} catch (Exception e) {
					//产生了异常，隔一秒再试
					e.printStackTrace();
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					run();
				}
			}
		}

		public void setContinue(boolean isContinue) {
			this.isContinue = isContinue;
		}

	}
	
	public void stop(){
		handler.removeMessages(4);
		speed = 0;
		if(threads != null){
			for (int i = 0; i < threads.length; i++) {
				threads[i].setContinue(false);
			}
		}
	}
	
	public interface OnDownProgress{
		public void onChange(long newLength);
		
		public void onAllLength(long allLength);
		
		public void onFinish();
		public void onSpeed(long speed);
	}
}
