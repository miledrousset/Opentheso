/*
 <script type="text/javascript">
 //<![CDATA[
 function srollToSelected() {
 console.log("\n\n DEBUG srollToSelected -----");
 var treeWidgetVar = PrimeFaces.widgets["treeWidgetVar"];
 console.log("treeWidgetVar : ",treeWidgetVar);
 var selectedElement = treeWidgetVar.jq.find('span .ui-state-highlight');
 if (selectedElement != null && selectedElement != undefined && selectedElement.position() != undefined) {
 var scrollPanel = document.getElementById("divArbreTheso");
 //var height = treeWidgetVar.jq.parent().height();
 console.log("selectedElement : ",selectedElement);
 console.log("scrollPanel : ",scrollPanel);
 scrollPanel.scrollTop =selectedElement.position().top;//test
 console.log("go to : ", selectedElement.position().top);
 //console.log("scrollPanel.height : ",height);
 
 }
 
 }
 //]]>
 </script> 
 */

function srollToSelected() {
    var treeWidgetVar = PrimeFaces.widgets["treeWidget"];
  /*  console.log(treeWidgetVar.toLocaleString());*/
    var selectedElement = treeWidgetVar.jq.find('.ui-state-highlight');
/*    console.log("VAleur de : " + selectedElement.toString());
    console.log("Valeur de position : " + selectedElement.position());
    var scrollPanel = document.getElementById("containerIndex:formLeftTab:tabTree:tree");
    console.log(scrollPanel);
    scrollPanel.scrollTop = 800;*/
    if (selectedElement !== null && selectedElement !== undefined && selectedElement.position() !== undefined) {
      /*  var scrollPanel = document.getElementById("formLeftTab:tabTree:tree");*/
       // console.log("je passe");
        var scrollPanel = document.getElementById("containerIndex:formLeftTab:tabTree:tree");
        
        scrollPanel.scrollTop = selectedElement.position().top;

    }

}

function srollGroupToSelected() {
    var treeGroupWidgetVar = PrimeFaces.widgets["groupWidget"];
  /*  console.log(treeWidgetVar.toLocaleString());*/
    var selectedElement = treeGroupWidgetVar.jq.find('.ui-state-highlight');
/*    console.log("VAleur de : " + selectedElement.toString());
    console.log("Valeur de position : " + selectedElement.position());
    var scrollPanel = document.getElementById("containerIndex:formLeftTab:tabTree:tree");
    console.log(scrollPanel);
    scrollPanel.scrollTop = 800;*/
    if (selectedElement !== null && selectedElement !== undefined && selectedElement.position() !== undefined) {
      /*  var scrollPanel = document.getElementById("formLeftTab:tabTree:tree");*/
       // console.log("je passe");
        var scrollPanel = document.getElementById("containerIndex:formLeftTab:tabTree:treeGroups");
        
        scrollPanel.scrollTop = selectedElement.position().top;

    }

}