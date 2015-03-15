package de.steinerix.ping_monitor.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides static method to initialize global Logger
 * 
 * @author usr
 *
 */

public class Log {
	static final private String OUTPUT_FILE_NAME = "log.txt";
	static private Logger log = Logger.getLogger("");

	/**
	 * Initialize log
	 */
	public static void init() {
		initLog();
	}

	/**
	 * Initialize log with specific log level
	 */
	public static void init(Level level) {
		log.setLevel(level);
		initLog();
	}

	private static void initLog() {
		try {
			log.addHandler(new FileHandler(OUTPUT_FILE_NAME));
		} catch (IOException e) {
			String msg = "Couldn't access log file: " + OUTPUT_FILE_NAME;
			System.err.println(msg);
			e.printStackTrace();
			log.warning(msg);
		}

		log.info("Application start.");
	}
}
