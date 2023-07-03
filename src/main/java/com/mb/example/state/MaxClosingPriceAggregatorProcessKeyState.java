package com.mb.example.state;

import java.io.Serializable;

public class MaxClosingPriceAggregatorProcessKeyState implements Serializable {
	public MaxClosingPriceAggregatorProcessKeyState() {
	}

	private static final long serialVersionUID = 1L;

	private String key;

	@Override
	public String toString() {
		return "MaxClosingPriceAggregatorProcessKeyState{" +
				"key='" + key + '\'' +
				", count=" + count +
				", maxClosingPrice=" + maxClosingPrice +
				'}';
	}

	private int count;
	private double maxClosingPrice;

	public String getMaxClosingDateStr() {
		return maxClosingDateStr;
	}

	public void setMaxClosingDateStr(String maxClosingDateStr) {
		this.maxClosingDateStr = maxClosingDateStr;
	}

	private String  maxClosingDateStr;

	public void setMaxClosingPrice(double maxClosingPrice) {
		this.maxClosingPrice = maxClosingPrice;
	}

	private Long timer = null;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Double getMaxClosingPrice() {
		return maxClosingPrice;
	}

	public void setAvgTemp(Double avgTemp) {
		this.maxClosingPrice = avgTemp;
	}

	public Long getTimer() {
		return timer;
	}

	public void setTimer(Long timer) {
		this.timer = timer;
	}
}
