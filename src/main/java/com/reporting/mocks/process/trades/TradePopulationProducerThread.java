package com.reporting.mocks.process.trades;

import com.reporting.mocks.configuration.TradeConfig;
import com.reporting.mocks.generators.TradeGenerator;
import com.reporting.mocks.model.Trade;
import com.reporting.mocks.model.TradeLifecycle;
import com.reporting.mocks.model.TradeLifecycleType;
import com.reporting.mocks.persistence.TradeStore;
import com.reporting.mocks.process.intraday.IntradayEvent;
import com.reporting.mocks.process.intraday.IntradayEventType;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TradePopulationProducerThread implements Runnable {
    protected TradeStore tradeStore;
    protected BlockingQueue<TradeLifecycleType> tradeEventQueue;
    protected BlockingQueue<IntradayEvent<?>> intradayEventQueue;
    protected TradeConfig tradeConfig;

    public TradePopulationProducerThread(TradeConfig tradeConfig,
                                         TradeStore tradeStore,
                                         BlockingQueue<IntradayEvent<?>> intradayEventQueue) {
        this.tradeEventQueue = new ArrayBlockingQueue(1024);
        this.tradeStore = tradeStore;
        this.intradayEventQueue = intradayEventQueue;
        this.tradeConfig = tradeConfig;
    }

    @Override
    public void run() {
        TimerTask newTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.New);
        TimerTask deleteTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Delete);
        TimerTask modifiedTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Modify);
        //running timer task as daemon thread
        Timer tradeTimer = new Timer(true);
        tradeTimer.scheduleAtFixedRate(newTradeTask, 0, 1*1000);
        tradeTimer.scheduleAtFixedRate(deleteTradeTask, 10*1000, 10*1000);
        tradeTimer.scheduleAtFixedRate(modifiedTradeTask, 5*1000, 5*1000);

        try {
            while(true) {
                TradeLifecycleType tradeEvent = this.tradeEventQueue.take();
                switch (tradeEvent) {
                    case New:
                        Trade newTrade = TradeGenerator.generateOne(this.tradeConfig);
                        this.tradeStore.putTrade(newTrade);
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, new TradeLifecycle(tradeEvent, newTrade)));
                        break;
                    case Modify:
                        Trade tradeToModify = this.tradeStore.getTradeAtRandom();
                        Trade modifiedTrade = new Trade(tradeToModify);
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, new TradeLifecycle(tradeEvent, modifiedTrade)));
                        break;
                    case Delete:
                        Trade tradeToDelete = this.tradeStore.getTradeAtRandom();
                        this.tradeStore.deleteTrade(tradeToDelete.getTcn());
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, new TradeLifecycle(tradeEvent, tradeToDelete)));
                        break;
                    default:
                        break;
                }
                System.out.println("TradeEvent " + tradeEvent.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
