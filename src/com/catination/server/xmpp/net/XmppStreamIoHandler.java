package com.catination.server.xmpp.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.stream.StreamIoHandler;

import com.catination.server.xmpp.MsgFileForm;


public class XmppStreamIoHandler extends StreamIoHandler {

	private static final Log log = LogFactory.getLog(XmppStreamIoHandler.class);

	public static final int BUFFER_SIZE = 1024*2;
	private ExecutorService pool = Executors.newCachedThreadPool();

	/*
	//设定一个线程池
	//参数说明：最少数量3，最大数量6 空闲时间 3秒 
	ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 6, 3, TimeUnit.SECONDS, 
			//缓冲队列为3
			new ArrayBlockingQueue<Runnable>(3),
			//抛弃旧的任务
			new ThreadPoolExecutor.DiscardOldestPolicy());
	 */
	{

	}

	public static MsgFileForm readSerialization() {
		MsgFileForm mff = null;
		String bufFile = "F:\\MsgFile.dat";
		try {
			FileInputStream fis = new FileInputStream(bufFile);
			ObjectInputStream ois = new ObjectInputStream(fis);

			mff = (MsgFileForm) ois.readObject();;
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mff;
	}

	@Override
	protected void processStreamIo(IoSession session, InputStream in, OutputStream out) {
		// TODO Auto-generated method stub
		log.debug("XmppStreamIoHandler.processStreamIo()...");

		//pool.execute(new ServerInWorker(session, in, fos));
		pool.execute(new ServerWorker(session, in, out));
		//threadPool.execute(new Worker(session, in, out)); 
	}

	static class ServerOutWorker implements Runnable {
		private BufferedInputStream bis;
		private BufferedOutputStream bos;

		private IoSession mSession;

		byte[] byteBuf = new byte[BUFFER_SIZE];

		public ServerOutWorker(IoSession session, InputStream in, OutputStream out) {
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(out);

			mSession = session;
		}

		public void run() {
			log.debug("ServerOutWorker.run()...");

			int dataLen = 0; 

			try {
				log.debug("ServerOutWorker.run(), before read");
				//if ((dataLen = bis.read(byteBuf, 0, 12)) != -1) {
				String req = new String(byteBuf, 0, 12);
				log.debug("ServerOutWorker.run(), req="+req);

				//if ("HELO".equals(req)) {
				byteBuf[0] = 'T';
				bos.write(byteBuf, 0, 1);

				try {
					bos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//} else {
				//	byteBuf[0] = 'F';
				//	bos.write(byteBuf, 0, 1);
				//}
				//}
				log.debug("ServerOutWorker.run(), after read");
				return;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				while((dataLen = bis.read(byteBuf)) != -1){
					log.info("ServerOutWorker.run(), read > BUF["+dataLen+"] > write");
					bos.write(byteBuf, 0, dataLen); 
				}

				try {
					bos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					bos.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mSession.close();
				log.info("ServerOutWorker.run(), finished!");
			}
		}
	}

	static class ServerInWorker extends Thread {
		private BufferedInputStream bis;
		private BufferedOutputStream fbos;

		private IoSession mSession;

		byte[] byteBuf = new byte[BUFFER_SIZE];

		public ServerInWorker(IoSession session, InputStream in, OutputStream fout) {
			bis = new BufferedInputStream(in);
			fbos = new BufferedOutputStream(fout);

			mSession = session;
		}

		public void run() {
			log.debug("ServerInWorker.run()...");

			int dataLen = 0; 

			try {
				log.debug("ServerInWorker.run(), before read");
				if ((dataLen = bis.read(byteBuf, 0, 2)) != -1) {
					String req = new String(byteBuf, 0, 2);
					log.debug("ServerWorker.run(), req="+req);
				}
				log.debug("ServerInWorker.run(), after read");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				while((dataLen = bis.read(byteBuf)) != -1){
					log.info("ServerInWorker.run(), read > BUF["+dataLen+"] > write");
					fbos.write(byteBuf, 0, dataLen); 
				}

				try {
					fbos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					fbos.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mSession.close();
				log.info("ServerInWorker.run(), finished!");
			}
		}
	}

	static class ServerWorker implements Runnable {
		private InputStream is;
		private OutputStream os;

		//private BufferedOutputStream fbos;

		private IoSession mSession;

		byte[] byteBuf = new byte[BUFFER_SIZE];

		public ServerWorker(IoSession session, InputStream in, OutputStream out) {
			is = in;
			os = out;

			mSession = session;
		}

		public void run() {
			log.debug("ServerWorker.run()...");

			for (;;) {
				int dataLen = 0;
				MsgFileForm mff = null;
				FileOutputStream fos = null;
				BufferedOutputStream fbos = null;

				if ((mff = readSerialization()) == null) {
					try {
						log.debug("ServerWorker.run(), sleep...");
						Thread.currentThread().sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				} else {
					String filename = "."+System.getProperty("file.separator", "\\")+mff.getHash()+"_"+mff.getCreatedDate().getTime();
					File receiveFile = new File(filename);
					log.debug("ServerWorker.run(), filename="+filename);
					log.debug("ServerWorker.run(), user.dir="+System.getProperty("user.dir", "Unknown"));
					try {
						log.debug("ServerWorker.run(), .="+new File(".").getCanonicalPath());
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						receiveFile.createNewFile();
						fos = new FileOutputStream(receiveFile); 
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fbos = new BufferedOutputStream(fos);
				}

				try {
					log.debug("ServerWorker.run(), start read");
					if ((dataLen = is.read(byteBuf, 0, 4)) != -1) {
						String req = new String(byteBuf, 0, 4);
						log.debug("ServerWorker.run(), req="+req);

						if ("HELO".equals(req)) {
							byteBuf[0] = "SYN".getBytes()[0];
							byteBuf[1] = "SYN".getBytes()[1];
							byteBuf[2] = "SYN".getBytes()[2];
							os.write(byteBuf, 0, 3);
						} else {
							byteBuf[0] = "FIN".getBytes()[0];
							byteBuf[1] = "FIN".getBytes()[1];
							byteBuf[2] = "FIN".getBytes()[2];
							os.write(byteBuf, 0, 3);
						}

						String ack = new String(byteBuf, 0, 3);
						log.debug("ServerWorker.run(), write result:"+ack);
						try {
							os.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					log.debug("ServerWorker.run(), negociate finished");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				int reqSize = Integer.valueOf(mff.getSize());//125 * 1024;
				try {
					int total = 0;
					while((dataLen = is.read(byteBuf)) != -1){
						total += dataLen;
						log.info("ServerWorker.run(), RECV TOTAL:"+total+", read[IO] > BUF["+dataLen+"] > write[FILE]");
						fbos.write(byteBuf, 0, dataLen);
						if (total >= reqSize) {
							log.info("ServerWorker.run(), RECV Finished:"+total);
							break;
						}
					}

					fbos.flush();

					byteBuf[0] = "ACK".getBytes()[0];
					byteBuf[1] = "ACK".getBytes()[1];
					byteBuf[2] = "ACK".getBytes()[2];
					os.write(byteBuf, 0, 3);
					log.debug("ServerWorker.run(), write ack");
					os.flush();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						fbos.close();
						fos.close();
						//bos.close();
						//bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					//mSession.close();
					log.info("ServerWorker.run(), finished!");
				}
				
				break;
			}
		}
	}
}
