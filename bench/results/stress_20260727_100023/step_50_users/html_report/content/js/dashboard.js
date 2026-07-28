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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9960895840739424, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.994, 500, 1500, "GET /users/admin/reports/overview"], "isController": false}, {"data": [0.994, 500, 1500, "GET /users/admin/sentiment/overview"], "isController": false}, {"data": [1.0, 500, 1500, "GET /notifications/unread-count"], "isController": false}, {"data": [0.9986666666666667, 500, 1500, "POST /users/posts (Create Post)"], "isController": false}, {"data": [1.0, 500, 1500, "GET /users/search"], "isController": false}, {"data": [0.972, 500, 1500, "GET /users/friends/suggestions"], "isController": false}, {"data": [0.9973333333333333, 500, 1500, "GET /users/posts/{postId}/comments (List Comments)"], "isController": false}, {"data": [0.998, 500, 1500, "POST /users/posts/{postId}/comments (Create Comment)"], "isController": false}, {"data": [1.0, 500, 1500, "GET /chat/conversations/online"], "isController": false}, {"data": [0.0, 500, 1500, "POST /auth/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /chat/conversations/user/{userId}"], "isController": false}, {"data": [0.9993333333333333, 500, 1500, "GET /users/{id}/profile"], "isController": false}, {"data": [1.0, 500, 1500, "GET /auth/validate-token"], "isController": false}, {"data": [0.996, 500, 1500, "GET /users/posts/suggested (Feed)"], "isController": false}, {"data": [0.9973333333333333, 500, 1500, "GET /users/groups/suggested"], "isController": false}, {"data": [0.9993333333333333, 500, 1500, "POST /users/posts/{id}/like"], "isController": false}, {"data": [0.9966666666666667, 500, 1500, "GET /users/groups/joined"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 11252, 0, 0.0, 135.17587984358337, 59, 2384, 107.0, 225.0, 288.0, 470.46999999999935, 89.36116140919343, 193.65364825310127, 45.49690337953477], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /users/admin/reports/overview", 750, 0, 0.0, 138.32666666666643, 83, 663, 115.0, 204.0, 243.0, 528.96, 6.401065137238836, 7.674835425948211, 3.1630263275809094], "isController": false}, {"data": ["GET /users/admin/sentiment/overview", 750, 0, 0.0, 190.38266666666655, 110, 752, 163.5, 279.9, 346.89999999999986, 574.7400000000002, 6.4000819210485895, 7.8264001779222765, 3.1750406405201983], "isController": false}, {"data": ["GET /notifications/unread-count", 750, 0, 0.0, 79.90933333333335, 61, 341, 76.0, 88.89999999999998, 94.0, 166.0700000000004, 6.402157954041042, 6.941106282224196, 3.2073310843975142], "isController": false}, {"data": ["POST /users/posts (Create Post)", 750, 0, 0.0, 126.39866666666681, 79, 593, 109.0, 193.19999999999982, 231.44999999999993, 361.4100000000001, 6.387980376124284, 9.808244806571954, 3.8839419747802535], "isController": false}, {"data": ["GET /users/search", 750, 0, 0.0, 103.83733333333336, 65, 477, 90.0, 138.89999999999998, 181.3499999999998, 342.4100000000001, 6.3876539424600125, 12.072932103497028, 3.0815439917727017], "isController": false}, {"data": ["GET /users/friends/suggestions", 750, 0, 0.0, 284.766666666667, 154, 1082, 249.0, 435.79999999999995, 518.6999999999996, 737.45, 6.386457304404101, 12.662315803927244, 3.19322865220205], "isController": false}, {"data": ["GET /users/posts/{postId}/comments (List Comments)", 750, 0, 0.0, 104.8359999999999, 66, 784, 89.0, 144.0, 184.89999999999986, 381.49, 6.3905930470347645, 9.498509860152522, 3.191402255879346], "isController": false}, {"data": ["POST /users/posts/{postId}/comments (Create Comment)", 750, 0, 0.0, 133.61600000000013, 82, 535, 118.0, 189.89999999999998, 227.0, 359.98, 6.389286444489879, 9.31890738942275, 3.6942804305740133], "isController": false}, {"data": ["GET /chat/conversations/online", 750, 0, 0.0, 80.12400000000008, 59, 270, 78.0, 91.0, 98.0, 127.90000000000009, 6.402212604676176, 6.8787106157221265, 3.144836855617301], "isController": false}, {"data": ["POST /auth/login", 1, 0, 0.0, 2384.0, 2384, 2384, 2384.0, 2384.0, 2384.0, 2384.0, 0.41946308724832215, 0.6795793571728188, 0.1106006187080537], "isController": false}, {"data": ["GET /chat/conversations/user/{userId}", 750, 0, 0.0, 82.22133333333336, 63, 262, 80.0, 93.89999999999998, 101.0, 129.49, 6.391900184086725, 35.499797989321266, 3.139771281831663], "isController": false}, {"data": ["GET /users/{id}/profile", 750, 0, 0.0, 113.44533333333331, 66, 534, 91.0, 197.89999999999998, 236.44999999999993, 351.74000000000024, 6.379069846562106, 10.312829585275406, 3.0711732757374204], "isController": false}, {"data": ["GET /auth/validate-token", 1, 0, 0.0, 80.0, 80, 80, 80.0, 80.0, 80.0, 80.0, 12.5, 13.97705078125, 8.3984375], "isController": false}, {"data": ["GET /users/posts/suggested (Feed)", 750, 0, 0.0, 176.32266666666672, 99, 806, 149.0, 275.9, 330.0, 463.35000000000014, 6.386131026378979, 36.17256782869416, 3.218011337511282], "isController": false}, {"data": ["GET /users/groups/suggested", 750, 0, 0.0, 130.60133333333349, 80, 738, 112.0, 187.89999999999998, 229.79999999999973, 357.82000000000016, 6.39075640992868, 17.109419602154112, 3.176655285794627], "isController": false}, {"data": ["POST /users/posts/{id}/like", 750, 0, 0.0, 132.58266666666674, 82, 550, 114.0, 195.0, 231.44999999999993, 400.47, 6.38890545271784, 6.82050596404324, 3.3153427967220654], "isController": false}, {"data": ["GET /users/groups/joined", 750, 0, 0.0, 147.3426666666668, 85, 701, 125.5, 224.89999999999998, 269.7999999999997, 428.84000000000015, 6.3902118994265855, 19.1416966917873, 3.1576633018650897], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 11252, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
