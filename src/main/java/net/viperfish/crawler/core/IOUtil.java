package net.viperfish.crawler.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtil {

	private IOUtil() {

	}

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
