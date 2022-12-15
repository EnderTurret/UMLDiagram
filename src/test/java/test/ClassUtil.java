package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

/**
 * Various utilities for tests.
 * @author EnderTurret
 */
public final class ClassUtil {

	static byte[] readClassBytecode(Class<?> clazz) {
		try (InputStream is = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			final byte[] buffer = new byte[2048];
			int written = 0;

			while ((written = bis.read(buffer)) != -1)
				baos.write(buffer, 0, written);

			return baos.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	static String getExpectedResult(String filename) {
		final StringBuilder sb = new StringBuilder();
		try (InputStream is = BulkStandardLibraryClassTests.class.getResourceAsStream("/expected_results/" + filename + ".txt");
				InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line).append("\n");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return sb.toString();
	}
}