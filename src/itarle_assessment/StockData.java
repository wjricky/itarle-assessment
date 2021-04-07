package itarle_assessment;

import java.util.ArrayList;

public class StockData {
	
	private Boolean isRoundNumer;
	private Boolean isTickUp;
	
	private Double lastTradeTime;
	private Double lastTradePrice;
	private Double lastTickChangeTime;
	private Integer lastTradeVolume; //to get rounding only
	
	private ArrayList<Double> tradeInterval;
	private ArrayList<Double> tickChangeInterval;
	private ArrayList<Double> bidAskSpread;
	
	private Integer trailingZeroPrice;
	private Integer trailingZeroVolume;
	
	public StockData() {
		super();
		tradeInterval = new ArrayList<Double>() ;
		tickChangeInterval= new ArrayList<Double>() ;
		bidAskSpread = new ArrayList<Double>() ;
	}

	public boolean isRoundNumer() {
		return isRoundNumer;
	}

	public void setRoundNumer(boolean isRoundNumer) {
		this.isRoundNumer = isRoundNumer;
	}
	
	public Boolean isTickUp() {
		return isTickUp;
	}

	public void setTickUp(boolean isTickUp) {
		this.isTickUp = isTickUp;
	}
	
	public Double getLastTradeTime() {
		return lastTradeTime;
	}
	
	public void setLastTradeTime(double lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}
	
	public Double getLastTradePrice() {
		return lastTradePrice;
	}

	public void setLastTradePrice(double lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
	}
	
	public Double getLastTickChangeTime() {
		return lastTickChangeTime;
	}

	public void setLastTickChangeTime(double lastTickChangeTime) {
		this.lastTickChangeTime = lastTickChangeTime;
	}
	
	public Integer getLastTradeVolume() {
		return lastTradeVolume;
	}

	public void setLastTradeVolume(Integer lastTradeVolume) {
		this.lastTradeVolume = lastTradeVolume;
	}
		
	public ArrayList<Double> getTradeInterval() {
		return tradeInterval;
	}

	public void addTradeInterval(Double d) {
		tradeInterval.add(d);
	}

	public ArrayList<Double> getTickChangeInterval() {
		return tickChangeInterval;
	}

	public void addTickChangeInterval(Double d) {
		tickChangeInterval.add(d);
	}

	public ArrayList<Double> getBidAskSpread() {
		return bidAskSpread;
	}
	
	public void addBidAskSpread(Double d) {
		bidAskSpread.add(d);
	}

	public Integer getTrailingZeroPrice() {
		return trailingZeroPrice;
	}

	public void setTrailingZeroPrice(Integer trailingZeroPrice) {
		this.trailingZeroPrice = trailingZeroPrice;
	}

	public Integer getTrailingZeroVolume() {
		return trailingZeroVolume;
	}

	public void setTrailingZeroVolume(Integer trailingZeroVolume) {
		this.trailingZeroVolume = trailingZeroVolume;
	}
}
