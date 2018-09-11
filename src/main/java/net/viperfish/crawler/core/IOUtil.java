package net.viperfish.crawler.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A container of all the utility functions written for input/output.
 */
public final class IOUtil {

	// ensures that it is a singleton.
	private IOUtil() {

	}

	/**
	 * read all the bytes from an {@link InputStream}.
	 * @param input the {@link InputStream} to read from.
	 * @return the bytes read from the stream.
	 * @throws IOException if an IO error occurred while reading.
	 */
	public static byte[] read(InputStream input) throws IOException {
		try (BufferedInputStream in = new BufferedInputStream(input)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] toRead = new byte[2048];
			int bytesRead = in.read(toRead);
			while (bytesRead != -1) {
				byte[] actuallyRead = new byte[bytesRead];
				System.arraycopy(toRead, 0, actuallyRead, 0, bytesRead);
				buffer.write(actuallyRead);
				bytesRead = in.read(toRead);
			}
			return buffer.toByteArray();
		}
	}
}
