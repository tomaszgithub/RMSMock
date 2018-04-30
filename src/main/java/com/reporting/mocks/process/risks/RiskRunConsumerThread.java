package com.reporting.mocks.process.risks;

import com.reporting.mocks.model.RiskRunResult;

import java.util.concurrent.BlockingQueue;

public class RiskRunConsumerThread implements Runnable {
    protected BlockingQueue<RiskRunResult> riskResultQueue;

    public RiskRunConsumerThread(BlockingQueue<RiskRunResult> riskResultQueue) {
        this.riskResultQueue = riskResultQueue;
    }

    @Override
    public void run() {

        while(true) {
            try {
                RiskRunResult result = this.riskResultQueue.take();

                System.out.println("Risk Result: " + result.getId() + " fragment: " + result.getFragmentNo() + "/" + result.getFragmentCount());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}