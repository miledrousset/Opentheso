package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class MyTreeNode extends DefaultTreeNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private int typeConcept;
    private String idConcept;
    private String idTheso;
    private String langue;
    private String idCurrentGroup;
    private String typeDomaine;
    private String idTopConcept;
    private String idParent;//déjà dans la super classe
    private String prefix;//pour la numérotation
    private String suffix;//pour la numérotation
    private boolean isGroup =false;
    private boolean isSubGroup = false;
    private boolean isTopConcept = false;
    
    private ArrayList<String> otherGroup;

    
    public MyTreeNode(int type, String id, String idT, String l, String idD, String typeDomaine, 
            String idTC, String icone, Object value, TreeNode parent)
   {
      super(icone, value, parent);
      this.typeConcept = type;
      this.idConcept = id;
      this.idTheso = idT;
      this.langue = l;
      this.idCurrentGroup = idD;
      this.typeDomaine = typeDomaine; 
      this.idTopConcept = idTC;
      
   }

    public ArrayList<String> getOtherGroup() {
        return otherGroup;
    }

    public void setOtherGroup(ArrayList<String> otherGroup) {
        this.otherGroup = otherGroup;
    }
    

    public int getTypeConcept() {
        return typeConcept;
    }

    public void setTypeConcept(int typeConcept) {
        this.typeConcept = typeConcept;
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdTheso() {
        return idTheso;
    }

    public void setIdTheso(String idTheso) {
        this.idTheso = idTheso;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getIdCurrentGroup() {
        return idCurrentGroup;
    }

    public void setIdCurrentGroup(String idCurrentGroup) {
        this.idCurrentGroup = idCurrentGroup;
    }

    public String getIdTopConcept() {
        return idTopConcept;
    }

    public void setIdTopConcept(String idTopConcept) {
        this.idTopConcept = idTopConcept;
    }    

    public String getTypeDomaine() {
        return typeDomaine;
    }

    public void setTypeDomaine(String typeDomaine) {
        this.typeDomaine = typeDomaine;
    }

    public boolean isIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public boolean isIsSubGroup() {
        return isSubGroup;
    }

    public void setIsSubGroup(boolean isSubGroup) {
        this.isSubGroup = isSubGroup;
    }

    public boolean isIsTopConcept() {
        return isTopConcept;
    }

    public void setIsTopConcept(boolean isTopConcept) {
        this.isTopConcept = isTopConcept;
    }

    public String getIdParent() {
        return idParent;
    }

    public void setIdParent(String idParent) {
        this.idParent = idParent;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isIsOrphan(){
        if(idCurrentGroup.equalsIgnoreCase("orphans")){
            return true;
        }
        return false;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getNumerotation(){
        if(this.isGroup){
            return this.prefix;
        }
        else if(!(this.suffix==null || this.suffix.isEmpty())){
            return this.prefix+"."+this.suffix;
        }
        return "";
    }

    public void setSuperParent(TreeNode treeNode) {
        super.setParent(treeNode);
    }
    
}
