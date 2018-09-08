package net.viperfish.framework.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * compressor with DEFLATE algorithm
 *
 * @author sdai
 */
final class DEFLATECompressor extends Compressor {

	@Override
	protected OutputStream createOutputStream(ByteArrayOutputStream out)
		throws CompressorException {
		return getFactory().createCompressorOutputStream(CompressorStreamFactory.DEFLATE, out);
	}

	@Override
	protected InputStream createInputStream(ByteArrayInputStream in)
		throws CompressorException {
		return getFactory().createCompressorInputStream(CompressorStreamFactory.DEFLATE, in);
	}

}
