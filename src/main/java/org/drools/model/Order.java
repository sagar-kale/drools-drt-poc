package org.drools.model;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private String securityId;
    private String state;
    private Double amount;
    private Double income;
    private Double netWorth;
    private Double netWorthAlt;
    private Double liquidNetWorth;
    private List<String> errors = new ArrayList<>();
    private String status = "";

    public String getSecurityId() {
        return securityId;
    }

    public Order setSecurityId(String securityId) {
        this.securityId = securityId;
        return this;
    }

    public String getState() {
        return state;
    }

    public Order setState(String state) {
        this.state = state;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public Order setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Double getIncome() {
        return income;
    }

    public Order setIncome(Double income) {
        this.income = income;
        return this;
    }

    public Double getNetWorth() {
        return netWorth;
    }

    public Order setNetWorth(Double netWorth) {
        this.netWorth = netWorth;
        return this;
    }

    public Double getNetWorthAlt() {
        return netWorthAlt;
    }

    public Order setNetWorthAlt(Double netWorthAlt) {
        this.netWorthAlt = netWorthAlt;
        return this;
    }

    public Double getLiquidNetWorth() {
        return liquidNetWorth;
    }

    public Order setLiquidNetWorth(Double liquidNetWorth) {
        this.liquidNetWorth = liquidNetWorth;
        return this;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Order setErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Order setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "Order{" +
                "securityId=" + securityId +
                ", state='" + state + '\'' +
                ", amount=" + amount +
                ", income=" + income +
                ", netWorth=" + netWorth +
                ", liquidNetWorth=" + liquidNetWorth +
                ", errors=" + errors +
                ", status='" + status + '\'' +
                '}';
    }
}
