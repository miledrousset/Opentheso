
var margin = {top: 20, right: 20, bottom: 20, left: 120},
        width = window.innerWidth - margin.right - margin.left,
        height = 800 - margin.top - margin.bottom;

var i = 0, duration = 750, root;

var tree = d3.layout.tree().size([height, width]);

var diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [d.y, d.x];
        });

var svg = d3.select("body").append("svg")
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var input = document.getElementById("url");
document.querySelector('#url').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        var inputVal = document.getElementById("url").value;

        document.getElementById("loader-annel").style.display = "block";
        document.getElementById("myDiv").style.display = "none";

        seatchDatas(inputVal);
    }
});

var tailleSelected = document.getElementById('taille');
tailleSelected.addEventListener('change', function () {
    var indexTaille = tailleSelected.selectedIndex;
    if (indexTaille === 0) {
        console.log("Taille normal");
        this.width = window.innerWidth - margin.right - margin.left;
        this.height = 800 - margin.top - margin.bottom;
    } else if (indexTaille === 1) {
        console.log("Taille moyenne");
        this.width  = 1400 - margin.right - margin.left;
        this.height = 1400 - margin.top - margin.bottom;
    } else if (indexTaille === 2) {
        console.log("Taille GRANDE");
        this.width  = 2400 - margin.right - margin.left;
        this.height = 2000 - margin.top - margin.bottom;
    } else {
        this.width  = 4000 - margin.right - margin.left;
        this.height = 6000 - margin.top - margin.bottom;
    }

    tree = d3.layout.tree().size([this.height, this.width]);

    diagonal = d3.svg.diagonal()
            .projection(function (d) {
                return [d.y, d.x];
            });

    //Taille de la plaque
    d3.select("svg").remove();
    svg = d3.select("body")
            .append("svg")
            .attr("width", this.width + margin.right + margin.left)
            .attr("height", this.height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    update(root);
    expandAll();
})