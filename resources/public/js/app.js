
$( document ).ready(function() {
    console.log( "ready" );
    $.getJSON( "./locations", function (data) {
       $('#stateSelector').append($('<option>').text("Choose a State"));
       $.each(data, function(i, item){
               $('#stateSelector').append($('<option>').text(item.state).attr('value', item.id));
       });
       //loading with no data to initialize the display
       loadChart(0);
    });
});

var loadCounties = function (stateId) {
    $.getJSON( "./locations/" + stateId, function (data) {
       $('#countySelector').append($('<option>').text("Choose a County"));
       $.each(data.counties, function(i, item){
               $('#countySelector').append($('<option>').text(item.county).attr('value', item.id));
       });
})};

$("#stateSelector").change(function () {
        var selectedValue = $(this).val();
        loadChart(selectedValue);
        $('#countySelector').empty();
        loadCounties(selectedValue);
});

$("#countySelector").change(function () {
        var selectedValue = $(this).val();
        loadChart(selectedValue);
});

var chartData = function (locationData) {

        var all0to5 = locationData.total_pop * (locationData.lt5years_pct / 100);
        var female0to5 = all0to5 * (locationData.female_pct / 100);
        var male0to5 = -(all0to5 * (locationData.male_pct / 100));

        var all6to18 = locationData.total_pop * (locationData.lt18years_pct / 100);
        var female6to18 = all6to18 * (locationData.female_pct / 100);
        var male6to18 = -(all6to18 * (locationData.male_pct / 100));

        var all19to64 = locationData.total_pop * (locationData.lt64years_pct / 100);
        var female19to64 = all19to64 * (locationData.female_pct / 100);
        var male19to64 = -(all19to64 * (locationData.male_pct / 100));

        var all65 = locationData.total_pop * (locationData.gt65years_pct / 100);
        var female65 = all65 * (locationData.female_pct / 100);
        var male65 = -(all65 * (locationData.male_pct / 100));

        var title = String("Population pyramid:  " + (locationData.state || ""));


return {
    chart: {
                type: 'bar'
    },
    title: {
        text: title
    },
    subtitle: {
        text: 'Source: <a href="http://www.census.gov/quickfacts/">U.S. Census Data from 2010</a><br/>Total Population is ' + (locationData.total_pop || "0")
    },
    xAxis: [{
        categories: ['0-5', '6-18', '19-64', '65+'],
        reversed: false,
        labels: {
            step: 1
        }
    }, { // mirror axis on right side
        opposite: true,
        reversed: false,
        categories: ['0-5', '6-18', '19-64', '65+'],
        linkedTo: 0,
        labels: {
            step: 1
        }
    }],
    yAxis: {
        title: {
            text: null
        },
        labels: {
            formatter: function () {
                return Math.abs(this.value); // + '%';
            }
        }
    },

    plotOptions: {
        series: {
            stacking: 'normal'
        }
    },

    tooltip: {
        formatter: function () {
            return '<b>' + this.series.name + ', age ' + this.point.category + '</b><br/>' +
                'Population: ' + Highcharts.numberFormat(Math.abs(this.point.y), 0);
        }
    },

    series: [{
        name: 'Male',
        data: [male0to5, male6to18, male19to64, male65]
    }, {
        name: 'Female',
        data: [female0to5, female6to18, female19to64, female65]
    }]
  }
};


var loadChart = function (id) {
    $.getJSON( "./locations/" + id, function (locationData) {
    Highcharts.chart('graph-container', chartData(locationData))
})};
