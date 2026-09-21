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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "02 POST /login-0"], "isController": false}, {"data": [1.0, 500, 1500, "03 GET /panel (tablero)"], "isController": false}, {"data": [1.0, 500, 1500, "05 POST /accesos/entrada"], "isController": false}, {"data": [1.0, 500, 1500, "02 POST /login"], "isController": false}, {"data": [1.0, 500, 1500, "08 GET /personas (listado)"], "isController": false}, {"data": [1.0, 500, 1500, "05 POST /accesos/entrada-1"], "isController": false}, {"data": [1.0, 500, 1500, "05 POST /accesos/entrada-0"], "isController": false}, {"data": [1.0, 500, 1500, "07 POST /accesos/salida-0"], "isController": false}, {"data": [1.0, 500, 1500, "06 GET /accesos (refresca token)"], "isController": false}, {"data": [1.0, 500, 1500, "07 POST /accesos/salida-1"], "isController": false}, {"data": [1.0, 500, 1500, "07 POST /accesos/salida"], "isController": false}, {"data": [1.0, 500, 1500, "04 GET /accesos (recepción)"], "isController": false}, {"data": [1.0, 500, 1500, "02 POST /login-1"], "isController": false}, {"data": [1.0, 500, 1500, "01 GET /login"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 28736, 0, 0.0, 27.400090478842042, 1, 166, 28.0, 66.0, 83.0, 96.0, 96.0999525118553, 7182.431944937371, 27.64407476184863], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["02 POST /login-0", 50, 0, 0.0, 72.7, 64, 116, 71.5, 81.9, 92.99999999999991, 116.0, 0.8536064874093043, 0.3376080345710627, 0.3367744344857021], "isController": false}, {"data": ["03 GET /panel (tablero)", 2875, 0, 0.0, 6.153739130434792, 3, 78, 5.0, 7.0, 11.199999999999818, 25.23999999999978, 9.665912445324555, 73.33012300714101, 1.6424499662953835], "isController": false}, {"data": ["05 POST /accesos/entrada", 2860, 0, 0.0, 32.955594405594425, 14, 114, 32.0, 40.0, 45.0, 72.0, 9.665884834023913, 727.5403100771919, 5.295470109265055], "isController": false}, {"data": ["02 POST /login", 50, 0, 0.0, 78.32, 68, 124, 76.5, 87.9, 97.99999999999991, 124.0, 0.8535336292249915, 6.8121485842010925, 0.48177972430863775], "isController": false}, {"data": ["08 GET /personas (listado)", 2835, 0, 0.0, 82.63915343915343, 57, 166, 82.0, 96.0, 100.0, 111.0, 9.667354350309457, 2920.5509836522397, 1.6710173046921621], "isController": false}, {"data": ["05 POST /accesos/entrada-1", 2860, 0, 0.0, 26.49615384615384, 6, 71, 27.0, 34.0, 36.0, 44.0, 9.667976012602173, 724.3271370748288, 1.6616833771659985], "isController": false}, {"data": ["05 POST /accesos/entrada-0", 2860, 0, 0.0, 6.412937062937065, 4, 84, 5.0, 9.0, 14.0, 41.0, 9.666930310220582, 3.3702091022936993, 3.634539227963793], "isController": false}, {"data": ["07 POST /accesos/salida-0", 2842, 0, 0.0, 5.834271639690366, 3, 83, 4.0, 8.0, 12.0, 41.570000000000164, 9.664497085688247, 3.369168188433889, 3.624186407133093], "isController": false}, {"data": ["06 GET /accesos (refresca token)", 2855, 0, 0.0, 27.21751313485112, 7, 108, 27.0, 34.0, 37.0, 56.0, 9.680164647564023, 711.5520264877955, 1.6637782988000664], "isController": false}, {"data": ["07 POST /accesos/salida-1", 2842, 0, 0.0, 26.545038705137266, 6, 68, 27.0, 34.0, 36.0, 45.0, 9.664267035736025, 712.4102987661653, 1.6610458967671293], "isController": false}, {"data": ["07 POST /accesos/salida", 2842, 0, 0.0, 32.428219563687556, 12, 117, 32.0, 39.0, 43.0, 70.57000000000016, 9.663445518160612, 715.7185414438809, 5.2846967677440855], "isController": false}, {"data": ["04 GET /accesos (recepción)", 2865, 0, 0.0, 26.91937172774872, 6, 103, 27.0, 34.0, 37.0, 54.340000000000146, 9.657194862979068, 696.9676692537162, 1.6598303670745271], "isController": false}, {"data": ["02 POST /login-1", 50, 0, 0.0, 5.5, 4, 9, 5.0, 7.899999999999999, 9.0, 9.0, 0.854642417612471, 6.482980196866881, 0.1452224420552441], "isController": false}, {"data": ["01 GET /login", 50, 0, 0.0, 3.6599999999999993, 1, 30, 2.0, 6.0, 12.249999999999979, 30.0, 0.8531404098486529, 8.18065008232805, 0.10081053671063184], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 28736, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
