function chercher() {

    var inputVal = document.getElementById("url").value;

    document.getElementById("loader-annel").style.display = "block";
    document.getElementById("myDiv").style.display = "none";

    seatchDatas(inputVal);
}

function dataLoading(option) {
    document.getElementById("url").value = option.url;
    $(document).ready(function () {
        seatchDatas(option.url);
    });
}

function seatchDatas(url) {

    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'html',
        success: function (arrayOfObjects) {
            try {
                arrayOfObjects = JSON.parse(arrayOfObjects);

                root = arrayOfObjects;
                root.x0 = window.innerHeight / 2;
                root.y0 = 0;

                update(root);
                collapseAll();
            } catch (error) {
                alert("Erreur pendant la recherche des données...");
                root = null;
                update(root);
            }
            document.getElementById("loader-annel").style.display = "none";
            document.getElementById("myDiv").style.display = "block";
        },
        error: function (resultat, statut, erreur) {
            document.getElementById("loader-annel").style.display = "none";
            document.getElementById("myDiv").style.display = "block";
            alert("Erreur pendant la recherche des données...");
            root = null;
            update(root);
        }
    });
}

function update(source) {

    var nodes = tree.nodes(root).reverse(), links = tree.links(nodes);

    nodes.forEach(function (d) {
        d.y = d.depth * 180;
    });

    var node = svg.selectAll("g.node").data(nodes, function (d) {
        return d.id || (d.id = ++i);
    });


    var tooltip = d3.select("body")
            .append("div")
            .style("position", "absolute")
            .style("z-index", "10")
            .style("visibility", "hidden");

    var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .attr("transform", function (d) {
                return "translate(" + source.y0 + "," + source.x0 + ")";
            })
            .on("mouseover", function (d) {
                var definition = '';
                if (d.definition.length > 0 && d.definition !== '[]') {
                    definition = '<b>Definition: </b>' + d.definition;
                }
                var synonym = '';
                if (d.synonym.length > 0) {
                    synonym = '</br><b>Synonymie: </b><ul>' + d.synonym.forEach(element => '<li>' + element + '</li>') + '</ul>';
                }
                
                var tooltip_body = '';
                if (definition.length > 0 || synonym.length > 0) {
                    tooltip_body = '<div class="tooltip" >' + definition + synonym + '</div>';
                }
                
                return tooltip
                        .style("visibility", "visible")
                        .style("animation", "5s fadeIn")
                        .style("animation-fill-mode", "forwards")
                        .html(tooltip_body);
            })
            .on("mousemove", function () {
                return tooltip
                    .style("top", (d3.event.pageY - 10) + "px")
                    .style("left", (d3.event.pageX + 10) + "px");
            })
            .on("mouseout", function () {
                return tooltip.style("visibility", "hidden");
            });

    nodeEnter.append("circle")
            .attr("r", 1e-6)
            .on("click", click)
            .style("fill", function (d) {
                return d._children ? "#43B572" : "#fff";
            });

    nodeEnter.append("text")
            .attr("x", function (d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("dy", ".35em")
            .attr("text-anchor", function (d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function (d) {
                return d.name;
            })
            .on('click', function (d) {
                window.open(d.url, '_blank');
            })
            .style("fill-opacity", 1e-6);


    var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function (d) {
                return "translate(" + d.y + "," + d.x + ")";
            });

    nodeUpdate.select("circle")
            .attr("r", 4.5)
            .style("fill", function (d) {
                return d._children ? "#43B572" : "#fff";
            });

    nodeUpdate.select("text").style("fill-opacity", 1);

    var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function (d) {
                return "translate(" + source.y + "," + source.x + ")";
            })
            .remove();

    nodeExit.select("circle").attr("r", 1e-6);

    nodeExit.select("text").style("fill-opacity", 1e-6);

    var link = svg.selectAll("path.link").data(links, function (d) {
        return d.target.id;
    });

    link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function (d) {
                var o = {x: source.x0, y: source.y0};
                return diagonal({source: o, target: o});
            });

    link.transition().duration(duration).attr("d", diagonal);

    link.exit().transition()
            .duration(duration)
            .attr("d", function (d) {
                var o = {x: source.x, y: source.y};
                return diagonal({source: o, target: o});
            })
            .remove();

    nodes.forEach(function (d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}

function click(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
    update(d);
}

function mouseover(d) {
    d3.select(this)
            .append("text")
            .attr("class", "hover")
            .attr('transform', function (d) {
                return 'translate(5, -10)';
            })
            .html('Définition : ' + d.definition);

}

function mouseout(d) {
    d3.select(this).select("text.hover").remove();
}

function collapse(d) {
    if (d.children) {
        d._children = d.children;
        d._children.forEach(collapse);
        d.children = null;
    }
}

function expand(d) {
    var children = (d.children) ? d.children : d._children;
    if (d._children) {
        d.children = d._children;
        d._children = null;
    }
    if (children)
        children.forEach(expand);
}

function expandAll() {
    expand(root);
    update(root);
}

function collapseAll() {
    root.children.forEach(collapse);
    collapse(root);
    update(root);
}