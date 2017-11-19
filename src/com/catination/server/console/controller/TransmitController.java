/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software;you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program;if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.catination.server.console.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * A controller class to process the user related requests.  
 *
 *
 */
public class TransmitController extends MultiActionController {

	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String SUCCESS = "200";

	// 上传文件域
	private File image;
	// 上传文件类型
	private String imageContentType="application/octet-stream";
	// 封装上传文件名
	private String imageFileName="test.image";
	// 接受依赖注入的属性
	private String savePath;


	public TransmitController() {
	}
	
	@Override
    public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
       //1、收集参数、验证参数
       //2、绑定参数到命令对象
       //3、将命令对象传入业务对象进行业务处理
       //4、选择下一个页面
       //ModelAndView mv = new ModelAndView();
       //添加模型数据 可以是任意的POJO对象
       //mv.addObject("message", "Hello World!");
       //设置逻辑视图名，视图解析器会根据该名字解析到具体的视图页面
       //mv.setViewName("hello");
       //return mv;
		
		int len = -1;
		
		log.debug("handleRequest(), \r\n"+req.getParameterMap().toString());
		//transmit(req, resp);
		
		ServletInputStream in = req.getInputStream();
		log.debug("in="+in);
		byte[] buffer = new byte[1024];
		char[] header = new char[1024];
		String path = getServletContext().getRealPath("/transmit");
		log.debug("path="+path);
		File file = new File(path+"/img_"+".jpg");
		log.debug("file1="+file.getAbsolutePath());
		file.createNewFile();
		
		log.debug("file2="+file.getAbsolutePath());
		
		//len = in.read(buffer, 0, 1024);
		//log.debug("len1="+len);
		//String headers = new String(buffer,"UTF-8");
		//log.debug("headers="+headers);
		
		
		/*BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) req.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		System.out.println(sb);*/
		
		
		
		FileOutputStream out = new FileOutputStream(file);
		len = in.read(buffer, 0, 1024);
		log.debug("len2="+len);
		while( len!=-1){
			out.write(buffer,0,len);
			len = in.read(buffer, 0, 1024);
			log.debug("len="+len);
		}
		out.close();
		in.close();

		resp.getWriter().print("HTTP/1.1 200");
		
		return super.handleRequest(req, resp);
    }
	
    public ModelAndView list(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		log.debug("list(), \r\n"+request.getParameterMap().toString());
		try {
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}

        ModelAndView mav = new ModelAndView();
        mav.setViewName("transmit/page");
        return mav;
    }

	public String send(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		return transmit(req, resp);
	}

	public String transmit(HttpServletRequest request,
			HttpServletResponse resp) {
		//HttpServletRequest request=ServletActionContext.getRequest();
		log.debug("transmit(), \r\n"+request.getParameterMap().toString());

		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			System.out.println("获取Android端传过来的普通信息：");
			System.out.println("用户名："+request.getParameter("username"));
			System.out.println("密码："+request.getParameter("pwd"));
			System.out.println("年龄："+request.getParameter("age"));
			System.out.println("文件名："+request.getParameter("fileName"));
			System.out.println("获取Android端传过来的文件信息：");
			System.out.println("文件存放目录: "+getSavePath());
			System.out.println("文件名称: "+imageFileName);
			System.out.println("文件大小: "+image.length());
			System.out.println("文件类型: "+imageContentType);

			File file = new File(getSavePath() + "/" + getImageFileName());
			file.createNewFile();
			fos = new FileOutputStream(file);
			fis = new FileInputStream(getImage());
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			System.out.println("文件上传成功");
		} catch (Exception e) {
			System.out.println("文件上传失败");
			e.printStackTrace();
		} finally {
			close(fos, fis);
		}
		return SUCCESS;
	}

	/**
	 * 文件存放目录
	 * 
	 * @return
	 */
	public String getSavePath() throws Exception{
		ServletContext sctx = getServletContext();
		String path = sctx.getRealPath("/transmit");
		log.debug("realPath="+path);
		return path;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getImageContentType() {
		return imageContentType;
	}

	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	private void close(FileOutputStream fos, FileInputStream fis) {
		if (fis != null) {
			try {
				fis.close();
				fis=null;
			} catch (IOException e) {
				System.out.println("FileInputStream关闭失败");
				e.printStackTrace();
			}
		}
		if (fos != null) {
			try {
				fos.close();
				fis=null;
			} catch (IOException e) {
				System.out.println("FileOutputStream关闭失败");
				e.printStackTrace();
			}
		}
	}

}

