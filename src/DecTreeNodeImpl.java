
public class DecTreeNodeImpl extends DecTreeNode {

	DecTreeNodeImpl(String _label, String _attribute, String _parentAttributeValue, boolean _terminal) {
		super(_label, _attribute, _parentAttributeValue, _terminal);
		// TODO Auto-generated constructor stub
	}

	public void addChild(DecTreeNode child) {
		super(child);
	}

	public void setParentValue(String _parentAttrVal){
		this.parentAttributeValue = _parentAttrVal;
	}

}
