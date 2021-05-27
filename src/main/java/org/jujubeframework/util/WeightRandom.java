package org.jujubeframework.util;

import java.util.*;

import org.apache.commons.math3.util.Pair;

import com.google.common.base.Preconditions;

/**
 * @author zhangliyong
 */
public class WeightRandom {
    private final TreeMap<Double, String> weightMap = new TreeMap<>();

    public WeightRandom(Map<String, Double> data) {
        Preconditions.checkNotNull(data, "list can NOT be null!");
        List<Pair<String, Double>> list = new ArrayList<>(data.size());
        data.forEach((key, value) -> list.add(new Pair<>(key, value)));
        for (Pair<String, Double> pair : list) {
            double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey();
            // 权重累加
            this.weightMap.put(pair.getValue() + lastWeight, pair.getKey());
        }
    }

    public String random() {
        double randomWeight = this.weightMap.lastKey() * Math.random();
        SortedMap<Double, String> tailMap = this.weightMap.tailMap(randomWeight, true);
        return this.weightMap.get(tailMap.firstKey());
    }
}
