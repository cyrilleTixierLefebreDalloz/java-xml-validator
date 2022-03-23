package eu.els.sie.xml.validation.cp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
	private static final ClassLoader CLASS_LOADER = Handler.class.getClassLoader();

	@Override
	protected URLConnection openConnection(URL url) {
		return new URLConnection(url) {
			@Override
			public void connect() throws IOException {
				throw new IOException("Should not be here...");
			}

			@Override
			public InputStream getInputStream() {
				return CLASS_LOADER.getResourceAsStream(url.getPath().substring(1));
			}
		};
	}
}
