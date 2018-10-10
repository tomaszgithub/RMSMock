package com.reporting.mocks.generators.Trade;

import com.reporting.mocks.configuration.UnderlyingSetConfig;
import com.reporting.mocks.generators.ITradeGenerator;
import com.reporting.mocks.model.trade.TradeType;
import com.reporting.mocks.model.trade.TradeTypes.Spot;
import com.reporting.mocks.model.underlying.Underlying;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

public class SpotGenerator implements ITradeGenerator<Spot> {
    @Override
    public TradeType getTradeType() {
        return TradeType.Spot;
    }

    @Override
    public Spot generate(UnderlyingSetConfig underlyingSetConfig, String book) {
        Random rand = new Random();
        Double price = rand.nextDouble();
        Date settlementDate = Date.from(LocalDate.now().plusDays(2).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Underlying underlying1 = underlyingSetConfig.selectRandomUnderlying1();
        Underlying underlying2 = underlyingSetConfig.selectRandomUnderlying(underlying1.getName());
        Spot spot = new Spot(
                book,
                new Random().nextDouble() * 1000000,
                underlying1,
                underlying2,
                settlementDate,
                price);
        return spot;
    }
}
