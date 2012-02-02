package org.logger;

import gui.FlasherGUI;

import java.awt.Color;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class ConsoleAppender extends WriterAppender {
	/**
	 * Format and then append the loggingEvent to the stored
	 * JTextArea.
	 */
	public void append(LoggingEvent loggingEvent) {
		final String message = this.layout.format(loggingEvent);
		if (!FlasherGUI.guimode && MyLogger.lastaction.equals("progress")) {
			System.out.println();
		}
		System.out.print(message);
		MyLogger.lastaction="log";
	}

}
