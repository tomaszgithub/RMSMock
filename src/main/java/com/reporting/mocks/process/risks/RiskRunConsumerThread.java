package com.reporting.mocks.process.risks;

import com.google.gson.Gson;

import java.util.concurrent.BlockingQueue;

public class RiskRunConsumerThread implements Runnable {
    protected BlockingQueue<RiskResult> riskResultQueue;

    public RiskRunConsumerThread(BlockingQueue<RiskResult> riskResultQueue) {
        this.riskResultQueue = riskResultQueue;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        while(true) {
            try {
                RiskResult result = this.riskResultQueue.take();

                String resultString = gson.toJson(result);

                System.out.println(resultString);
                //System.out.println("{Risk Result: (" + result.getRequest().getType() + "): " + result.getId() + " Risk: " + result.getRequest() + " fragment: " + result.getFragmentNo() + "/" + result.getFragmentCount() + "}") ;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
