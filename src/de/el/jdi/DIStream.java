package de.el.jdi;

import com.Ostermiller.util.CircularByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author PEH
 */
public class DIStream {

	public DIStream(InputStream in, int bufferSize, boolean metaInfSend) {
		this.in = in;
		this.metaInfSend = metaInfSend;

		cbb = new CircularByteBuffer(bufferSize, true);
		metaInfo = new HashMap<String, String>();
		ft = new FillerThread();
		metaint = metaInfSend ? 0 : bufferSize / 10;
		ft.start();

	}
	private static final Logger LOG = LoggerFactory.getLogger(DIStream.class);
	private InputStream in;
	private CircularByteBuffer cbb;
	private FillerThread ft;
	private int metaint;
	private Map<String, String> metaInfo;
	private boolean metaInfSend;

	public InputStream getCircularByteBufferInputStream() {
		return cbb.getInputStream();
	}

	public Map<String, String> getMetaInfo() {
		return metaInfo;
	}

	public void close() {
		ft.interrupt();
	}

	private void fillHTMLHeadMeta() throws IOException {
		int _byte = 0;
		String s = "";
		boolean bytesTillMetaDataFound = false;
		while ((_byte = in.read()) != -1 && !bytesTillMetaDataFound) {
			s += (char) _byte;
			if (_byte == 0x0a && !bytesTillMetaDataFound) {
				if (s.indexOf(":") > 0) {
					metaInfo.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1, s.length()));
				}
				if (s.contains("icy-metaint")) {
					String metaInt = s.substring(s.indexOf(":") + 1, s.length() - 2);
					metaint = Integer.valueOf(metaInt);
					bytesTillMetaDataFound = true;
					break;
				}
				s = "";
			}
		}
	}

	public static int signedByteToInt(byte value) {
		return (value & 0x7F) + (value < 0 ? 128 : 0);
	}

	private class FillerThread extends Thread {

		@Override
		public void run() {
			try {
				if (metaInfSend) {
					fillHTMLHeadMeta();
				}
				byte[] input = new byte[metaint];
				int countBytes = 0;
				while (!isInterrupted()) {
//					input = new byte[in.available()];
					countBytes += in.read(input);
					cbb.getOutputStream().write(input);
//					if(countBytes == metaint){
//						LOG.debug("ASD");
//						countBytes = 0;
//					} else{

//					}
					countBytes++;
				}
				in.close();
			} catch (Exception ex) {
				LOG.error("", ex);
			}
		}
	}
}
