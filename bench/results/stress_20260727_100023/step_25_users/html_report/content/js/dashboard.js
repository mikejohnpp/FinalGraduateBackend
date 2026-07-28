/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9984005686866891, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET /users/admin/reports/overview"], "isController": false}, {"data": [0.996, 500, 1500, "GET /users/admin/sentiment/overview"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "GET /notifications/unread-count"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "POST /users/posts (Create Post)"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "GET /users/search"], "isController": false}, {"data": [0.996, 500, 1500, "GET /users/friends/suggestions"], "isController": false}, {"data": [1.0, 500, 1500, "GET /users/posts/{postId}/comments (List Comments)"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "POST /users/posts/{postId}/comments (Create Comment)"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "GET /chat/conversations/online"], "isController": false}, {"data": [0.0, 500, 1500, "POST /auth/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /chat/conversations/user/{userId}"], "isController": false}, {"data": [1.0, 500, 1500, "GET /users/{id}/profile"], "isController": false}, {"data": [1.0, 500, 1500, "GET /auth/validate-token"], "isController": false}, {"data": [0.9973333333333333, 500, 1500, "GET /users/posts/suggested (Feed)"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "GET /users/groups/suggested"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "POST /users/posts/{id}/like"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "GET /users/groups/joined"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 5627, 0, 0.0, 115.96427936733603, 63, 2195, 100.0, 175.19999999999982, 216.0, 341.3200000000015, 52.26493781521972, 113.23019188056715, 26.605706652355035], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /users/admin/reports/overview", 375, 0, 0.0, 112.84266666666663, 86, 487, 102.0, 134.0, 174.2, 275.32000000000016, 3.7804324814758807, 4.530680966152528, 1.8680652691667927], "isController": false}, {"data": ["GET /users/admin/sentiment/overview", 375, 0, 0.0, 159.85066666666654, 113, 550, 142.0, 209.40000000000003, 244.79999999999995, 438.120000000001, 3.7787182587666264, 4.621274026350262, 1.874598511185006], "isController": false}, {"data": ["GET /notifications/unread-count", 375, 0, 0.0, 79.50399999999999, 63, 999, 75.0, 85.0, 89.19999999999999, 147.72000000000003, 3.784132878564653, 4.102216839643585, 1.89576188154655], "isController": false}, {"data": ["POST /users/posts (Create Post)", 375, 0, 0.0, 111.24000000000004, 82, 804, 101.0, 131.0, 161.0, 366.8000000000002, 3.7839037778495315, 5.808597770397764, 2.300593789100339], "isController": false}, {"data": ["GET /users/search", 375, 0, 0.0, 94.3119999999999, 67, 532, 84.0, 113.2000000000001, 137.2, 365.4400000000003, 3.784743949456006, 7.154299516435882, 1.825843272491472], "isController": false}, {"data": ["GET /users/friends/suggestions", 375, 0, 0.0, 216.91466666666665, 153, 806, 197.0, 284.40000000000003, 325.19999999999993, 498.2000000000007, 3.7805468182917976, 7.495603539095895, 1.890273409145899], "isController": false}, {"data": ["GET /users/posts/{postId}/comments (List Comments)", 375, 0, 0.0, 88.36, 66, 313, 82.0, 102.2000000000001, 122.79999999999995, 223.88000000000034, 3.7844001977979835, 5.584749750229587, 1.8885043955808298], "isController": false}, {"data": ["POST /users/posts/{postId}/comments (Create Comment)", 375, 0, 0.0, 119.27999999999984, 86, 508, 108.0, 148.80000000000007, 168.59999999999997, 338.96000000000095, 3.7840183247394075, 5.516556735678752, 2.1865910472649115], "isController": false}, {"data": ["GET /chat/conversations/online", 375, 0, 0.0, 80.24000000000005, 64, 553, 76.0, 86.0, 92.19999999999999, 211.56000000000108, 3.7847821479395645, 4.065624810760893, 1.859126387122657], "isController": false}, {"data": ["POST /auth/login", 1, 0, 0.0, 2195.0, 2195, 2195, 2195.0, 2195.0, 2195.0, 2195.0, 0.4555808656036447, 0.7372045842824602, 0.120123861047836], "isController": false}, {"data": ["GET /chat/conversations/user/{userId}", 375, 0, 0.0, 85.91733333333329, 65, 475, 80.0, 94.0, 102.59999999999997, 274.1600000000001, 3.785164175187492, 21.023698834043262, 1.8593140430852622], "isController": false}, {"data": ["GET /users/{id}/profile", 375, 0, 0.0, 98.48266666666667, 66, 377, 84.0, 135.0, 214.0, 267.24000000000024, 3.779327582036604, 6.110395182239176, 1.8195395487734822], "isController": false}, {"data": ["GET /auth/validate-token", 1, 0, 0.0, 83.0, 83, 83, 83.0, 83.0, 83.0, 83.0, 12.048192771084338, 13.495387801204819, 8.094879518072288], "isController": false}, {"data": ["GET /users/posts/suggested (Feed)", 375, 0, 0.0, 136.3519999999999, 98, 684, 123.0, 174.40000000000003, 202.2, 318.0800000000004, 3.783140309107784, 21.444671730610146, 1.9063480463863445], "isController": false}, {"data": ["GET /users/groups/suggested", 375, 0, 0.0, 114.60000000000007, 83, 507, 102.0, 147.0, 184.79999999999995, 345.72000000000116, 3.783789237894397, 10.129647202644616, 1.8808092989143048], "isController": false}, {"data": ["POST /users/posts/{id}/like", 375, 0, 0.0, 114.20799999999996, 87, 739, 104.0, 132.0, 156.39999999999998, 352.28000000000156, 3.7844383893430216, 4.041080472928651, 1.9624382663487738], "isController": false}, {"data": ["GET /users/groups/joined", 375, 0, 0.0, 121.90399999999993, 86, 600, 107.0, 152.60000000000014, 214.0, 355.0000000000007, 3.7842092516347785, 11.3354312831749, 1.8699315247335917], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 5627, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
