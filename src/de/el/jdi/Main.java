package de.el.jdi;

import de.el.jdi.gui.Gui;
import de.el.net.FileTools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.*;

/**
 *
 * @author PEH
 */
public class Main {



	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {

		File channelFile = new File("channels.dat");
		List<DIChannel> channelList = new ArrayList<DIChannel>();
		if (!channelFile.exists() || channelFile.lastModified() - System.currentTimeMillis() > 1000 * 60 * 60 * 24) {
			System.out.println("File to old need new one");
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
		}

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

		Collections.sort(channelList);
		Gui g = new Gui();
		g.addChannels(channelList);
		g.open();
//		System.out.println(channelFile.);




//		os = new FileOutputStream(new File("channels.jdi"));

	}
}
