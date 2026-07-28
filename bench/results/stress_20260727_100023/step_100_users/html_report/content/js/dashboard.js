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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9058528130832815, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.8983333333333333, 500, 1500, "GET /users/admin/reports/overview"], "isController": false}, {"data": [0.848, 500, 1500, "GET /users/admin/sentiment/overview"], "isController": false}, {"data": [0.9876666666666667, 500, 1500, "GET /notifications/unread-count"], "isController": false}, {"data": [0.911, 500, 1500, "POST /users/posts (Create Post)"], "isController": false}, {"data": [0.9243333333333333, 500, 1500, "GET /users/search"], "isController": false}, {"data": [0.7346666666666667, 500, 1500, "GET /users/friends/suggestions"], "isController": false}, {"data": [0.9236666666666666, 500, 1500, "GET /users/posts/{postId}/comments (List Comments)"], "isController": false}, {"data": [0.898, 500, 1500, "POST /users/posts/{postId}/comments (Create Comment)"], "isController": false}, {"data": [0.9883333333333333, 500, 1500, "GET /chat/conversations/online"], "isController": false}, {"data": [0.5, 500, 1500, "POST /auth/login"], "isController": false}, {"data": [0.989, 500, 1500, "GET /chat/conversations/user/{userId}"], "isController": false}, {"data": [0.919, 500, 1500, "GET /users/{id}/profile"], "isController": false}, {"data": [1.0, 500, 1500, "GET /auth/validate-token"], "isController": false}, {"data": [0.861, 500, 1500, "GET /users/posts/suggested (Feed)"], "isController": false}, {"data": [0.9036666666666666, 500, 1500, "GET /users/groups/suggested"], "isController": false}, {"data": [0.9033333333333333, 500, 1500, "POST /users/posts/{id}/like"], "isController": false}, {"data": [0.898, 500, 1500, "GET /users/groups/joined"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 22502, 0, 0.0, 322.5067105146209, 61, 4831, 282.0, 655.9000000000015, 840.9500000000007, 1930.8400000000256, 118.15360704027893, 256.20942522722856, 60.170980968458416], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /users/admin/reports/overview", 1500, 0, 0.0, 374.7673333333332, 84, 2820, 312.0, 665.0, 851.6500000000003, 2428.99, 8.206272874985638, 9.851171120621268, 4.05505280736595], "isController": false}, {"data": ["GET /users/admin/sentiment/overview", 1500, 0, 0.0, 448.9173333333335, 113, 3249, 385.0, 748.9000000000001, 899.95, 2534.3800000000015, 8.204477456830775, 10.033434954902722, 4.070189988349642], "isController": false}, {"data": ["GET /notifications/unread-count", 1500, 0, 0.0, 108.10400000000001, 62, 1699, 80.0, 116.0, 201.95000000000005, 796.9000000000001, 8.208113994287151, 8.901197307328204, 4.112072733466122], "isController": false}, {"data": ["POST /users/posts (Create Post)", 1500, 0, 0.0, 335.5466666666669, 77, 3741, 274.5, 607.7000000000003, 770.5500000000004, 2234.060000000001, 8.210630029010893, 12.611661361117193, 4.992127203185724], "isController": false}, {"data": ["GET /users/search", 1500, 0, 0.0, 311.38933333333364, 66, 3634, 259.5, 587.7000000000003, 763.3000000000006, 1789.2800000000016, 8.211888624891877, 15.520351882849196, 3.9615947077115106], "isController": false}, {"data": ["GET /users/friends/suggestions", 1500, 0, 0.0, 562.9053333333331, 153, 4831, 505.0, 871.9000000000001, 1076.95, 2555.130000000003, 8.206811653672547, 16.271606996306936, 4.103405826836274], "isController": false}, {"data": ["GET /users/posts/{postId}/comments (List Comments)", 1500, 0, 0.0, 307.866666666667, 66, 2541, 250.5, 582.8000000000002, 755.8500000000001, 1467.7600000000002, 8.211214329116418, 12.274487765632784, 4.10560716455821], "isController": false}, {"data": ["POST /users/posts/{postId}/comments (Create Comment)", 1500, 0, 0.0, 359.6753333333341, 81, 3685, 296.5, 641.9000000000001, 905.95, 2084.9600000000028, 8.210630029010893, 11.986947877894247, 4.752383220893317], "isController": false}, {"data": ["GET /chat/conversations/online", 1500, 0, 0.0, 108.10466666666682, 61, 2307, 80.0, 110.90000000000009, 192.95000000000005, 897.5700000000004, 8.208742858393714, 8.821213409528708, 4.032224275168006], "isController": false}, {"data": ["POST /auth/login", 1, 0, 0.0, 1476.0, 1476, 1476, 1476.0, 1476.0, 1476.0, 1476.0, 0.6775067750677507, 1.0976403709349594, 0.17863948170731708], "isController": false}, {"data": ["GET /chat/conversations/user/{userId}", 1500, 0, 0.0, 108.23333333333319, 62, 1387, 82.0, 114.0, 191.95000000000005, 917.8600000000001, 8.209102252577658, 45.59207123858935, 4.032400813522034], "isController": false}, {"data": ["GET /users/{id}/profile", 1500, 0, 0.0, 319.8206666666671, 64, 2429, 272.0, 573.7000000000003, 736.7000000000003, 1571.93, 8.206362666520777, 13.265713474847498, 3.9509148384714283], "isController": false}, {"data": ["GET /auth/validate-token", 1, 0, 0.0, 75.0, 75, 75, 75.0, 75.0, 75.0, 75.0, 13.333333333333334, 14.934895833333334, 8.958333333333334], "isController": false}, {"data": ["GET /users/posts/suggested (Feed)", 1500, 0, 0.0, 422.6026666666663, 102, 3185, 373.0, 713.0, 870.0, 2202.810000000004, 8.209731268129824, 46.55852448039242, 4.136934896831043], "isController": false}, {"data": ["GET /users/groups/suggested", 1500, 0, 0.0, 358.0560000000002, 81, 3567, 293.0, 624.0, 834.5500000000004, 2354.8500000000004, 8.208832703989493, 21.97670187913862, 4.080367037432278], "isController": false}, {"data": ["POST /users/posts/{id}/like", 1500, 0, 0.0, 347.0260000000003, 81, 2800, 287.5, 629.9000000000001, 781.7500000000002, 2168.1600000000008, 8.211034535611258, 8.766904467350189, 4.265889036079286], "isController": false}, {"data": ["GET /users/groups/joined", 1500, 0, 0.0, 363.98133333333317, 83, 3603, 311.5, 624.9000000000001, 770.0, 2247.960000000001, 8.209102252577658, 24.589799284029468, 4.0564509177776324], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 22502, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
