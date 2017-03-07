package sentifi.stockprice.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InvalidDataException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	/*public static final int ERR_CODE = 404;
	public static final String INVALID_STARTDATE = "An invalid start date is provided";
	public static final String INVALID_ENDDATE = "An invalid end date is provided";
	public static final String INVALID_RANGE_OF_DATE = "An invalid range of dates is provided";
	public static final String INVALID_TICKER_SYMBOL = "An invalid ticker symbol is provided";
	public static final String NO_DATA_FOR_START_DATE = "No data for the start date. The first possible start date is %s";
	public static final String NO_TICKER_SYMBOL = "No ticker symbol is provided";*/

	private String ticker;
	private String code;
	private String message;

	public InvalidDataException(String ticker, String code, String message) {
		this.ticker = ticker;
		this.code = code;
		this.message = message;
	}
	
	public InvalidDataException(String ticker, String message) {
		this(ticker, String.valueOf(404), message);
		
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String convertIdeAsJsonString() {
		return convertIdeAsJsonNode().toString();
	}

	public JsonNode convertIdeAsJsonNode() {
		ObjectNode contentNode = JsonNodeFactory.instance.objectNode();

		contentNode.put("Ticker", this.ticker);
		contentNode.put("Error code", this.code);
		contentNode.put("Message", this.message);

		return contentNode;
	}
}
