package sentifi.stockprice;

import java.io.IOException;
import java.util.Properties;

public class StockPricePropertiesComponent {

	private Properties properties;
	private static StockPricePropertiesComponent instance;

	public static StockPricePropertiesComponent getInstance() {
		if (instance == null) {
			instance = new StockPricePropertiesComponent();
		}
		return instance;
	}

	private StockPricePropertiesComponent() {
		Properties properties = new Properties();
		try {
			properties.load(StockPricePropertiesComponent.class.getResourceAsStream("/application.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		invalidStartDateMsg = properties.getProperty("invalid_startdate");
		invalidEndDateMsg = properties.getProperty("invalid_enddate");
		invalidRangeOfDatesMsg = properties.getProperty("invalid_rangeofdates");
		invalidTickerSymbolMsg = properties.getProperty("invalid_ticker_symbol");
		noDataForStartDateMsg = properties.getProperty("no_data_for_start_date");
		noTickerSymbolMsg = properties.getProperty("no_ticker_symbol");
		dataForFirstPossibleStartDateMsg = properties.getProperty("data_for_first_possible_start_date");
	}

	private String invalidStartDateMsg;
	private String invalidEndDateMsg;
	private String invalidRangeOfDatesMsg;
	private String invalidTickerSymbolMsg;
	private String noDataForStartDateMsg;
	private String noTickerSymbolMsg;
	private String dataForFirstPossibleStartDateMsg;

	public String getInvalidStartDateMsg() {
		return invalidStartDateMsg;
	}

	public String getInvalidEndDateMsg() {
		return invalidEndDateMsg;
	}

	public String getInvalidRangeOfDatesMsg() {
		return invalidRangeOfDatesMsg;
	}

	public String getInvalidTickerSymbolMsg() {
		return invalidTickerSymbolMsg;
	}

	public String getNoDataForStartDateMsg() {
		return noDataForStartDateMsg;
	}

	public String getNoTickerSymbolMsg() {
		return noTickerSymbolMsg;
	}

	public String getDataForFirstPossibleStartDateMsg() {
		return dataForFirstPossibleStartDateMsg;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
