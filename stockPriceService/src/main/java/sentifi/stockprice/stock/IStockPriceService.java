package sentifi.stockprice.stock;

public interface IStockPriceService {

	String closePriceRestApi(String ticker_symbol, String startDateStr, String endDateStr);

	String twoHundredDayMovingAverageRestApi(String ticker_symbol, String startDateStr);

	String twoHundredDayMovingAverageForTickerSymbolsRestApi(String startDateStr, String tickerSymbols);

}