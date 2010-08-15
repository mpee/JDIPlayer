package de.el.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author PEH
 */
public class FileTools {

	public static File getFileFromURL(String url) throws MalformedURLException, IOException{
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + (int)(Math.random() * 1000000) + ".tmp";
		return getFileFromURL(url, fileName);
	}

		public static File getFileFromURL(String url, String fileName) throws MalformedURLException, IOException{
		File f = new File(fileName);

		URL u = new URL(url);
		URLConnection urlc = u.openConnection();
		InputStream is = urlc.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		OutputStream os = new FileOutputStream(f);
		int ch = 0;
		while ((ch = bis.read()) != -1) {
			os.write(ch);
		}
		os.close();
		is.close();
		return f;
	}
}
