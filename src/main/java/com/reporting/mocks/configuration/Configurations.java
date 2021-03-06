package com.reporting.mocks.configuration;

import com.reporting.mocks.configuration.defaults.FXOptionDeskDefaultPricingGroupConfig;
import com.reporting.mocks.configuration.defaults.FXSpotDeskDefaultPricingGroupConfig;
import com.reporting.mocks.configuration.defaults.FXSwapsDeskDefaultPricingGroupConfig;
import com.reporting.mocks.configuration.defaults.WroclawDeskDefaultPricingGroupConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
//@Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
@Scope
public class Configurations {
    protected Map<String, PricingGroupConfig> pricingGroups;

    public Configurations() {
        this.pricingGroups = new HashMap<>();

        this.addPricingGroup(new FXSwapsDeskDefaultPricingGroupConfig());
        this.addPricingGroup(new FXOptionDeskDefaultPricingGroupConfig());
        this.addPricingGroup(new FXSpotDeskDefaultPricingGroupConfig());
        this.addPricingGroup(new WroclawDeskDefaultPricingGroupConfig());
    }

    public Collection<PricingGroupConfig> getPricingGroups() {
        return pricingGroups.values();
    }

    public PricingGroupConfig addPricingGroup(PricingGroupConfig pricingGroup) {
        return this.pricingGroups.put(pricingGroup.getPricingGroupId().getName(), pricingGroup);
    }

    public PricingGroupConfig getPricingGroup(String name) {
        if (this.pricingGroups.containsKey(name)) {
            return this.pricingGroups.get(name);
        }
        else {
            return null;
        }
    }
}
