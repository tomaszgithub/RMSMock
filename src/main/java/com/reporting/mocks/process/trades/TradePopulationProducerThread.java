package com.reporting.mocks.process.trades;

import com.reporting.mocks.configuration.TradeConfig;
import com.reporting.mocks.endpoints.RiskRunPublisher;
import com.reporting.mocks.generators.TradeGenerator;
import com.reporting.mocks.model.trade.Trade;
import com.reporting.mocks.model.TradeLifecycle;
import com.reporting.mocks.model.TradeLifecycleType;
import com.reporting.mocks.persistence.ITradeStore;
import com.reporting.mocks.process.intraday.IntradayEvent;
import com.reporting.mocks.process.intraday.IntradayEventType;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TradePopulationProducerThread implements Runnable {
    protected ITradeStore tradeStore;
    protected TradeGenerator tradeGenerator;
    protected BlockingQueue<TradeLifecycleType> tradeEventQueue;
    protected BlockingQueue<IntradayEvent<?>> intradayEventQueue;
    protected TradeConfig tradeConfig;
    protected RiskRunPublisher riskRunPublisher;

    public TradePopulationProducerThread(TradeConfig tradeConfig,
                                         ITradeStore tradeStore,
                                         TradeGenerator tradeGenerator,
                                         BlockingQueue<IntradayEvent<?>> intradayEventQueue,
                                         RiskRunPublisher riskRunPublisher) {
        this.tradeEventQueue = new ArrayBlockingQueue(1024);
        this.tradeStore = tradeStore;
        this.tradeGenerator = tradeGenerator;
        this.intradayEventQueue = intradayEventQueue;
        this.tradeConfig = tradeConfig;
        this.riskRunPublisher = riskRunPublisher;
    }

    @Override
    public void run() {
        TimerTask newTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.New);
        TimerTask deleteTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Delete);
        TimerTask modifiedTradeTask = new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Modify);
        //running timer task as daemon thread
        Timer tradeTimer = new Timer(true);
        tradeTimer.schedule(newTradeTask, this.tradeConfig.getNewTradePeriodicity());

        Timer deleteTradeTimer = new Timer(true);
        deleteTradeTimer.schedule(deleteTradeTask, this.tradeConfig.getDeleteTradePeriodicity());

        Timer modifiedTradeTimer = new Timer(true);
        modifiedTradeTimer.schedule(modifiedTradeTask, this.tradeConfig.getModifiedTradePeriodicity());

        try {
            while(true) {
                TradeLifecycleType tradeEvent = this.tradeEventQueue.take();
                switch (tradeEvent) {
                    case New:
                        int nextNewTrade  = (new Random()).nextInt(this.tradeConfig.getNewTradePeriodicity());
                        Trade newTrade = this.tradeGenerator.generateOneOtc();
                        TradeLifecycle newTradeLifecycle = new TradeLifecycle(tradeEvent, newTrade);
                        this.tradeStore.add(newTrade);
                        this.riskRunPublisher.publishIntradayTrade(newTradeLifecycle);
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, newTradeLifecycle));
                        tradeTimer.schedule(new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.New), nextNewTrade);
                        break;
                    case Modify:
                        int nextModifyTrade  = (new Random()).nextInt(this.tradeConfig.getModifiedTradePeriodicity());
                        Trade tradeToModify = this.tradeStore.oneAtRandom();
                        Trade modifiedTrade = tradeToModify.createNewVersion();
                        TradeLifecycle modifiedTradeLifecycle = new TradeLifecycle(tradeEvent, modifiedTrade);
                        this.tradeStore.modified(tradeToModify, modifiedTrade);
                        this.riskRunPublisher.publishIntradayTrade(modifiedTradeLifecycle);
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, modifiedTradeLifecycle));
                        modifiedTradeTimer.schedule(new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Modify), nextModifyTrade);
                        break;
                    case Delete:
                        int newDeleteTrade  = (new Random()).nextInt(this.tradeConfig.getModifiedTradePeriodicity());
                        Trade tradeToDelete = this.tradeStore.oneAtRandom();
                        TradeLifecycle deleteTradeLifecycle = new TradeLifecycle(tradeEvent, tradeToDelete);
                        this.tradeStore.delete(tradeToDelete.getTcn());
                        this.riskRunPublisher.publishIntradayTrade(deleteTradeLifecycle);
                        this.intradayEventQueue.put(new IntradayEvent<>(IntradayEventType.Trade, deleteTradeLifecycle));
                        deleteTradeTimer.schedule(new TradeEventTimerThread(this.tradeEventQueue, TradeLifecycleType.Delete), newDeleteTrade);
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
