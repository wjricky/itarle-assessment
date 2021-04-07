package itarle_assessment;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.csv.*;

public class CsvParsing {

	static String csvFilePath = "C:\\Users\\Ricky\\Documents\\scandi.csv\\scandi.csv";
	static Charset csvCharSet = Charset.forName("UTF-8");

	static int csvPosCode = 0;
	static int csvPosBid = 2;
	static int csvPosAsk = 3;
	static int csvPosTradePrice = 4;
	static int csvPosBidVol = 5;
	static int csvPosAskVol = 6;
	static int csvPosTradeVol = 7;
	static int csvPosUpdateType = 8;
	static int csvPosDate = 10;
	static int csvPosTime = 11;
	static int csvPosOpeningPrice = 12;
	static int csvPosCondition = 14;

	static String updateTypeTrade = "1";
	static String updateTypeBid = "2";
	static String updateTypeAsk = "3";
	
	static int reportTyoe = 2;//1=daily, 2=consolidated
	static boolean isSkipAuction = false;//skip record outside marketOpen and marketClose
	static int marketOpen = 31162;	//08:39
	static int marketClose = 55199; //15:20
	static boolean isSkipCrossSpread = true;	

	public static void main(String[] args) {
		long startTimestamp = System.currentTimeMillis();
		File csvData = new File(csvFilePath);
		HashMap<String, HashMap<String, StockData>> tradeMap = new HashMap<String, HashMap<String, StockData>>();

		try {
			CSVParser parser = CSVParser.parse(csvData, csvCharSet, CSVFormat.RFC4180);
			for (CSVRecord csvRecord : parser) {
				double currentTradeTime = Double.parseDouble(csvRecord.get(csvPosTime));
				if ((csvRecord.get(csvPosCondition).isEmpty() || csvRecord.get(csvPosCondition).isBlank()
						|| csvRecord.get(csvPosCondition).equals("XT")) // only process XT or blank
						&& (!isSkipAuction || (currentTradeTime >= marketOpen && currentTradeTime <= marketClose))) { //check if between predefined market hour

					String date = csvRecord.get(csvPosDate);
					String stockCode = csvRecord.get(csvPosCode);
					HashMap<String, StockData> tradeDay = tradeMap.get(date);
					String updateType = csvRecord.get(csvPosUpdateType).trim();

					if (tradeDay == null) {// empty day
						tradeDay = new HashMap<String, StockData>();
						tradeMap.put(date, tradeDay);
					}
					if (tradeDay.get(stockCode) == null) {// empty stock
						tradeDay.put(stockCode, new StockData());
					}

					StockData stockdata = tradeDay.get(stockCode);
					double currentTradePrice = Double.parseDouble(csvRecord.get(csvPosTradePrice));					

					if (updateType.equals(updateTypeTrade)) {// update type is trade

						if (stockdata.getLastTradePrice() != null) {// not the first trade record
							stockdata.addTradeInterval(currentTradeTime - stockdata.getLastTradeTime());
							double lastTradePrice = stockdata.getLastTradePrice(); //process tick change
							if (lastTradePrice != currentTradePrice) {//only process tick status if price changed
								boolean isCurrentPriceUp = lastTradePrice < currentTradePrice;// check if price is going up
								if (stockdata.isTickUp() != null && stockdata.isTickUp() != isCurrentPriceUp) {//not the first tick record
									stockdata.addTickChangeInterval(currentTradeTime - stockdata.getLastTickChangeTime());
								}
								// first tick record only need to do the following
								stockdata.setTickUp(isCurrentPriceUp);
								stockdata.setLastTickChangeTime(currentTradeTime);
							}
						}
						// first trade record only need to do the following
						stockdata.setLastTradePrice(currentTradePrice);
						stockdata.setLastTradeTime(currentTradeTime);
						stockdata.setLastTradeVolume(Integer.parseInt(csvRecord.get(csvPosTradeVol)));//only for checking trade volume rounding 
						
						//check suspected price rounding
						String priceStr = csvRecord.get(csvPosTradePrice);
						Integer mostTrailingZeroPrice = stockdata.getTrailingZeroPrice();
						if (mostTrailingZeroPrice == null || mostTrailingZeroPrice > 0) { //don't need to check if one record has no trailing zero
							int currentTrailingZeroPrice = countTrailingZero(priceStr);
							if (mostTrailingZeroPrice == null || currentTrailingZeroPrice < mostTrailingZeroPrice) {
								stockdata.setTrailingZeroPrice(currentTrailingZeroPrice);
							}
						}
						
						//check suspected volume rounding
						String volumeStr = csvRecord.get(csvPosTradeVol);
						Integer mostTrailingZeroVol = stockdata.getTrailingZeroVolume();
						if (mostTrailingZeroVol == null || mostTrailingZeroVol > 0) {
							int currentTrailingZeroVol = countTrailingZero(volumeStr);
							if (mostTrailingZeroVol == null || currentTrailingZeroVol < mostTrailingZeroVol) {
								stockdata.setTrailingZeroVolume(currentTrailingZeroVol);
							}
						}
						
					} else if (updateType.equals(updateTypeBid) || updateType.equals(updateTypeAsk)) {// update type is bid or ask, process spread
						BigDecimal currentBidPrice = new BigDecimal(csvRecord.get(csvPosBid));
						BigDecimal currentAskPrice = new BigDecimal(csvRecord.get(csvPosAsk));
						double spread = currentAskPrice.subtract(currentBidPrice).doubleValue();				
						if (spread < 0 && isSkipCrossSpread) { //do nothing, exclude cross spread
							//System.out.println("Auction at:" + currentTradeTime +" Spread:"+ currentAskPrice.subtract(currentBidPrice).doubleValue() + " stockCode:" + stockCode);
						}else {
							stockdata.addBidAskSpread(spread);
						}
					}
				}
			}//end for parser
			
			//report with data consolidated from all trade day			
			System.out.println("Stock code, Mean time between trades, Median time between trades, Mean time between tick changes, Median time between tick changes, Longest time between trades, Longest time between tick changes, Mean bid ask spread, Median bid ask spread, Suspected number of digit rounded (Price), Suspected number of digit rounded (Volume), Initial price (For rounding reference), Initial volume (For rounding reference)"); 
			HashMap<String, StockData> consolidatedTrade = new  HashMap<String, StockData>();//to consolidate all trading days data				
			for (String dateKey : tradeMap.keySet()) {
				for (String stockKey : tradeMap.get(dateKey).keySet()) {
					StockData stockData= tradeMap.get(dateKey).get(stockKey);
					if (consolidatedTrade.get(stockKey) == null) {							
						consolidatedTrade.put(stockKey, new StockData());
						consolidatedTrade.get(stockKey).setLastTradePrice(stockData.getLastTradePrice());//initial data only, only used for reference		
						consolidatedTrade.get(stockKey).setLastTradeVolume(stockData.getLastTradeVolume());			
					}
					
					StockData consolidatedStock = consolidatedTrade.get(stockKey);						
					consolidatedStock.getBidAskSpread().addAll(stockData.getBidAskSpread()); //combining arraylists from different trade day
					consolidatedStock.getTickChangeInterval().addAll(stockData.getTickChangeInterval());
					consolidatedStock.getTradeInterval().addAll(stockData.getTradeInterval());
					
					Integer consolidatedTrailingZeroPrice = consolidatedStock.getTrailingZeroPrice();
					Integer currentTrailingZeroPrice = stockData.getTrailingZeroPrice();
					if (consolidatedTrailingZeroPrice == null || consolidatedTrailingZeroPrice < currentTrailingZeroPrice) { //compare the rounding from each day and take the smallest
						consolidatedStock.setTrailingZeroPrice(currentTrailingZeroPrice);
					}
					
					Integer consolidatedTrailingZeroVolume = consolidatedStock.getTrailingZeroVolume();
					Integer currentTrailingZeroVolume = stockData.getTrailingZeroVolume();
					if (consolidatedTrailingZeroVolume == null || consolidatedTrailingZeroVolume < currentTrailingZeroVolume) {
						consolidatedStock.setTrailingZeroVolume(currentTrailingZeroVolume);						
					}															
				}
			}			
			for (String stockCode : consolidatedTrade.keySet()) {//calculate and print consolidated stock data
				StockData consolidatedStock =  consolidatedTrade.get(stockCode);
	
				ArrayList <Double> tradeIntervals = consolidatedStock.getTradeInterval();
				double tradeAverage = tradeIntervals.stream().mapToDouble(a -> a).average().orElse(0.0);
				double tradeMax = tradeIntervals.stream().mapToDouble(a -> a).max().orElse(0.0);
				Collections.sort(tradeIntervals);
				double tradeMedian  = tradeIntervals.get(tradeIntervals.size()/2);	
				
				ArrayList <Double> tickIntervals = consolidatedStock.getTickChangeInterval();
				double tickAverage = tickIntervals.stream().mapToDouble(a -> a).average().orElse(0.0);
				double tickMax = tickIntervals.stream().mapToDouble(a -> a).max().orElse(0.0);
				Collections.sort(tickIntervals);
				double tickMedian  = tickIntervals.get(tickIntervals.size()/2);	
									
				ArrayList <Double> bidAskSpread = consolidatedStock.getBidAskSpread();
				double bidAskAverage = bidAskSpread.stream().mapToDouble(a -> a).average().orElse(0.0);		
				Collections.sort(bidAskSpread);
				double bidAskMedian = bidAskSpread.get(bidAskSpread.size()/2);	
				
				System.out.println( stockCode + "," + tradeAverage + "," + tradeMedian + "," + tickAverage + "," + tickMedian + "," + tradeMax + "," + tickMax + "," + bidAskAverage + "," + bidAskMedian + "," + consolidatedStock.getTrailingZeroPrice() + "," + consolidatedStock.getTrailingZeroVolume() + "," + consolidatedStock.getLastTradePrice() + "," + consolidatedStock.getLastTradeVolume()) ;
			}
				
		} catch (IOException e) {
			System.out.println("Failed to read CSV from:" + csvFilePath);
			e.printStackTrace();
		}
		System.out.println("Completed:" + csvFilePath);
		long duration = System.currentTimeMillis() - startTimestamp;
		System.out.println("Analysis completed in: " + duration + "ms");
	}	

	public static int countTrailingZero(String str) {
		int i = 0;
		if (!str.isEmpty()) {
			char lastChar = str.charAt(str.length() - 1);
			while ( lastChar == '0' || lastChar == '.') {//while '.' or '0' at left most
				if(lastChar == '0') {
					i++;
				}
				str = str.substring(0, str.length() - 1);
				lastChar = str.charAt(str.length() - 1);
			}
		}	
		return i;
	}
	
}