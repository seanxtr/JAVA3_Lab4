import cs_1c.*;
import java.util.*;

public class FHlazySearchTree<E extends Comparable< ? super E > >
   implements Cloneable {
   protected int mSize; // number of undeleted nodes
   protected int mSizeHard; // number of all nodes
   protected FHlazySTNode<E> mRoot;
   
   public FHlazySearchTree() { clear(); }
   public boolean empty() { return (mSize == 0); }
   public int size() { return mSize; }
   public int sizeHard() { return mSizeHard; }
   public void clear() { mSize = 0; mRoot = null; }
   public int showHeight() { return findHeight(mRoot, -1); }

   /**
    * Method to find minimum value in the tree
    * The method will ignore soft deleted nodes
    * @return      minimum value
    */
   public E findMin() {
      if (mRoot == null)
         throw new NoSuchElementException();
      
      FHlazySTNode<E> temp = findMinSkipDeleted(mRoot);
      
      // throw exception when nothing is found
      if (temp == null)
         throw new NoSuchElementException();
      
      return temp.data;
   }
   
   /**
    * Method to find maximum value in the tree
    * The method will ignore soft deleted nodes
    * @return      maximum value
    */
   public E findMax() {
      if (mRoot == null)
         throw new NoSuchElementException();
      
      FHlazySTNode<E> temp = findMaxSkipDeleted(mRoot);
      
   // throw exception when nothing is found
      if (temp == null)
         throw new NoSuchElementException();
      
      return temp.data;
   }
   
   /**
    * Method to search a value
    * @param x
    * @return
    */
   public E find( E x ) {
      FHlazySTNode<E> resultNode;
      resultNode = find(mRoot, x);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }
   
   /***
    * Method to check if a value exists in the tree
    * @param x
    * @return
    */
   public boolean contains(E x)  { 
      return find(mRoot, x) != null; 
   }
   
   /**
    * Method to insert a new value to the tree
    * @param x     value to insert
    * @return      boolean indicates if the call success
    */
   public boolean insert( E x ) {
      int oldSize = mSize;
      mRoot = insert(mRoot, x);
      return (mSize != oldSize);
   }
   
   /**
    * Method to remove a value from the tree
    * @param x     value to remove
    * @return      boolean indicates if the call success
    */
   public boolean remove( E x ) {
      int oldSize = mSize;
      remove(mRoot, x);
      return (mSize != oldSize);
   }
   
   public < F extends Traverser<? super E > > 
   void traverse(F func) {
      traverse(func, mRoot);
   }
   
   /**
    * Method to make hard-copy of the current tree
    */
   public Object clone() throws CloneNotSupportedException {
	  FHlazySearchTree<E> newObject = (FHlazySearchTree<E>)super.clone();
      newObject.clear();  // can't point to other's data

      newObject.mRoot = cloneSubtree(mRoot);
      newObject.mSize = mSize;
      newObject.mSizeHard = mSizeHard;
      return newObject;
   }
   
   /**
    * Method to perform hard delete
    */
   public void collectGarbage(){
      collectGarbage(mRoot);
   }
   
   // private helper methods ----------------------------------------
   /**
    * Helper method to find the node with minimum value
    * @param root           starting node
    * @return               target node
    */
   protected FHlazySTNode<E> findMin( FHlazySTNode<E> root ) {
      if (root == null)
         return null;
      if (root.lftChild == null)
         return root;
      return findMin(root.lftChild);
   }
   
   /**
    * Helper method to find the node with minimum value and exclude 
    * the soft deleted nodes
    * @param root           starting node
    * @return               target node
    */
   protected FHlazySTNode<E> findMinSkipDeleted(FHlazySTNode<E> root) {
      if (root == null)
         return null;
      
      FHlazySTNode<E> temp = findMinSkipDeleted(root.lftChild);
      
      if (temp != null)
         return temp;
      
      if (!root.deleted)
         return root;
      
      return findMinSkipDeleted(root.rtChild);
   }
   
   /**
    * Helper method to find the node with maximum value excluding
    * the soft deleted nodes
    * @param root           starting node
    * @return               target node
    */
   protected FHlazySTNode<E> findMaxSkipDeleted( FHlazySTNode<E> root ) {
      if (root == null)
         return null;
      
      FHlazySTNode<E> temp = findMaxSkipDeleted(root.rtChild);
      
      if (temp != null)
         return temp;
      
      if (!root.deleted)
         return root;
      
      return findMaxSkipDeleted(root.lftChild);
   }
   
   /**
    * helper method to perform insertion
    * @param root     the node to start
    * @param x        the value to insert
    * @return         the root node
    */
   protected FHlazySTNode<E> insert( FHlazySTNode<E> root, E x ) {
      int compareResult;  // avoid multiple calls to compareTo()
      
      // when the node to start is null, create a new one
      if (root == null) {
         mSize++;
         mSizeHard++;
         return new FHlazySTNode<E>(x, null, null);
      }
      
      // compare value to current root value
      compareResult = x.compareTo(root.data); 
      if ( compareResult < 0 )
         root.lftChild = insert(root.lftChild, x);
      else if ( compareResult > 0 )
         root.rtChild = insert(root.rtChild, x);
      else if (root.deleted) {
         // found the node and bring it back to live
         root.deleted = false;
         mSize++;
      }
      return root;
   }

   /**
    * helper method to perform a soft delete 
    * @param root   the node to search
    * @param x      the target value to delete
    */
   protected void remove(FHlazySTNode<E> root, E x ) {
      int compareResult;
     
      // terminate recursion until root is null
      if (root == null)
         return;

      // compare value to root value
      compareResult = x.compareTo(root.data); 
      if ( compareResult < 0 )
         remove(root.lftChild, x);
      else if ( compareResult > 0 )
         remove(root.rtChild, x);
      // found the node and set its deleted property to true
      else if (!root.deleted) {
          root.deleted = true;
          this.mSize--;
      }
      return;
   }
   
   /**
    * Helper method to perform hard deletion
    * @param root        starting node
    * @param x           target value
    * @return            result node
    */
   protected FHlazySTNode<E> removeHard(FHlazySTNode<E> root, E x) {
      int compareResult;  // avoid multiple calls to compareTo()
     
      if (root == null)
         return null;

      compareResult = x.compareTo(root.data); 
      if ( compareResult < 0 )
         root.lftChild = removeHard(root.lftChild, x);
      else if ( compareResult > 0 )
         root.rtChild = removeHard(root.rtChild, x);
      // found the node
      else if (root.lftChild != null && root.rtChild != null) {
         FHlazySTNode<E> temp = findMin(root.rtChild);
         root.data = temp.data;
         root.deleted = temp.deleted;
         root.rtChild = removeHard(root.rtChild, root.data);
      } else {
         root = (root.lftChild != null)? root.lftChild : root.rtChild;
         mSizeHard--;
      }
      return root;
   }
   
   protected <F extends Traverser<? super E>> 
   void traverse(F func, FHlazySTNode<E> treeNode) {
      if (treeNode == null)
         return;

      traverse(func, treeNode.lftChild);
      
      // only print out the node when it's visible to user
      if (!treeNode.deleted)
    	  func.visit(treeNode.data);
      
      traverse(func, treeNode.rtChild);
   }
   
   protected FHlazySTNode<E> find( FHlazySTNode<E> root, E x ) {
      int compareResult;  // avoid multiple calls to compareTo()

      if (root == null)
         return null;

      compareResult = x.compareTo(root.data); 
      if (compareResult < 0)
         return find(root.lftChild, x);
      if (compareResult > 0)
         return find(root.rtChild, x);
      return root;   // found
   }
   
   /**
    * Helper method to clone the input node and its child nodes
    * @param root       starting node
    * @return           result node
    */
   protected FHlazySTNode<E> cloneSubtree(FHlazySTNode<E> root) {
      FHlazySTNode<E> newNode;
      
      // end the method call when starting node is null
      if (root == null)
         return null;

      // does not set myRoot which must be done by caller
      newNode = new FHlazySTNode<E> (
         root.data, 
         cloneSubtree(root.lftChild), 
         cloneSubtree(root.rtChild)
      );
      
      // also clone the deletion state
      newNode.deleted = root.deleted;
      return newNode;
   }
   
   protected int findHeight( FHlazySTNode<E> treeNode, int height ) {
      int leftHeight, rightHeight;
      if (treeNode == null)
         return height;
      height++;
      leftHeight = findHeight(treeNode.lftChild, height);
      rightHeight = findHeight(treeNode.rtChild, height);
      return (leftHeight > rightHeight)? leftHeight : rightHeight;
   }
   
   /**
    * Helper method to perform hard removal of deleted nodes
    * @param node         starting node
    * @return             result node
    */
   protected FHlazySTNode<E> collectGarbage(FHlazySTNode<E> node){
      // terminate the recursion when reaching the end of tree
      if (node == null)
         return null;

      // check if current node should be deleted
      if (node.deleted) {
         // perform hard deletion on current node
         node = removeHard(node, node.data);
         
         // recursive call to continue the progress
         collectGarbage(node);
      } else {
         // when current node is active, do collect garbage on its child-nodes
         collectGarbage(node.rtChild);
         collectGarbage(node.lftChild);
      }
      
      return node;
   }
}

/**
 * Class of binary tree node to store actual data value and used as
 * the basic building block of the binary search tree
 * @author Tianrong Xiao
 *
 * @param <E>     Generic data type stored by the class
 */
class FHlazySTNode<E extends Comparable< ? super E > > {
   // use public access so the tree or other classes can access members
   public boolean deleted;
   public FHlazySTNode<E> lftChild, rtChild;
   public E data;
   public FHlazySTNode<E> myRoot;  // needed to test for certain error

   /**
    * Three parameters constructor
    * @param d          value to store
    * @param lft        left child node
    * @param rt         right child node
    */
   public FHlazySTNode( E d, FHlazySTNode<E> lft, FHlazySTNode<E> rt ) {
      deleted = false;
      lftChild = lft; 
      rtChild = rt;
      data = d;
   }
   
   /**
    * Default constructor
    */
   public FHlazySTNode() {
      this(null, null, null);
   }
   
   // function stubs -- for use only with AVL Trees when we extend
   public int getHeight() { return 0; }
   boolean setHeight(int height) { return true; }
}
