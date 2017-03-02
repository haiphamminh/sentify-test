package sentifi.stockprice.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjUtils {
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
}
