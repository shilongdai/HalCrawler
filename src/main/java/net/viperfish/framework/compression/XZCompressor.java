package net.viperfish.framework.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * compressor with XZ algorithm
 * 
 * @author sdai
 *
 */
final class XZCompressor extends Compressor {

	@Override
	protected OutputStream createOutputStream(ByteArrayOutputStream out) throws IOException, CompressorException {
		return getFactory().createCompressorOutputStream(CompressorStreamFactory.XZ, out);
	}

	@Override
	protected InputStream createInputStream(ByteArrayInputStream in) throws IOException, CompressorException {
		return getFactory().createCompressorInputStream(CompressorStreamFactory.XZ, in);
	}

}
