package de.el.jdi;

import de.el.net.FileTools;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
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
				if (!channelName.isEmpty() && !channelURL.isEmpty()) {
					break;
				}
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

	/**
	 * Checks weather the given channelFile ist older then 24hours.
	 * @param channelFile
	 * @return
	 */
	public static boolean isChannelListOutdated(File channelFile) {
		return (!channelFile.exists() || channelFile.lastModified() - System.currentTimeMillis() > 1000 * 60 * 60 * 24);
	}

	public static List<DIChannel> updateChannelListFile(File channelFile) throws MalformedURLException, IOException {
		List<DIChannel> channelList = new ArrayList<DIChannel>();
		File temp = FileTools.getFileFromURL("http://www.di.fm/menus/diguest.js", "channels.tmp");
		BufferedReader br = new BufferedReader(new FileReader(temp));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.contains("http://listen.di.fm/") && line.contains(".pls") && line.contains("96k")) {
				String plsURL = line.substring(line.indexOf("href=\"") + 6, line.indexOf("\">"));
				channelList.add(DIChannelFactory.getChannel(plsURL));
			}
		}
		br.close();
		if (!temp.delete()) {
			System.out.println("could not delete temp file");
		}
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(channelFile));
		for (DIChannel d : channelList) {
			os.writeObject(d);
			os.flush();
		}
		os.flush();
		os.close();
		return channelList;
	}

	public static List<DIChannel> getChannelListFromFile(File channelFile) throws IOException, ClassNotFoundException{
		List<DIChannel> channelList = new ArrayList<DIChannel>();

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(channelFile));
		DIChannel dic = null;

		try {
			while ((dic = (DIChannel) ois.readObject()) != null) {
				channelList.add(dic);
			}

		} catch (EOFException e) {
			System.out.println("end of file");
		}
		ois.close();

		return channelList;
	}
}
