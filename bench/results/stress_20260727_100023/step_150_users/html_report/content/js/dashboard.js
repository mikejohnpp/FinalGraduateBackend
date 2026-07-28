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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.7458224697795686, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.6862222222222222, 500, 1500, "GET /users/admin/reports/overview"], "isController": false}, {"data": [0.6475555555555556, 500, 1500, "GET /users/admin/sentiment/overview"], "isController": false}, {"data": [1.0, 500, 1500, "GET /notifications/unread-count"], "isController": false}, {"data": [0.7002222222222222, 500, 1500, "POST /users/posts (Create Post)"], "isController": false}, {"data": [0.7042222222222222, 500, 1500, "GET /users/search"], "isController": false}, {"data": [0.6177777777777778, 500, 1500, "GET /users/friends/suggestions"], "isController": false}, {"data": [0.708, 500, 1500, "GET /users/posts/{postId}/comments (List Comments)"], "isController": false}, {"data": [0.694, 500, 1500, "POST /users/posts/{postId}/comments (Create Comment)"], "isController": false}, {"data": [1.0, 500, 1500, "GET /chat/conversations/online"], "isController": false}, {"data": [0.0, 500, 1500, "POST /auth/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /chat/conversations/user/{userId}"], "isController": false}, {"data": [0.7026666666666667, 500, 1500, "GET /users/{id}/profile"], "isController": false}, {"data": [1.0, 500, 1500, "GET /auth/validate-token"], "isController": false}, {"data": [0.6517777777777778, 500, 1500, "GET /users/posts/suggested (Feed)"], "isController": false}, {"data": [0.6893333333333334, 500, 1500, "GET /users/groups/suggested"], "isController": false}, {"data": [0.6951111111111111, 500, 1500, "POST /users/posts/{id}/like"], "isController": false}, {"data": [0.6906666666666667, 500, 1500, "GET /users/groups/joined"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 33752, 0, 0.0, 517.191159042423, 61, 3069, 540.0, 1031.0, 1266.0, 1774.950000000008, 132.31719747220524, 286.9079018455685, 67.38410266936577], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /users/admin/reports/overview", 2250, 0, 0.0, 613.2222222222222, 89, 2851, 640.0, 1030.0, 1338.5999999999985, 1814.859999999997, 9.141436302471844, 10.974048287605838, 4.517155047901126], "isController": false}, {"data": ["GET /users/admin/sentiment/overview", 2250, 0, 0.0, 710.6631111111124, 120, 2904, 752.0, 1120.9, 1397.4499999999998, 1908.2899999999954, 9.139691037822072, 11.177477186315649, 4.534143600794544], "isController": false}, {"data": ["GET /notifications/unread-count", 2250, 0, 0.0, 84.09199999999998, 61, 230, 80.0, 97.0, 111.0, 179.46999999999935, 9.142142024192138, 9.913502321596177, 4.57999888516657], "isController": false}, {"data": ["POST /users/posts (Create Post)", 2250, 0, 0.0, 580.5431111111106, 81, 2610, 620.0, 992.0000000000009, 1331.0, 1816.4699999999993, 9.14537487907782, 14.048835920796988, 5.5604593747205575], "isController": false}, {"data": ["GET /users/search", 2250, 0, 0.0, 567.9311111111117, 70, 2910, 602.0, 1015.9000000000001, 1343.0, 1787.859999999997, 9.146192744833417, 17.284732285857142, 4.412323453073934], "isController": false}, {"data": ["GET /users/friends/suggestions", 2250, 0, 0.0, 794.2648888888876, 158, 2767, 840.0, 1232.9, 1535.0, 1996.1899999999932, 9.138466037398665, 18.119325266437055, 4.569233018699332], "isController": false}, {"data": ["GET /users/posts/{postId}/comments (List Comments)", 2250, 0, 0.0, 565.3377777777772, 66, 3069, 597.0, 985.0, 1337.8999999999996, 1752.449999999999, 9.142067732564044, 13.645920891910285, 4.571033866282023], "isController": false}, {"data": ["POST /users/posts/{postId}/comments (Create Comment)", 2250, 0, 0.0, 598.4413333333329, 83, 2443, 634.0, 1007.9000000000001, 1329.249999999999, 1870.7999999999956, 9.141250604338234, 13.34689639941374, 5.291034407413351], "isController": false}, {"data": ["GET /chat/conversations/online", 2250, 0, 0.0, 84.85111111111117, 62, 298, 80.0, 99.0, 112.0, 179.46999999999935, 9.141993442143372, 9.82223869897244, 4.490647169334097], "isController": false}, {"data": ["POST /auth/login", 1, 0, 0.0, 2596.0, 2596, 2596, 2596.0, 2596.0, 2596.0, 2596.0, 0.3852080123266564, 0.6210726839368259, 0.1015685188751926], "isController": false}, {"data": ["GET /chat/conversations/user/{userId}", 2250, 0, 0.0, 87.62844444444447, 64, 275, 82.0, 103.0, 116.44999999999982, 196.0, 9.141844864924163, 50.77579822272378, 4.490574186578959], "isController": false}, {"data": ["GET /users/{id}/profile", 2250, 0, 0.0, 567.1462222222219, 65, 2587, 602.0, 959.9000000000001, 1305.699999999999, 1781.2099999999937, 9.140247964771453, 14.77507595292163, 4.400529537726881], "isController": false}, {"data": ["GET /auth/validate-token", 1, 0, 0.0, 77.0, 77, 77, 77.0, 77.0, 77.0, 77.0, 12.987012987012989, 14.470880681818182, 8.72564935064935], "isController": false}, {"data": ["GET /users/posts/suggested (Feed)", 2250, 0, 0.0, 703.340444444445, 111, 2845, 735.5, 1129.7000000000003, 1443.0, 2069.2899999999954, 9.144334170547928, 51.8607100727889, 4.607887140627667], "isController": false}, {"data": ["GET /users/groups/suggested", 2250, 0, 0.0, 602.5480000000016, 82, 2650, 632.0, 1032.9, 1374.4499999999998, 1792.9599999999991, 9.141102051263301, 24.47080160609163, 4.54377045321584], "isController": false}, {"data": ["POST /users/posts/{id}/like", 2250, 0, 0.0, 587.1004444444436, 84, 2520, 613.0, 980.8000000000002, 1296.8999999999996, 1729.9399999999987, 9.140879230703604, 9.759570754467859, 4.748972412826482], "isController": false}, {"data": ["GET /users/groups/joined", 2250, 0, 0.0, 610.0288888888884, 89, 2755, 646.5, 1026.9, 1380.3499999999995, 1822.2099999999937, 9.140693555201665, 27.379059102708897, 4.516788026300823], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 33752, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
