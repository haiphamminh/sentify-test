package sentifi.stockprice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.exception.InvalidDataException;
import sentifi.stockprice.stock.IStockPriceService;
import sentifi.stockprice.stock.StockPriceService;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StockPriceService.class)
public class StockPriceControllerTest {

	private IStockPriceService stockPriceService;

	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {

		stockPriceService = PowerMock.createPartialMock(StockPriceService.class, "readAndCacheStockData");

		List<List<Object>> data = new ArrayList<List<Object>>();
		List<Object> insideData = new ArrayList<Object>();

		Collections.addAll(insideData, "2017-03-01", 49.25);
		data.add(insideData);

		insideData.clear();
		Collections.addAll(insideData, "2017-02-27", 48.7);
		data.add(insideData);

		insideData.clear();
		Collections.addAll(insideData, "2017-02-24", 48.55);
		data.add(insideData);

		try {
			PowerMock.expectPrivate(stockPriceService, "readAndCacheStockData", "tickerSymbol")
					.andReturn(new ClosePriceCacheData(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

		PowerMock.replay(stockPriceService);
	}

	@Test
	public void closePriceRestApiTestWithInvalidStartDate() {
		String fakerTicker = "fakerTicker";
		String fakeDate = "fakeDate";

		String result = stockPriceService.closePriceRestApi(fakerTicker, fakeDate, fakeDate);
		String expectedResult = (new InvalidDataException(fakerTicker, "404", "An invalid start date is provided"))
				.convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void closePriceRestApiTestWithInvalidEndDate() {
		String fakerTicker = "fakerTicker";
		String fakeDate = "fakeDate";

		String result = stockPriceService.closePriceRestApi(fakerTicker, "2017-02-24", fakeDate);
		String expectedResult = (new InvalidDataException(fakerTicker, "404", "An invalid end date is provided"))
				.convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void closePriceRestApiTestWithStartDateAfterEndDate() {
		String fakerTicker = "fakerTicker";
		String startDate = "2017-02-30";
		String endDate = "2017-02-20";

		String result = stockPriceService.closePriceRestApi(fakerTicker, startDate, endDate);
		String expectedResult = (new InvalidDataException(fakerTicker, "404", "An invalid range of dates is provided"))
				.convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}
	/*
	@Test
	public void closePriceRestApiTestWithValidRangeOfDate() {
		String fakerTicker = "fakerTicker";
		String startDate = "2017-02-24";
		String endDate = "2017-03-01";
		
		String result = stockPriceService.closePriceRestApi(fakerTicker, startDate, endDate);
	}*/
}
