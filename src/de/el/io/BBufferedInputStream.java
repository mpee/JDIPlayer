package de.el.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author PEH
 */
public class BBufferedInputStream extends BufferedInputStream {

	public BBufferedInputStream(InputStream in){
		super(in);
	}

	@Override
	public synchronized int read() throws IOException {
//		System.out.println("called");
		return super.read();
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		System.out.println("called");
		return super.read(b, off, len);
	}





 }
