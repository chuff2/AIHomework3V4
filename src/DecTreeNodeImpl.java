import java.util.List;

public class DecTreeNodeImpl extends DecTreeNode {

	DecTreeNodeImpl(String _label, String _attribute, String _parentAttributeValue, boolean _terminal) {
		super(_label, _attribute, _parentAttributeValue, _terminal);
		// TODO Auto-generated constructor stub
	}

	//public void addChild(DecTreeNode child) {
	//	super(child);
	//}

	public String getAttribute(){
		return this.attribute;
	}
	
	public boolean isTerminal(){
		return this.terminal;
	}
	
	public List<DecTreeNode> getChildren(){
		return this.children;
	}
	
	public void setParentValue(String _parentAttrVal){
		this.parentAttributeValue = _parentAttrVal;
	}

}
