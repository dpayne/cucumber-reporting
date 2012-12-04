package net.masterthought.cucumber.charts;

import net.masterthought.cucumber.TagObject;

import java.util.*;

public class JsChartUtil {

    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        @Override
        public int compare(String a, String b) {
            return (base.get(a).compareTo(base.get(b)));
        }
    }

    public List<String> orderStepsByValue(int numberTotalPassed, int numberTotalFailed, int numberTotalSkipped, int numberTotalPending) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);

        map.put("#88dd11", numberTotalPassed);
        map.put("#cc1134", numberTotalFailed);
        map.put("#88aaff", numberTotalSkipped);
        map.put("#FBB917", numberTotalPending);

        sorted_map.putAll(map);
        List<String> colours = new ArrayList<String>();
        for (String colour : sorted_map.keySet()) {
            colours.add(colour);
        }
        return colours;
    }

    public List<String> orderScenariosByValue(int numberTotalPassed, int numberTotalFailed) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);

        map.put("#88dd11", numberTotalPassed);
        map.put("#cc1134", numberTotalFailed);

        sorted_map.putAll(map);
        List<String> colours = new ArrayList<String>();
        for (String colour : sorted_map.keySet()) {
            colours.add(colour);
        }
        return colours;
    }

    public static String generateTagChartData(List<TagObject> tagObjectList) {
        StringBuilder buffer = new StringBuilder();
        for (TagObject tag : tagObjectList) {
           buffer.append("[[").append(tag.getNumberOfStepsPassed()).append(",").append(tag.getNumberOfStepsFailed()).append(",").append(tag.getNumberOfStepsSkipped()).append(",").append(tag.getNumberOfStepsPending()).append("],{label:'").append(tag.getTagName()).append("'}],");
        }
        return buffer.toString();
    }
}
