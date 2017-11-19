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
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.stream.StreamIoHandler;

import com.catination.server.xmpp.MsgFileForm;


public class XmppBlockingStreamIoHandler extends StreamIoHandler {

	private static final Log log = LogFactory.getLog(XmppBlockingStreamIoHandler.class);

	public static final int BUFFER_SIZE = 1024*2;

	private static final String XMPP_STREAM_BUFFER_DIR = "/tmp";
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public XmppBlockingStreamIoHandler() {
		super();
	}
	
	private Map<String, MsgFileForm> map;
	private BlockingQueue<MsgFileForm> que;
	public XmppBlockingStreamIoHandler(Map<String, MsgFileForm> map, BlockingQueue<MsgFileForm> que) {
		super();
		
		this.map = map;
		this.que = que;
	}

	/*public static MsgFileForm readSerialization() {
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
	
	public MsgFileForm readQueue() {
		MsgFileForm mff = null;
		try {
			mff = que.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mff;
	}
	*/
	
	public MsgFileForm readMap(String key) {
		
		MsgFileForm mff = null;
		mff = map.get(key);
		return mff;
	}
	
	public MsgFileForm removeMap(String key) {
		
		MsgFileForm mff = null;
		mff = map.remove(key);
		return mff;
	}
	
	public void writeQueue(final MsgFileForm mff) {
		pool.execute(new Runnable() {
		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					que.put(mff);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void processStreamIo(IoSession session, InputStream in, OutputStream out) {
		// TODO Auto-generated method stub
		log.debug("XmppStreamIoHandler.processStreamIo()...");

		pool.execute(new ServerWorker(session, in, out));
	}
	
	class ServerWorker implements Runnable {
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
				boolean bSend = false;
				int dataLen = 0;
				MsgFileForm mff = null;
				FileInputStream fis = null;
				BufferedInputStream fbis = null;
				FileOutputStream fos = null;
				BufferedOutputStream fbos = null;

				// Start connecting.
				try {
					log.debug("ServerWorker.run(), start read");
					if ((dataLen = is.read(byteBuf, 0, 3)) != -1) {
						String req = new String(byteBuf, 0, 3);
						log.debug("ServerWorker.run(), req="+req);

						if ("SND".equals(req)) {
							byteBuf[0] = "SYN".getBytes()[0];
							byteBuf[1] = "SYN".getBytes()[1];
							byteBuf[2] = "SYN".getBytes()[2];
							os.write(byteBuf, 0, 3);
							os.flush();
							
							bSend = true;
						} else if ("RCV".equals(req)) {
							byteBuf[0] = "SYN".getBytes()[0];
							byteBuf[1] = "SYN".getBytes()[1];
							byteBuf[2] = "SYN".getBytes()[2];
							os.write(byteBuf, 0, 3);
							os.flush();
							
							bSend = false;
						} else {
							byteBuf[0] = "FIN".getBytes()[0];
							byteBuf[1] = "FIN".getBytes()[1];
							byteBuf[2] = "FIN".getBytes()[2];
							os.write(byteBuf, 0, 3);
							os.flush();
							log.debug("ServerWorker.run(), start read failed! finished!");
							continue;
						}

						String ack = new String(byteBuf, 0, 3);
						log.debug("ServerWorker.run(), write result:"+ack);
					}

					log.debug("ServerWorker.run(), negociate [ok]");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// Checking Transaction Id.
				// Transaction Id is a string of 12 long chars.
				String tid = "";
				try {
					log.debug("ServerWorker.run(), start read");
					if ((dataLen = is.read(byteBuf, 0, 12)) != -1) {
						tid = new String(byteBuf, 0, 12);
						log.debug("ServerWorker.run(), tid="+tid);

						if ((mff = readMap(tid)) != null) {
							byteBuf[0] = "ACK".getBytes()[0];
							byteBuf[1] = "ACK".getBytes()[1];
							byteBuf[2] = "ACK".getBytes()[2];
							os.write(byteBuf, 0, 3);
							os.flush();
							
							// Prepare, make output file firstly.
							if (bSend) {
								String sep = System.getProperty("file.separator", "\\");
								//String filename = "."+sep+mff.getHash()+"_"+mff.getCreatedDate().getTime();
								String filename = XMPP_STREAM_BUFFER_DIR+sep+"lightmsg"+sep+"file_"+mff.getHash()+"_"+mff.getCreatedDate().getTime();
								File receiveFile = new File(filename);
								
								//Make sure folder exists.
								String foldername = filename.substring(0, filename.lastIndexOf(sep));
								File folder = new File(foldername);
								if (folder.exists() && folder.isDirectory()) {
									//Just use it.
									log.debug("ServerWorker.run(), No need make folder, existed folder="+folder);
								} else {
									//Create the folder firstly.
									if (!folder.mkdirs()) {
										log.debug("ServerWorker.run(), Make folder failed, folder="+folder);
									} else {
										log.debug("ServerWorker.run(), Make folder successful, folder="+folder);
									}
								}
								
								log.debug("ServerWorker.run(), filename="+filename+", sep="+sep);
								log.debug("ServerWorker.run(), folder="+folder+", sep="+sep);
								log.debug("ServerWorker.run(), .="+new File(".").getAbsolutePath()+", sep="+sep);
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
							} else {
								String sep = System.getProperty("file.separator", "\\");
								//String filename = "."+sep+mff.getHash()+"_"+mff.getCreatedDate().getTime();
								String filename = XMPP_STREAM_BUFFER_DIR+sep+"lightmsg"+sep+"file_"+mff.getHash()+"_"+mff.getCreatedDate().getTime();
								File deliverFile = new File(filename);
								
								//Make sure folder exists.
								String foldername = filename.substring(0, filename.lastIndexOf(sep));
								File folder = new File(foldername);
								if (folder.exists() && folder.isDirectory()) {
									//Just use it.
								} else {
									//Create the folder firstly.
									folder.mkdirs();
								}
								
								log.debug("ServerWorker.run(), filename="+filename+", sep="+sep);
								log.debug("ServerWorker.run(), folder="+folder+", sep="+sep);
								log.debug("ServerWorker.run(), .="+new File(".").getAbsolutePath()+", sep="+sep);
								log.debug("ServerWorker.run(), user.dir="+System.getProperty("user.dir", "Unknown"));
								if (!deliverFile.exists()) {
									byteBuf[0] = "FIN".getBytes()[0];
									byteBuf[1] = "FIN".getBytes()[1];
									byteBuf[2] = "FIN".getBytes()[2];
									os.write(byteBuf, 0, 3);
									os.flush();
									
									log.debug("ServerWorker.run(), file not found! finished!");
									continue;
								}
								
								try {
									log.debug("ServerWorker.run(), .="+new File(".").getCanonicalPath());
								} catch (IOException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								try {
									fis = new FileInputStream(deliverFile); 
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								fbis = new BufferedInputStream(fis);
							}
						} else {
							byteBuf[0] = "FIN".getBytes()[0];
							byteBuf[1] = "FIN".getBytes()[1];
							byteBuf[2] = "FIN".getBytes()[2];
							os.write(byteBuf, 0, 3);
							os.flush();
							
							log.debug("ServerWorker.run(), tid not found! finished!");
							continue;
						}
						
						String ack = new String(byteBuf, 0, 3);
						log.debug("ServerWorker.run(), write result:"+ack);
					}

					log.debug("ServerWorker.run(), check tid [ok]");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (bSend)
				try {
					int reqSize = Integer.valueOf(mff.getSize());//125 * 1024;
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

					byteBuf[0] = "SUC".getBytes()[0];
					byteBuf[1] = "SUC".getBytes()[1];
					byteBuf[2] = "SUC".getBytes()[2];
					os.write(byteBuf, 0, 3);
					log.debug("ServerWorker.run(), write ack");
					os.flush();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fbos != null)
							fbos.close();
						if (fos != null)
							fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					log.info("ServerWorker.run(), finished!");
					
					writeQueue(mff);
				}
				else {
					try {
						while((dataLen = fbis.read(byteBuf)) != -1 ){
							log.debug("ServerWorker.run(), read[FILE] > BUF["+dataLen+"] > write[IO]");
							os.write(byteBuf, 0, dataLen);
						}
						os.flush();
						
						if ((dataLen = is.read(byteBuf, 0, 3)) != -1) {
							String ret = new String(byteBuf, 0, 3);
							log.debug("ClientSendWorker.run(), write result:"+ret);
							if ("SUC".equals(ret)) {
								log.debug("ServerWorker.run(), recv finish OK!");
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							if (fbis != null)
								fbis.close();
							if (fis != null)
								fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						log.info("ServerWorker.run(), finished!");
						
						//Todo: Remove mff from MAP???
						removeMap(tid);
					}
				}
				
				//break;
			}
		}
	}
}
