import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
		this.root = DecisionTreeImplHelper(train.instances, train.attributes, null);
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
		
		if (this.root == null){
			return null;
		}
		
		//start from the root and move downwards
		DecTreeNodeImpl currNode = (DecTreeNodeImpl) this.root;
		//once we hit a terminal node, then we break out of this, and we return that terminal nodes value
		while (!currNode.isTerminal()){
			String currNodeAttr = currNode.getAttribute(); //like A1
			int attrIndex = getAttributeIndex(currNodeAttr); 
			int attrValue = Integer.parseInt(instance.attributes.get(attrIndex)) - 1; //value in instance
			currNode = (DecTreeNodeImpl) currNode.getChildren().get(attrValue);
		}
		
		return currNode.label;
	}

	@Override
	public void rootInfoGain(DataSet train) {
		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: add code here

		////////////////////////////////////////////////////////////////////////////////////
		//makes a list of maps (each corresponding to an attribute (A1, A2, etc)) and then for each
		//one of them, go through all the (global) possible values (1,2,3,4...) and zero out
		//their respective counts. I.e. A1->1 = 0
		////////////////////////////////////////////////////////////////////////////////////
		//list of attribute maps (each of A1, A2, A3, ... has its own map)
		List<HashMap<String, List<Integer>>> listOfAttrMaps = new ArrayList<HashMap<String, List<Integer>>>();
		//zeroing out the map at every possible attribute and each attribute's values
		for (String singleAttribute : this.attributes){
			HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
			//going through each value and setting its count to 0
			for (String value : this.attributeValues.get(singleAttribute)){
				//first index is the total count, while each subsequent index corresponds
				//to the individual label counts
				List<Integer> counts = new ArrayList<Integer>();
				counts.add(0, 0);
				for (int i = 1; i <= this.labels.size(); i++){
					counts.add(i, 0);
				}
				map.put(value, counts);
			}
			listOfAttrMaps.add(map);
		}

		////////////////////////////////////////////////////////////////////////////////////
		//First if-else simply counts the total number of L, B, and R's that occur. 
		//The for loop after that get the List<Integer> counts which is specific to a single
		//A1->3 combo (attribute->value). Index 0 of this list is the total count and subsequent
		//indexes correspond to the various classes (L, B, R)
		////////////////////////////////////////////////////////////////////////////////////
		//count occurences of L, B, R using this map
		HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
		//looping through all the instances
		for (Instance singleInstance : train.instances){
			//populate the count (value) for each label/class found in instances
			if (labelMap.get(singleInstance.label) == null){
				labelMap.put(singleInstance.label, 1);
			}
			else{
				labelMap.put(singleInstance.label, labelMap.get(singleInstance.label) + 1);
			}

			//increment counts for the various values in the instance 
			for (int i = 0; i < singleInstance.attributes.size(); i++){
				//get the encoded list of integer values
				List<Integer> counts = listOfAttrMaps.get(i).get(singleInstance.attributes.get(i));
				//always increment the total count
				int newTotal = counts.get(0) + 1;
				counts.remove(0);
				counts.add(0, newTotal);
				//get index of the label we have encountered
				int labelIndex = getLabelIndex(singleInstance.label) + 1;
				int newLabelCount = counts.get(labelIndex) + 1;
				counts.remove(labelIndex);
				counts.add(labelIndex, newLabelCount);
				//put the augmented count list back into the map
				listOfAttrMaps.get(i).put(singleInstance.attributes.get(i), counts);
			}

		}

		////////////////////////////////////////////////////////////////////////////////////
		//Forms the H value to be used... Loops through L,B,R and performs a calulation that is
		//added to the running total
		////////////////////////////////////////////////////////////////////////////////////
		//loop through key's (labels (L, B, R)) and start forming the H value through its formula
		double HValue = 0;
		double total = (double) train.instances.size();
		Iterator it = labelMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			HValue += -((double) (int)pair.getValue()/total)*(Math.log((double) (int)pair.getValue()/total)/Math.log(2));
		}

		////////////////////////////////////////////////////////////////////////////////////
		//
		////////////////////////////////////////////////////////////////////////////////////
		//single loop going through A1, A2, A3, A4 (only the ones passed in/still left to choose from)
		List<Double> condHList = new ArrayList<Double>(); 
		for (String singleAttribute : this.attributes){
			double condHValue = 0;
			//find conditional H (i.e. H(class | A1, A2, ...))
			int index = getAttributeIndex(singleAttribute);
			//only look at attributes left in our list
			HashMap<String, List<Integer>> map = listOfAttrMaps.get(index);
			it = map.entrySet().iterator();
			//look at each attr value
			while (it.hasNext()) {
				//attr value and counts pair
				Map.Entry<String, List<Integer>> pair = (Map.Entry<String, List<Integer>>)it.next();
				//get list of counts for this value where counts[0] = # occurance of val and counts[1-3] = # of this val that are L, B, R
				List<Integer> counts = pair.getValue();
				// H(label|attr) = #v1/total * H(#v1_L/#v1, #v1_B/#v1, #v1_R/#v1) + #v2/total * H(#v2_L/#v2, #v2_B/#v2, #v2_R/#v2) + ...
				for(int i = 1; i < counts.size(); i++){
					//#v1/total * H(#v1_L/#v1, #v1_B/#v1, #v1_R/#v1) = #v1/total*H(#v1_L/#v1) + #v1/total*H(#v1_B/#v1) + #v1/total*H(#v1_R/#v1)
					if (counts.get(i) != 0){
						condHValue += -((double) counts.get(i)/counts.get(0))*((double) counts.get(0)/total)*(Math.log((double) counts.get(i)/counts.get(0))/Math.log(2));
						//System.out.println("HValue: " + HValue + " CondH: "+ condHValue + " Total: " + total + " counts.get(0): " + counts.get(0) + " counts.get(i): " + counts.get(i));
					}
				}
			}
			//add new condH to end of list.
			condHList.add(condHValue);
		}

		////////////////////////////////////////////////////////////////////////////////////
		//
		////////////////////////////////////////////////////////////////////////////////////
		//loop through condHList looking for attr that gives largest H(label) - H(label|attr)
		double minCondH = condHList.get(0);
		int indexOfBestAttr = 0;
		for (int i = 0; i < condHList.size(); i++){
			if(minCondH > condHList.get(i)){
				minCondH = condHList.get(i);
				indexOfBestAttr = i;
			}
		}
		////////////////////////////////////////////////////////////////////////////////////
		//
		////////////////////////////////////////////////////////////////////////////////////
		//best attr will have same index in attributes that its condHValue had in the list of condHValues
		//return attributes.get(indexOfBestAttr);
		for(int i = 0; i < condHList.size(); i++){
			double hValTotal = HValue - condHList.get(i);
			System.out.format(train.attributes.get(i) + " %.5f\n", hValTotal);
		}

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
		DecTreeNodeImpl node;

		//if list of examples is empty. 
		if (instances.isEmpty()){
			return mostCommonClass(parentInstances);
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
			return mostCommonClass(instances);
		}

		///////////////////// SUDO

		// find best attribute to split on TODO make helper fcn
		String bestAttr = getBestAttribute(attributes, instances);
		// remove this attr from attributes list (not the master one though)
		List<String> updatedAttr = new ArrayList<String>(attributes);
		updatedAttr.remove(bestAttr);

		// make a new node N with this attribute
		node = new DecTreeNodeImpl(null, bestAttr, null, false);

		// for each possible value of the attribute:
		List<String> attrVals = this.attributeValues.get(bestAttr);
		for(String v: attrVals){
			// make list of instances with that value
			List<Instance> valInstances = new ArrayList<Instance>();
			int attrIndex = getAttributeIndex(bestAttr);
			for(Instance i: instances){
				if(i.attributes.get(attrIndex).equals(v))
					valInstances.add(i);
			}
			// new node = DecTreeNodeImplHelper(.....) (recursive call)
			DecTreeNodeImpl child = DecisionTreeImplHelper(valInstances, updatedAttr, instances);
			// make new node a child of node N
			child.setParentValue(v);
			node.children.add(child);
		}

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

	private String getBestAttribute(List<String> attributes, List<Instance> instances){

		//list of attribute maps (each of A1, A2, A3, ... has its own map)
		List<HashMap<String, List<Integer>>> listOfAttrMaps = new ArrayList<HashMap<String, List<Integer>>>();
		//zeroing out the map at every possible attribute and each attribute's values
		for (String singleAttribute : this.attributes){
			HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
			//going through each value and setting its count to 0
			for (String value : this.attributeValues.get(singleAttribute)){
				//first index is the total count, while each subsequent index corresponds
				//to the individual label counts
				List<Integer> counts = new ArrayList<Integer>();
				counts.add(0, 0);
				for (int i = 1; i <= this.labels.size(); i++){
					counts.add(i, 0);
				}
				map.put(value, counts);
			}
			listOfAttrMaps.add(map);
		}

		//count occurences of L, B, R using this map
		HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
		//looping through all the instances
		for (Instance singleInstance : instances){
			//populate the count (value) for each label/class found in instances
			if (labelMap.get(singleInstance.label) == null){
				labelMap.put(singleInstance.label, 1);
			}
			else{
				labelMap.put(singleInstance.label, labelMap.get(singleInstance.label) + 1);
			}

			//increment counts for the various values in the instance 
			for (int i = 0; i < singleInstance.attributes.size(); i++){
				//get the encoded list of integer values
				List<Integer> counts = listOfAttrMaps.get(i).get(singleInstance.attributes.get(i));
				//always increment the total count
				int newTotal = counts.get(0) + 1;
				counts.remove(0);
				counts.add(0, newTotal);
				//get index of the label we have encountered
				int labelIndex = getLabelIndex(singleInstance.label) + 1;
				int newLabelCount = counts.get(labelIndex) + 1;
				counts.remove(labelIndex);
				counts.add(labelIndex, newLabelCount);
				//put the augmented count list back into the map
				listOfAttrMaps.get(i).put(singleInstance.attributes.get(i), counts);
			}

		}


		//loop through key's (labels (L, B, R)) and start forming the H value through its formula
		double HValue = 0;
		double total = (double) instances.size();
		Iterator it = labelMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			HValue += -((double) (int)pair.getValue()/total)*(Math.log((double) (int)pair.getValue()/total)/Math.log(2));
		}


		//single loop going through A1, A2, A3, A4 (only the ones passed in/still left to choose from)
		List<Double> condHList = new ArrayList<Double>(); 
		for (String singleAttribute : attributes){
			double condHValue = 0;
			//find conditional H (i.e. H(class | A1, A2, ...))
			int index = getAttributeIndex(singleAttribute);
			//only look at attributes left in our list
			HashMap<String, List<Integer>> map = listOfAttrMaps.get(index);
			it = map.entrySet().iterator();
			//look at each attr value
			while (it.hasNext()) {
				//attr value and counts pair
				Map.Entry<String, List<Integer>> pair = (Map.Entry<String, List<Integer>>)it.next();
				//get list of counts for this value where counts[0] = # occurance of val and counts[1-3] = # of this val that are L, B, R
				List<Integer> counts = pair.getValue();
				// H(label|attr) = #v1/total * H(#v1_L/#v1, #v1_B/#v1, #v1_R/#v1) + #v2/total * H(#v2_L/#v2, #v2_B/#v2, #v2_R/#v2) + ...
				for(int i = 1; i < counts.size(); i++){
					//#v1/total * H(#v1_L/#v1, #v1_B/#v1, #v1_R/#v1) = #v1/total*H(#v1_L/#v1) + #v1/total*H(#v1_B/#v1) + #v1/total*H(#v1_R/#v1)
					if (counts.get(i) != 0){
						condHValue += -((double) counts.get(i)/counts.get(0))*((double) counts.get(0)/total)*(Math.log((double) counts.get(i)/counts.get(0))/Math.log(2));
					}
				}
			}
			//add new condH to end of list.
			condHList.add(condHValue);
		}

		//loop through condHList looking for attr that gives largest H(label) - H(label|attr)
		double minCondH = condHList.get(0);
		int indexOfBestAttr = 0;
		for (int i = 0; i < condHList.size(); i++){
			if(minCondH > condHList.get(i)){
				minCondH = condHList.get(i);
				indexOfBestAttr = i;
			}
		}
		//best attr will have same index in attributes that its condHValue had in the list of condHValues
		return attributes.get(indexOfBestAttr);
	}

}
