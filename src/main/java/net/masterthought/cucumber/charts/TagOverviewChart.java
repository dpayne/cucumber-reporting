package net.masterthought.cucumber.charts;

import com.google.gson.Gson;
import net.masterthought.cucumber.Project;
import net.masterthought.cucumber.TagObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 7/7/12
 * Time: 5:37 PM
 */
public class TagOverviewChart {
    public static String generateTagOverviewChartScenariosData(Project project) {

        //generate array with following format
        /*
         *  [
         *  [{x: <index>, y: <numberPassed>},...],
         *  [{x: <index>, y: <numberFailed>},...],
         *  ]
         */
        List<List<TagOverviewChartItem>> data = new ArrayList<List<TagOverviewChartItem>>(2);
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));

        TagOverviewChartItem item0;
        TagOverviewChartItem item1;
        List<TagObject> tags = project.getTags();
        for (int i = 0, tagsSize = tags.size(); i < tagsSize; i++) {
            TagObject tagObject = tags.get(i);
            item0 = new TagOverviewChartItem(i, tagObject.getNumberOfScenariosPassed(), tagObject.getTagName());
            item1 = new TagOverviewChartItem(i, tagObject.getNumberOfScenariosFailed(), tagObject.getTagName());

            data.get(0).add(item0);
            data.get(1).add(item1);
        }
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    public static String generateTagOverviewChartStepsData(Project project) {
        List<List<TagOverviewChartItem>> data = new ArrayList<List<TagOverviewChartItem>>(4);
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));
        data.add(new ArrayList<TagOverviewChartItem>(project.getTags().size()));

        TagOverviewChartItem item0;
        TagOverviewChartItem item1;
        TagOverviewChartItem item2;
        TagOverviewChartItem item3;
        List<TagObject> tags = project.getTags();
        for (int i = 0, tagsSize = tags.size(); i < tagsSize; i++) {
            TagObject tagObject = tags.get(i);
            item0 = new TagOverviewChartItem(i, tagObject.getNumberOfStepsPassed(), tagObject.getTagName());
            item1 = new TagOverviewChartItem(i, tagObject.getNumberOfStepsFailed(), tagObject.getTagName());
            item2 = new TagOverviewChartItem(i, tagObject.getNumberOfStepsSkipped(), tagObject.getTagName());
            item3 = new TagOverviewChartItem(i, tagObject.getNumberOfStepsPending(), tagObject.getTagName());

            data.get(0).add(item0);
            data.get(1).add(item1);
            data.get(2).add(item2);
            data.get(3).add(item3);
        }
        Gson gson = new Gson();
        return gson.toJson(data);
    }
}
