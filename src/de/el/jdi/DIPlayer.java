package de.el.jdi;

import de.el.io.BBufferedInputStream;
import de.el.jdi.exceptions.InvalidChannelException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author PEH
 */
public class DIPlayer {

	private static final Logger LOG = LoggerFactory.getLogger(DIPlayer.class);
	private PlayerThread playerThread;
	private DIStream stream;
	private static final int bufferSize = 409590;

	public void setChannel(URL channelURL) throws IOException, JavaLayerException, UnsupportedAudioFileException {
		LOG.debug("trying to play {} now", channelURL);
		HttpURLConnection conn = (HttpURLConnection) channelURL.openConnection();
//		conn.addRequestProperty("Icy-MetaData", "1");
//		LOG.debug("{}", conn.getRequestProperties());
//		LOG.debug("{}", conn.getResponseMessage());

		stream = new DIStream(conn.getInputStream(),bufferSize ,false);
//		stream.start();
//		int _byte = 0;
//		int countTillMetaData = 0;
//		int bytesTillMetaData = 0;
//		boolean bytesTillMetaDataFound = false;
//		String s = "";
//		while ((_byte = stream.read()) != -1 && bytesTillMetaDataFound) {
//			s += (char) _byte;
//			if (_byte == 0x0a && !bytesTillMetaDataFound) {
//				if (s.contains("icy-metaint")) {
//					String metaint = s.substring(s.indexOf(":") + 1, s.length() - 2);
////					LOG.debug("found metaint: \"{}\"", metaint);
////					bytesTillMetaData = Integer.valueOf(metaint) + 13;
//					bytesTillMetaData = Integer.valueOf(metaint);
//					bytesTillMetaDataFound = true;
//					break;
//				}
////				LOG.debug(s);
//				s = "";
//			}
//			if (bytesTillMetaDataFound) {
//				if (countTillMetaData == bytesTillMetaData) {
//					LOG.debug("Found MetaInf Length: {}(raw: {})", signedByteToInt((byte) _byte) * 16, _byte);
//					return;
//				} else if (countTillMetaData > bytesTillMetaData) {
//					if (_byte == 0x0a) {
////						LOG.debug("#{}#", s);
//						s = "";
//					}
//				}
//				countTillMetaData++;
//			}
////			System.out.print((char) b);
//		}
////		try {
////			getDamnMetaInf(bytesTillMetaData);
////		} catch (Exception ex) {
////			java.util.logging.Logger.getLogger(DIPlayer.class.getName()).log(Level.SEVERE, null, ex);
////		}
		playerThread = new PlayerThread();
	}

	public void play() throws InvalidChannelException {
		if (stream == null) {
			throw new InvalidChannelException("please choose a channel");
		}
		playerThread.start();
	}

	public void stop() {
		if (playerThread != null) {
			System.out.println("asd");
			playerThread.interrupt();
			stream.close();
		}
	}

	private class PlayerThread extends Thread {

		@Override
		public void run() {
			setName("PlayerThread");
			try {
//			File file = new File(filename);
//				AudioFileFormat aff = AudioSystem.getAudioFileFormat(stream);
//				LOG.debug("{}", aff.properties());
				AudioInputStream in = AudioSystem.getAudioInputStream(stream.getCircularByteBufferInputStream());
				AudioInputStream din = null;
				AudioFormat baseFormat = in.getFormat();
				AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						baseFormat.getSampleRate(),
						16,
						baseFormat.getChannels(),
						baseFormat.getChannels() * 2,
						baseFormat.getSampleRate(),
						false);
				din = AudioSystem.getAudioInputStream(decodedFormat, in);
				// Play now.
				byte[] data = new byte[4096];
//				LOG.debug("{}->{}", dataSize, data.length);
				SourceDataLine line = getLine(decodedFormat);
				if (line != null) {
					// Start
					line.start();
					int nBytesRead = 0, nBytesWritten = 0;
//						for(int i = 0; i < dataSize; i++){
//							data[i] = (byte) din.read();
//						}
//						LOG.debug("{}", din.read());
//						line.write(data, 0, dataSize);
//						line.
					long startTime;
					startTime = System.currentTimeMillis();
					while(stream.getCircularByteBufferInputStream().available() < (bufferSize/10)*9){
						LOG.debug("currentBuffer: {} treshhold: {}", stream.getCircularByteBufferInputStream().available(), (bufferSize/10)*9);
						continue;
					}
					LOG.debug("finished buffering after: {} ms", System.currentTimeMillis()-startTime);
					while (nBytesRead != -1 && !isInterrupted()) {
						if(stream.getCircularByteBufferInputStream().available() < 5){
							return;
						}
//						System.out.println(stream.getCircularByteBufferInputStream().available());
						try{
							nBytesRead = din.read(data, 0, data.length);
						} catch(ArrayIndexOutOfBoundsException e){
							LOG.debug("EX occured after: {}", (System.currentTimeMillis()-startTime) / 1000);
						}
						if (nBytesRead != -1) {
							nBytesWritten = line.write(data, 0, nBytesRead);
						}
					}
//						byte[] metaInfBlockLength = new byte[1];
//						stream.read(metaInfBlockLength);
//						int metaBytes = signedByteToInt(metaInfBlockLength[0]) * 16;
//						if (metaBytes > 0) {
//							byte[] metaBuffer = new byte[metaBytes];
//							int readMetaInf = stream.read(metaBuffer);
//							System.out.println("Read " + readMetaInf + " bytes meta");
//							String header = new String(metaBuffer);
//							System.out.println("Header:" + header);
//						}
					// Stop
					line.drain();
					line.stop();
					line.close();
					din.close();
				}
//				rawplay(decodedFormat, din);
				in.close();
			} catch (Exception e) {
				LOG.debug("", e);
			}
		}
	}

	private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1) {
					nBytesWritten = line.write(data, 0, nBytesRead);
				}
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
//	private void getDamnMetaInf(int metaInt) throws Exception {
//
//		// Play now.
//		byte[] soundBuffer = new byte[1];
//		byte[] data = new byte[metaInt];
//		byte[] metaInfBlockLength = new byte[1];
//
//		for (int i = 0; i < 2000; i++) {
//
//			int mp3Bytes = 0;
//			while (mp3Bytes < metaInt) {
//				int _byte = stream.read(soundBuffer);
//				data[mp3Bytes] = new Integer(_byte).byteValue();
//				mp3Bytes += _byte;
//			}
//
////			System.out.println("Read " + mp3Bytes + " bytes data"); // sollten 16000 sein
//			int readLength = stream.read(metaInfBlockLength);
////			System.out.println("Read " + readLength + " bytes metalength"); // sollte 1 sein
//			int metaBytes = signedByteToInt(metaInfBlockLength[0]) * 16; // wert des header-längen-bytes mal 16
////			System.out.println("Awaiting " + readLength + " bytes of metainf");
//			// wenn meta vorhanden ist
//			if (metaBytes > 0) {
//				byte[] metaBuffer = new byte[metaBytes];
//				int readMetaInf = stream.read(metaBuffer);
//				System.out.println("Read " + readMetaInf + " bytes meta");
//				String header = new String(metaBuffer);
//				System.out.println("Header:" + header);
//			}
//			if (i == 0) {
//				metaInt = metaInt - 13;
//			}
//			LOG.debug("{}", i);
//		}
//	}
//
//	public static int signedByteToInt(byte value) {
//		return (value & 0x7F) + (value < 0 ? 128 : 0);
//	}
}
//		AudioFileFormat aff = AudioSystem.getAudioFileFormat(url);
//			player.open();

