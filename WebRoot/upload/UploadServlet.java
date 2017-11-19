
package com.catination.upload;

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

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
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
}
