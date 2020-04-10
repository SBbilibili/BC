package com.meng.bc.update;

public interface WebSocketMessageAction {
	public int useTimes();
	public int forOpCode();
	public BotDataPack onMessage(BotDataPack rec);
}
