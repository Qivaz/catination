package com.catination.server.console.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UploadServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1927266699439162553L;

	protected final Log log = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked") 
	@Override 
	protected void service(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		log.info("req="+req.toString());
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		ServletFileUpload sfu = new ServletFileUpload(factory);
		sfu.setFileSizeMax(1024*1024*4); //Max. 4M per file
		sfu.setSizeMax(1024*1024*4*5);
		try {
			List<FileItem> items = sfu.parseRequest(req);
			log.info("req.items.size()="+items.size());
			for (int i = 0;i < items.size();i++) {
				FileItem item = items.get(i);
				if(!item.isFormField()){
					ServletContext sctx = getServletContext();
					String path = sctx.getRealPath("/upload");
					log.info(path);

					String fileName = item.getName();
					log.info(fileName);
					if (fileName == null || fileName.isEmpty()) {
						log.error("Destination file name is null!");
						continue;
					}

					fileName = fileName.substring(fileName.lastIndexOf("/")+1);
					File file = new File(path+"\\"+"ADVERTISEMENT_PIC1");
					if(!file.exists()){
						log.debug("Writed to the file:"+file.getAbsolutePath());
						item.write(file);
						//resp.sendRedirect("/index.jsp");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			if(ServletFileUpload.isMultipartContent(request)){
				DiskFileItemFactory dff = new DiskFileItemFactory();
				//dff.setRepository(tmpDir);
				dff.setSizeThreshold(1024*1024);
				ServletFileUpload sfu = new ServletFileUpload(dff);
				sfu.setFileSizeMax(1024*1024*4);
				sfu.setSizeMax(1024*1024*4*5);
				FileItemIterator fii = sfu.getItemIterator(request);
				log.debug("doPost(), fii"+fii.toString());
				while(fii.hasNext()){
					FileItemStream fis = fii.next();
					log.debug("doPost(), fis"+fis);
					if(!fis.isFormField() && fis.getName().length()>0){
						ServletContext sctx = getServletContext();
						String path = sctx.getRealPath("/upload");
						log.info(path);

						String fileName = fis.getName().substring(fis.getName().lastIndexOf("\\"));
						if (fileName == null || fileName.isEmpty()) {
							log.error("Destination file name is null!");
							continue;
						}

						BufferedInputStream in = new BufferedInputStream(fis.openStream());
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path+fileName)));
						Streams.copy(in, out, true);
					}
				}
				response.getWriter().println("File upload successfully!!!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
}
