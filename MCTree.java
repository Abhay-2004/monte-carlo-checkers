package edu.iastate.cs472.proj2; 

/** 
 * This class requires no implementation.  You may use it to create a Monte Carlo search tree, or 
 * you may get the work done using the MCNode class. 
 * 
 * @author Abhay Prasanna Rao
 *
 * @param <E>
 */
public class MCTree<E> 
{
	MCNode<E> root;
	int size;

	public MCTree() {
		this.root = null;
		this.size = 0;
	}

	public void setRoot(MCNode<E> root) {
		this.root = root;
		size = 1;
	}

	public MCNode<E> getRoot() {
		return root;
	}

	public void addNode(MCNode<E> parent, MCNode<E> child) {
		if (root == null) {
			root = child;
		} else {
			parent.addChild(child);
		}
		size++;
	}

	public int getSize() {
		return size;
	}
}
