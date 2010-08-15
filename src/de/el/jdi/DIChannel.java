package de.el.jdi;

import java.io.Serializable;

/**
 *
 * @author PEH
 */
public class DIChannel implements Serializable, Comparable<DIChannel>{

	private static final long serialVersionUID = -615681235233776147L;

	public DIChannel(String channelName, String channelURL) {
		this.channelName = channelName;
//		this.channelUrls = new HashMap<String, String>();
		this.channelURL = channelURL;
	}
	private String channelName;
	private String channelURL;

	public String getChannelName() {
		return channelName;
	}

	public String getChannelURL() {
		return channelURL;
	}
//	private Map<String, String> channelUrls;

//	public void addURL(String name, String url){
//		channelUrls.put(name, url);
//	}
	@Override
	public String toString() {
		return channelName + ":" + channelURL;
	}

	public int compareTo(DIChannel o) {
		return this.getChannelName().compareTo(o.getChannelName());
	}
}
