var TagOverviewChart = function () {
    "use strict";
    var myBarChart = {},
        margin = 10,
        leftMargin = 60,
        rightMargin = 45,
        bottomMargin = 120,
        topMargin = 10,
        maxLabelLength = 17,
        legendWidth = 65,
        vis,
        legendIconSize = 5,
        legendMarginSize = 10,
        legendGrowSize = 3,
        isGrouped = false;

    function createBarChart(chartElementName, chartContainer, inputData, numberOfLayers, numberOfGroups, inputColors, inputNames) {
        var n = numberOfLayers,
            m = numberOfGroups,
            data = d3.layout.stack()(inputData),
            colors = inputColors,
            width = chartContainer.width() - rightMargin - leftMargin - legendWidth,
            height = chartContainer.height() - 0.5 - margin;

        //this stops the columns from becoming to big
        if ((width / m) > 50) {
            width = m * 50;
        }

        var mx = m,
            my = d3.max(data, function (d) {
                return d3.max(d, function (d) {
                    return d.y0 + d.y;
                });
            }),
            mz = d3.max(data, function (d) {
                return d3.max(d, function (d) {
                    return d.y;
                });
            }),
            y0 = function (d) {
                return height - d.y0 * height / my;
            },
            y1 = function (d) {
                return height - (d.y + d.y0) * height / my;
            },
            x = function (d) {
                return (d.x * width / mx);
            },
            x0 = leftMargin,
            y2 = function (d) {
                return d.y * height / mz;
            }; // or `my` to not rescale

        vis = d3.select("#" + chartElementName)
            .append("svg")
            .attr("width", width + leftMargin + rightMargin + legendWidth)
            .attr("height", height + margin + bottomMargin); //todo: handle max char length for tags

        var i = 0,
            maxYRangeStacked = 0,
            maxYRangeGrouped = 0;

        for (i = 0; i < inputData[0].length; i += 1) {
            if (maxYRangeStacked < (inputData[0][i].y + inputData[1][i].y)) {
                maxYRangeStacked = (inputData[0][i].y + inputData[1][i].y);
            }
            if (maxYRangeGrouped < (inputData[0][i].y)) {
                maxYRangeGrouped = inputData[0][i].y;
            }
            if (maxYRangeGrouped < (inputData[1][i].y)) {
                maxYRangeGrouped = inputData[1][i].y;
            }
        }

        var axisGroup = vis.append("svg:g").
            attr("transform", "translate(" + (margin + 2.5) + ",0)");

        var yScaleStacked = d3.scale.linear().domain([0, maxYRangeStacked]).range([0, height]);
        var yScaleGrouped = d3.scale.linear().domain([0, maxYRangeGrouped]).range([0, height]);

        var yticks = vis.selectAll('.yTicks')
            .data(yScaleStacked.ticks(10))
            .enter().append('svg:g')
            .attr('transform', function (d) {
                return "translate(" + (leftMargin) + "," + (height - yScaleStacked(d) + topMargin) + ")";
            })
            .attr('class', 'yTick');

        yticks.append("svg:line").
            attr("x1", 0).
            attr("y1",function (d) {
                return 0;
            }).
            attr("x2", width).
            attr("y2",function (d) {
                return 0;
            }).
            attr("stroke", "lightgrey");

        yticks.append('svg:text').
            text(function (d) {
                return d;
            }).
            attr('text-anchor', 'start').
            attr("class", "yTickText").
            attr("x", width + 8).
            attr("y",function (d) {
                return 0;
            }).
            attr('dy', 2).
            attr('dx', -4);

        var layers = vis.selectAll("g.layer")
            .data(data)
            .enter().append("g")
            .style("fill", function (d, i) {
                return colors[i];
            })
            .attr("id", function (d, i) {
                return ("tagGroupLayer" + i);
            })
            .on("mouseover", function (d, i) {
                //highlight ith legend element
                var le = vis.select("#legendElement" + i);
                le.select("circle").transition().attr("r", legendIconSize + legendGrowSize);
                le.select("text").transition().style("font-weight", "bold");
            })
            .on("mouseout", function (d, i) {
                var le = vis.select("#legendElement" + i);
                le.select("circle").transition().attr("r", legendIconSize);
                le.select("text").transition().style("font-weight", "normal");
            })
            .attr("class", "layer")
            .attr('transform', function (d) {
                return "translate(0," + topMargin + ")";
            });

        var bars = layers.selectAll("g.bar")
            .data(function (d) {
                return d;
            })
            .enter().append("g")
            .attr("class", "bar")
            .attr("transform", function (d) {
                return "translate(" + (x(d)) + ",0)";
            });

        bars.append("rect")
            .attr("width", x({x:0.9}))
            .attr("x", leftMargin)
            .attr("y", height)
            .attr("height", 0)
            .on("mouseover", function (d, i) {
                var output = '';
                var property;
                for (property in d) {
                    output += property + ': ' + d[property] + '; ';
                }
                $('#breakdownTable tr').eq(i + 1).toggleClass("borderedHover");
            })
            .on("mouseout", function (d, i) {
                $('#breakdownTable tr').eq(i + 1).toggleClass("borderedHover");
            })
            .transition()
            .delay(function (d, i) {
                return i * 10;
            })
            .attr("y", y1)
            .attr("height", function (d) {
                //add tipsy notifications
                window.$(this).tipsy({
                    gravity: 'w',
                    html: true,
                    title: function () {
                        //10 scenarios passed
                        if (isGrouped) {
                            return d.y;
                        } else {
                            return d.y;
                        }
                    }
                });
                return y0(d) - y1(d);
            });

        var gText = vis.selectAll("text.label")
            .data(data[0])
            .enter().append("svg:g")
            .attr("transform", function (d, i) {
                return "translate(" + (leftMargin + x(d)) + "," + (height + margin + 10 + topMargin) + ")";
            });

        gText.append("text")
            .text(function (d, i) {
                if (d.label.length > maxLabelLength) {
                    return (d.label.substring(0, maxLabelLength - 3) + "...");
                }
                return d.label;
            })
            .attr("text-anchor", "end")
            .attr("class", "label")
            .attr("transform", function (d, i) {
                return "translate(20,0) rotate(-45,0,0)";
            });


        axisGroup.selectAll(".xTicks").
            data(data[0]).
            enter().append("svg:line").
            attr("x1",function (d) {
                return x(d) + leftMargin + margin;
            }).
            attr("y1", height + topMargin).
            attr("x2",function (d) {
                return x(d) + leftMargin + margin;
            }).
            attr("y2", height + topMargin + 5).
            attr("stroke", "black").
            attr("class", "xTicks");

        var xAxis = vis.append("svg:line").
            attr("x1", leftMargin - (margin / 2)).
            attr("y1", height + topMargin).
            attr("x2", leftMargin + width).
            attr("y2", height + topMargin).
            attr("stroke", "black").
            attr("class", "xTicks");

        var yAxis = vis.append("svg:line").
            attr("x1", leftMargin + width).
            attr("y1", topMargin).
            attr("x2", leftMargin + width).
            attr("y2", height + topMargin).
            attr("stroke", "black").
            attr("class", "yTicks");

        createLegend(width + leftMargin + rightMargin, topMargin, inputColors, inputNames);

        function transitionGroup() {
            var group = d3.selectAll("#" + chartElementName);
            isGrouped = true;

            function transitionEnd() {
                d3.select(this)
                    .transition()
                    .duration(500)
                    .attr("y", function (d) {
                        return height - y2(d);
                    })
                    .attr("height", y2);
            }

            group.selectAll("g.layer rect")
                .transition()
                .duration(500)
                .delay(function (d, i) {
                    return (i % m) * 20;
                })
                .attr("x", function (d, i) {
                    return x({x:(0.9 * ~~(i / m) / n)}) + leftMargin;
                })
                .attr("width", x({x:0.9 / n}))
                .each("end", transitionEnd);

            group.selectAll("text.yTickText")
                .text(function (d, i) {
                    return Math.round(yScaleGrouped.invert(yScaleStacked(d)));
                });
        }

        function transitionStack() {
            var stack = d3.select("#" + chartElementName);
            isGrouped = false;

            function transitionEnd() {
                d3.select(this)
                    .transition()
                    .duration(500)
                    .attr("x", leftMargin)
                    .attr("width", x({x:0.9}));
            }

            stack.selectAll("g.layer rect")
                .transition()
                .duration(500)
                .delay(function (d, i) {
                    return (i % m) * 20;
                })
                .attr("y", y1)
                .attr("height", function (d) {
                    return y0(d) - y1(d);
                })
                .each("end", transitionEnd);

            stack.selectAll("text.yTickText")
                .text(function (d, i) {
                    return d;
                });
        }

        myBarChart.transitionGroup = transitionGroup;
        myBarChart.transitionStack = transitionStack;
    }

    function createLegend(xPosition, yPosition, colors, names) {
        var textYadjustment = 4,
            legendElements;

        legendElements = vis.selectAll(".legendElement")
            .data(d3.range(colors.length))
            .enter().append('svg:g')
            .attr('transform', function (d, i) {
                return "translate(" + xPosition + "," + (yPosition + (i * legendIconSize + i * legendMarginSize)) + ")";
            })
            .attr('class', 'legendElement')
            .attr('id', function (d, i) {
                return ('legendElement' + i);
            })
            .on("mouseover", function (d, i) {
                d3.select(this).select("circle").transition().attr("r", legendIconSize + 3);
                d3.select(this).select("text").transition().style("font-weight", "bold");
                vis.select("#tagGroupLayer" + i).style("stroke", "black");
            })
            .on("mouseout", function (d, i) {
                d3.select(this).select("circle").transition().attr("r", legendIconSize);
                d3.select(this).select("text").transition().style("font-weight", "normal");
                vis.select("#tagGroupLayer" + i).style("stroke", "none");
            });

        legendElements.append("svg:circle").
            attr("x", 0).
            attr("y", 0).
            attr("r", legendIconSize).
            style("fill", function (d, i) {
                return colors[i];
            });

        legendElements.append("svg:text").
            text(function (d, i) {
                return "-  " + names[i];
            }).
            attr('text-anchor', 'start').
            attr("x", legendGrowSize + legendIconSize).
            attr("y", textYadjustment);
    }

    myBarChart.createBarChart = createBarChart;
    myBarChart.createLegend = createLegend;

    return myBarChart;
};

var scenarioTagChart = new TagOverviewChart();
var stepTagChart = new TagOverviewChart();
