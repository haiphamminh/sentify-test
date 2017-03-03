package sentifi.stockprice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sentifi.stockprice.stock.IStockPriceService;

@RestController
public class StockPriceController {

	@Autowired
	private IStockPriceService stockPriceService;

	@RequestMapping(value = "/api/v2/{ticker_symbol}/closePrice", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String closePriceRestApi(@PathVariable String ticker_symbol,
			@RequestParam(value = "startDate") String startDateStr,
			@RequestParam(value = "endDate") String endDateStr) {

		return stockPriceService.closePriceRestApi(ticker_symbol, startDateStr, endDateStr);
	}

	@RequestMapping(value = "/api/v2/{ticker_symbol}/200dma", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String twoHundredDayMovingAverageRestApi(@PathVariable String ticker_symbol,
			@RequestParam(value = "startDate") String startDateStr) {

		return stockPriceService.twoHundredDayMovingAverageRestApi(ticker_symbol, startDateStr);
	}

	@RequestMapping(value = "/api/v2/200dma", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String twoHundredDayMovingAverageForTickerSymbolsRestApi(
			@RequestParam(value = "startDate") String startDateStr,
			@RequestParam(value = "tickerSymbols") String tickerSymbols) {

		return stockPriceService.twoHundredDayMovingAverageForTickerSymbolsRestApi(startDateStr, tickerSymbols);
	}
}
