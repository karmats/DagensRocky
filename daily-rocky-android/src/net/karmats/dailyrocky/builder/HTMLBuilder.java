package net.karmats.dailyrocky.builder;

import java.util.Date;

import android.text.format.DateFormat;

public class HTMLBuilder {
	
	public static final String DATE_FORMAT = "d MMMM, yyyy";
	
	private HTMLBuilder() {
	}
	
	/**
	 * Creates a strip HTML 'page'.
	 *  
	 * @param src The url to the image
	 * @return A HTML syntax page
	 */
	public static String buildImageHtmlImageView(String src) {
		StringBuilder builder = new StringBuilder("<html><head></head>");
		builder.append("<body bgcolor='#000000'><img src='");
		builder.append(src);
		builder.append("' alt='");
		builder.append("Dagens Rocky");
		builder.append("'></body></html>");
		return builder.toString();
	}
	
	/**
	 * Creates an error page to display that the rocky strip wasn't found.
	 * 
	 * @param date The date the strip wasn't found
	 * @return
	 */
	public static String buildRockyNotFoundView(Date date) {
		StringBuilder builder = new StringBuilder("<html><head></head>");
		builder.append("<body><p style='font-weight:bold'>");
		builder.append("Ingen rocky ");
		builder.append(DateFormat.format(DATE_FORMAT, date));
		builder.append(" :(</p></body></html>");
		return builder.toString();
	}

}
