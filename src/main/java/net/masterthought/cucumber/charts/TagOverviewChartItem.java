package net.masterthought.cucumber.charts;

/**
 * Date: 7/7/12
 * Time: 5:49 PM
 */
public class TagOverviewChartItem {
    int x;
    int y;
    String label;

    public TagOverviewChartItem() {
    }

    public TagOverviewChartItem(int x, int y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
