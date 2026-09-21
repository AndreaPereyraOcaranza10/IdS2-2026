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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.7241564670856765, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "02 POST /login-0"], "isController": false}, {"data": [0.8351633269283557, 500, 1500, "03 GET /panel (tablero)"], "isController": false}, {"data": [0.5648897571867151, 500, 1500, "05 POST /accesos/entrada"], "isController": false}, {"data": [1.0, 500, 1500, "02 POST /login"], "isController": false}, {"data": [0.8066106117715279, 500, 1500, "08 GET /personas (listado)"], "isController": false}, {"data": [0.7115545632151828, 500, 1500, "05 POST /accesos/entrada-1"], "isController": false}, {"data": [0.8174713926876919, 500, 1500, "05 POST /accesos/entrada-0"], "isController": false}, {"data": [0.8176756139348943, 500, 1500, "07 POST /accesos/salida-0"], "isController": false}, {"data": [0.6620396600566573, 500, 1500, "06 GET /accesos (refresca token)"], "isController": false}, {"data": [0.7065962307252999, 500, 1500, "07 POST /accesos/salida-1"], "isController": false}, {"data": [0.5726727584237579, 500, 1500, "07 POST /accesos/salida"], "isController": false}, {"data": [0.684078910808558, 500, 1500, "04 GET /accesos (recepción)"], "isController": false}, {"data": [1.0, 500, 1500, "02 POST /login-1"], "isController": false}, {"data": [1.0, 500, 1500, "01 GET /login"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 36276, 0, 0.0, 679.2989304223187, 1, 8055, 824.0, 2562.9000000000015, 3224.0, 4971.0, 201.03298457173258, 22051.827117005312, 57.8899082509227], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["02 POST /login-0", 200, 0, 0.0, 77.10499999999995, 64, 108, 76.0, 83.9, 91.84999999999997, 101.0, 6.654688227856525, 2.6319811838690357, 2.62548246489652], "isController": false}, {"data": ["03 GET /panel (tablero)", 3643, 0, 0.0, 424.8331045841339, 4, 4962, 206.0, 1206.1999999999994, 1756.3999999999978, 3160.24, 20.424754151669077, 154.98122213356825, 3.470612521865644], "isController": false}, {"data": ["05 POST /accesos/entrada", 3583, 0, 0.0, 1102.7289980463313, 29, 8055, 737.0, 2784.0, 3507.599999999998, 5245.399999999994, 20.1892140124302, 2790.7810466217015, 11.06069244235678], "isController": false}, {"data": ["02 POST /login", 200, 0, 0.0, 84.895, 69, 125, 83.0, 97.0, 106.84999999999997, 123.93000000000006, 6.652917304237909, 53.112889611469626, 3.755259962743663], "isController": false}, {"data": ["08 GET /personas (listado)", 3449, 0, 0.0, 517.4868077703679, 61, 5524, 325.0, 1123.0, 1661.5, 3557.0, 19.759268064920853, 5969.363644749601, 3.4154203588779213], "isController": false}, {"data": ["05 POST /accesos/entrada-1", 3583, 0, 0.0, 657.6000558191453, 24, 6729, 448.0, 1596.1999999999998, 2249.799999999997, 3800.399999999998, 20.194789823133544, 2784.5112317402154, 3.470979500851078], "isController": false}, {"data": ["05 POST /accesos/entrada-0", 3583, 0, 0.0, 445.05191180574974, 3, 4975, 224.0, 1225.3999999999996, 1765.199999999999, 3240.0399999999972, 20.22374243655739, 7.050660204932606, 7.603653162182222], "isController": false}, {"data": ["07 POST /accesos/salida-0", 3502, 0, 0.0, 445.7692747001715, 3, 4991, 231.0, 1157.2000000000016, 1764.0999999999995, 3193.879999999999, 19.90394725624485, 6.9391691117963, 7.463980221091819], "isController": false}, {"data": ["06 GET /accesos (refresca token)", 3530, 0, 0.0, 839.0790368271943, 23, 7070, 496.5, 2243.7000000000003, 2939.4999999999945, 5032.0, 19.972728455762955, 2723.3223409802335, 3.4328127033342573], "isController": false}, {"data": ["07 POST /accesos/salida-1", 3502, 0, 0.0, 653.3986293546544, 25, 5984, 463.0, 1581.800000000001, 2190.5499999999997, 3866.0399999999936, 19.89264108608594, 2698.849021423855, 3.4190476866710218], "isController": false}, {"data": ["07 POST /accesos/salida", 3502, 0, 0.0, 1099.242147344375, 30, 7923, 758.0, 2749.100000000001, 3865.85, 5027.969999999999, 19.89196312432193, 2705.692032910563, 10.878417333613555], "isController": false}, {"data": ["04 GET /accesos (recepción)", 3599, 0, 0.0, 753.7357599333152, 23, 6862, 463.0, 1956.0, 2780.0, 4982.0, 20.235812721742114, 2698.453870392613, 3.4780303115494253], "isController": false}, {"data": ["02 POST /login-1", 200, 0, 0.0, 7.66, 4, 42, 6.5, 9.800000000000011, 17.94999999999999, 38.91000000000008, 6.6771274997496075, 50.66531315727974, 1.1345900243715155], "isController": false}, {"data": ["01 GET /login", 200, 0, 0.0, 2.4050000000000007, 1, 22, 2.0, 3.0, 3.0, 5.990000000000009, 6.715465717547512, 64.39370886777247, 0.7935267107648916], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 36276, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
