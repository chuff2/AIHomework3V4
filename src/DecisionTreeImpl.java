import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
  private DecTreeNode root;
  //ordered list of class labels
  private List<String> labels; 
  //ordered list of attributes
  private List<String> attributes; 
  //map to ordered discrete values taken by attributes
  private Map<String, List<String>> attributeValues; 
  
  /**
   * Answers static questions about decision trees.
   */
  DecisionTreeImpl() {
    // no code necessary this is void purposefully

    //Phteven
  }

  /**
   * Build a decision tree given only a training set.
   * 
   * @param train: the training set
   */
  DecisionTreeImpl(DataSet train) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
    
    //helper function
    DecisionTreeImplHelper(train.instances, train.attributes, null);
  }

  /**
   * Build a decision tree given a training set then prune it using a tuning set.
   * 
   * @param train: the training set
   * @param tune: the tuning set
   */
  DecisionTreeImpl(DataSet train, DataSet tune) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
  }

  @Override
  public String classify(Instance instance) {

    // TODO: add code here
    return null;
  }

  @Override
  public void rootInfoGain(DataSet train) {
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here

  }
  
  @Override
  /**
   * Print the decision tree in the specified format
   */
  public void print() {

    printTreeNode(root, null, 0);
  }

  /**
   * Prints the subtree of the node with each line prefixed by 4 * k spaces.
   */
  public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < k; i++) {
      sb.append("    ");
    }
    String value;
    if (parent == null) {
      value = "ROOT";
    } else {
      int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
      value = attributeValues.get(parent.attribute).get(attributeValueIndex);
    }
    sb.append(value);
    if (p.terminal) {
      sb.append(" (" + p.label + ")");
      System.out.println(sb.toString());
    } else {
      sb.append(" {" + p.attribute + "?}");
      System.out.println(sb.toString());
      for (DecTreeNode child : p.children) {
        printTreeNode(child, p, k + 1);
      }
    }
  }

  /**
   * Helper function to get the index of the label in labels list
   */
  private int getLabelIndex(String label) {
    for (int i = 0; i < this.labels.size(); i++) {
      if (label.equals(this.labels.get(i))) {
        return i;
      }
    }
    return -1;
  }
 
  /**
   * Helper function to get the index of the attribute in attributes list
   */
  private int getAttributeIndex(String attr) {
    for (int i = 0; i < this.attributes.size(); i++) {
      if (attr.equals(this.attributes.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
   */
  private int getAttributeValueIndex(String attr, String value) {
    for (int i = 0; i < attributeValues.get(attr).size(); i++) {
      if (value.equals(attributeValues.get(attr).get(i))) {
        return i;
      }
    }
    return -1;
  }
  
  
  
  
  
  /********************************************************************
  //our own methods area
  ********************************************************************/
  private DecTreeNodeImpl DecisionTreeImplHelper(List<Instance> instances, 
		  	List<String> attributes, List<Instance> parentInstances){

  	boolean allTheSame = true;
  	String label;
  	DecTreeNodeImpl node = null;

	//if list of examples is empty. 
    if (instances.isEmpty()){
    	return mostCommonClass(instances);
    }

    //sets allTheSame to false if it finds any labels different
    label = instances.get(0).label;
    for (Instance i : instances){
    	if (!label.equals(i.label))
    		allTheSame = false;
    }
    if (allTheSame){
    	// make new node with label class and return
    	node = new DecTreeNodeImpl(label, null, null, true);
    	return node;
    }

    //return most common from parent if no more attributes to split on
    if (attributes.isEmpty()){
    	return mostCommonClass(parentInstances);
    }

    ///////////////////// SUDO

    // find best attribute to split on TODO make helper fcn
    String bestAttr = getBestAttribute(attributes);

    // make a new node N with this attribute
    node = new DecTreeNodeImpl(null, bestAttr, null, false);

    // for each possible value of the attribute:
    List<String> attrVals = this.attributeValues.get(bestAttr);
    	// make list of instances with that value
    	// new node = DecTreeNodeImplHelper(.....) (recursive call)
    	// make new node a child of node N

    //return node

	  return node;
  }
  
  private DecTreeNodeImpl mostCommonClass(List<Instance> instances){
	  int commonLabelIndex = 0;
	  int highestCount = 0;
	  int currCount;
	  DecTreeNodeImpl node;

	  // count number for each label and update vars to find most common
	  for (String singleLabel : this.labels) {
	  	  currCount = 0;

	  	  // count instances with this label
		  for (Instance i : instances){
		  	if (i.label.equals(singleLabel))
		  		currCount++;
		  }
		  // check if new max found TODO check how we break ties
		  if (currCount > highestCount){
		  	highestCount = currCount;
		  	commonLabelIndex = getLabelIndex(singleLabel);
		  }

	  }

	  //TODO check what null params should be: String _label, String _attribute, String _parentAttributeValue, boolean _terminal
	  node = new DecTreeNodeImpl(this.labels.get(commonLabelIndex), null, null, true);

	  return node;
  }
  
  
  private String allInstancesHaveSameClassification(DataSet train){
	//run through all the instances and check if they all have the same classification
    //used for the first else if clause below
    boolean allTheSame = true;
    boolean firstLabelCheck = true;
    String prevLabel = "";
    for (Instance singleInstance : train.instances){
    	//if we are on the first instance, record its label, and continue to the next iteration
    	if (firstLabelCheck){
    		prevLabel = singleInstance.label;
    		firstLabelCheck = false;
    		continue;
    	}
    	else if (!singleInstance.label.equals(prevLabel)){
    		allTheSame = false;
    		break;
    	}	
    }
	  return null;
  }
  
  private String getBestAttribute(List<String> attributes){
  	// TODO figure out H() fcn

  	return null;	
  }
  
}
