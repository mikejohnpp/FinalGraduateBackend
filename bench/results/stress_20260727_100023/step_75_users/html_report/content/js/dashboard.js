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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9821650767316467, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9826666666666667, 500, 1500, "GET /users/admin/reports/overview"], "isController": false}, {"data": [0.9822222222222222, 500, 1500, "GET /users/admin/sentiment/overview"], "isController": false}, {"data": [0.9991111111111111, 500, 1500, "GET /notifications/unread-count"], "isController": false}, {"data": [0.9831111111111112, 500, 1500, "POST /users/posts (Create Post)"], "isController": false}, {"data": [0.992, 500, 1500, "GET /users/search"], "isController": false}, {"data": [0.9053333333333333, 500, 1500, "GET /users/friends/suggestions"], "isController": false}, {"data": [0.9897777777777778, 500, 1500, "GET /users/posts/{postId}/comments (List Comments)"], "isController": false}, {"data": [0.9764444444444444, 500, 1500, "POST /users/posts/{postId}/comments (Create Comment)"], "isController": false}, {"data": [0.9977777777777778, 500, 1500, "GET /chat/conversations/online"], "isController": false}, {"data": [0.5, 500, 1500, "POST /auth/login"], "isController": false}, {"data": [0.9955555555555555, 500, 1500, "GET /chat/conversations/user/{userId}"], "isController": false}, {"data": [0.9893333333333333, 500, 1500, "GET /users/{id}/profile"], "isController": false}, {"data": [1.0, 500, 1500, "GET /auth/validate-token"], "isController": false}, {"data": [0.9777777777777777, 500, 1500, "GET /users/posts/suggested (Feed)"], "isController": false}, {"data": [0.9911111111111112, 500, 1500, "GET /users/groups/suggested"], "isController": false}, {"data": [0.9813333333333333, 500, 1500, "POST /users/posts/{id}/like"], "isController": false}, {"data": [0.9893333333333333, 500, 1500, "GET /users/groups/joined"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 16877, 0, 0.0, 192.21218225988036, 61, 1292, 147.0, 370.2000000000007, 458.0, 651.0, 114.94871340807236, 249.27883551170805, 58.53872349469085], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /users/admin/reports/overview", 1125, 0, 0.0, 204.58933333333314, 84, 871, 165.0, 371.19999999999993, 456.0, 682.7200000000003, 8.033820599430134, 9.644552812997652, 3.9698371321402814], "isController": false}, {"data": ["GET /users/admin/sentiment/overview", 1125, 0, 0.0, 260.1066666666659, 111, 745, 237.0, 400.0, 466.0, 589.44, 8.032042494859493, 9.823634195796208, 3.9846460814342017], "isController": false}, {"data": ["GET /notifications/unread-count", 1125, 0, 0.0, 96.896, 61, 669, 79.0, 116.39999999999998, 225.4000000000001, 423.48, 8.035140347118062, 8.712184620741375, 4.025416990304264], "isController": false}, {"data": ["POST /users/posts (Create Post)", 1125, 0, 0.0, 185.05955555555556, 79, 1200, 139.0, 328.0, 417.70000000000005, 657.96, 8.033017486986513, 12.338561451691218, 4.884102524509629], "isController": false}, {"data": ["GET /users/search", 1125, 0, 0.0, 164.10133333333312, 66, 1038, 118.0, 314.0, 387.8000000000002, 520.0, 8.03307484683604, 15.183801494241179, 3.8753310296259804], "isController": false}, {"data": ["GET /users/friends/suggestions", 1125, 0, 0.0, 384.8017777777778, 148, 1292, 369.0, 577.0, 660.7, 919.5000000000002, 8.029921271083003, 15.92011354754784, 4.014960635541502], "isController": false}, {"data": ["GET /users/posts/{postId}/comments (List Comments)", 1125, 0, 0.0, 161.4204444444446, 66, 885, 118.0, 310.0, 371.70000000000005, 611.9200000000001, 8.034336970805006, 12.036817683932755, 4.017168485402502], "isController": false}, {"data": ["POST /users/posts/{postId}/comments (Create Comment)", 1125, 0, 0.0, 204.42133333333325, 82, 1038, 156.0, 353.0, 494.0, 704.8800000000001, 8.033591122346238, 11.727815684426258, 4.649956908709841], "isController": false}, {"data": ["GET /chat/conversations/online", 1125, 0, 0.0, 96.39288888888883, 61, 717, 79.0, 117.39999999999998, 207.70000000000005, 448.0, 8.035140347118062, 8.63513934272552, 3.9469488228519394], "isController": false}, {"data": ["POST /auth/login", 1, 0, 0.0, 609.0, 609, 609, 609.0, 609.0, 609.0, 609.0, 1.6420361247947455, 2.647462541050903, 0.43295874384236455], "isController": false}, {"data": ["GET /chat/conversations/user/{userId}", 1125, 0, 0.0, 101.720888888889, 62, 614, 81.0, 134.0, 240.4000000000001, 459.2800000000002, 8.034968181526, 44.62877303134351, 3.946864253230057], "isController": false}, {"data": ["GET /users/{id}/profile", 1125, 0, 0.0, 173.74222222222247, 63, 1113, 141.0, 304.0, 384.10000000000014, 602.7, 8.023564316891564, 12.96937433359841, 3.8629074299097086], "isController": false}, {"data": ["GET /auth/validate-token", 1, 0, 0.0, 76.0, 76, 76, 76.0, 76.0, 76.0, 76.0, 13.157894736842104, 14.635587993421053, 8.84046052631579], "isController": false}, {"data": ["GET /users/posts/suggested (Feed)", 1125, 0, 0.0, 248.71644444444453, 98, 952, 220.0, 405.0, 489.4000000000001, 702.3200000000002, 8.031526418367566, 45.543572313632886, 4.047136359255531], "isController": false}, {"data": ["GET /users/groups/suggested", 1125, 0, 0.0, 195.49688888888886, 79, 718, 158.0, 341.4, 418.70000000000005, 556.2800000000002, 8.033877970749543, 21.507256209741342, 3.9934022335073407], "isController": false}, {"data": ["POST /users/posts/{id}/like", 1125, 0, 0.0, 194.7377777777777, 80, 969, 153.0, 339.79999999999995, 439.4000000000001, 662.48, 8.03353375512361, 8.576041357970693, 4.173671833716562], "isController": false}, {"data": ["GET /users/groups/joined", 1125, 0, 0.0, 210.7119999999998, 85, 1045, 181.0, 356.4, 432.0, 555.7, 8.033763228930118, 24.064335770687833, 3.969808783045546], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 16877, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
