package sentifi.stockprice.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
	private static ObjectMapper mapper;
	private static DateFormat df;

	static {
		mapper = new ObjectMapper();
		df = new SimpleDateFormat("yyyy-MM-dd");
	}

	public static ObjectMapper getObjectMapper() {
		return mapper;
	}

	public static DateFormat getDateFormat() {
		return df;
	}

	public static Date parseDate(String dateStr) {
		Date date = null;
		try {
			date = df.parse(dateStr);
		} catch (ParseException e) {
		}
		return date;
	}
	
	public static String formatDate(Date date) {
		return df.format(date);
	}
}
