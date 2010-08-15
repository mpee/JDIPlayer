package de.el.jdi;

import de.el.net.FileTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author PEH
 */
public class DIChannelFactory {

	private static final Logger LOG = LoggerFactory.getLogger(DIChannelFactory.class);

	public static DIChannel getChannel(String plsURL) {
		try {
			File f = FileTools.getFileFromURL(plsURL);
//			LOG.debug("{}", f);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			String channelName = "";
			String channelURL = "";
			while ((line = br.readLine()) != null) {
				if(!channelName.isEmpty() && !channelURL.isEmpty())
					break;
				if (line.startsWith("File")) {
					channelURL = line.substring(line.indexOf("=") + 1);
				} else if (line.startsWith("Title")) {
					channelName = line.substring(line.indexOf("=") + 1);
				}
			}
			br.close();
			if (!f.delete()) {
				LOG.debug("could not delete temp file");
			}
			return new DIChannel(channelName, channelURL);
		} catch (MalformedURLException ex) {
			LOG.error("", ex);
		} catch (IOException ex) {
			LOG.error("", ex);
		}
		return null;
	}
}
