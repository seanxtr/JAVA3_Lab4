import cs_1c.*;

import java.util.*;

public class FHlazySearchTree<E extends Comparable< ? super E > >
   implements Cloneable
{
   protected int mSize; // number of undeleted nodes
   protected int mSizeHard; // number of all nodes
   protected FHlazySTNode<E> mRoot;
   
   public FHlazySearchTree() { clear(); }
   public boolean empty() { return (mSize == 0); }
   public int size() { return mSize; }
   public int sizeHard() { return mSizeHard; }
   public void clear() { mSize = 0; mRoot = null; }
   public int showHeight() { return findHeight(mRoot, -1); }

   
   public E findMin() 
   {
      if (mRoot == null)
         throw new NoSuchElementException();
      return findMin(mRoot).data;
   }
   
   public E findMax() 
   {
      if (mRoot == null)
         throw new NoSuchElementException();
      return findMax(mRoot).data;
   }
   
   public E find( E x )
   {
      FHlazySTNode<E> resultNode;
      resultNode = find(mRoot, x);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }
   public boolean contains(E x)  { return find(mRoot, x) != null; }
   
   /**
    * Method to insert a new value to the tree
    * @param x     value to insert
    * @return      boolean indicates if the call success
    */
   public boolean insert( E x )
   {
      int oldSize = mSize;
      mRoot = insert(mRoot, x);
      return (mSize != oldSize);
   }
   
   /**
    * Method to remove a value from the tree
    * @param x     value to remove
    * @return      boolean indicates if the call success
    */
   public boolean remove( E x )
   {
      int oldSize = mSize;
      remove(mRoot, x);
      return (mSize != oldSize);
   }
   
   public < F extends Traverser<? super E > > 
   void traverse(F func)
   {
      traverse(func, mRoot);
   }
   
   /**
    * 
    */
   public Object clone() throws CloneNotSupportedException
   {
	  FHlazySearchTree<E> newObject = (FHlazySearchTree<E>)super.clone();
      newObject.clear();  // can't point to other's data

      newObject.mRoot = cloneSubtree(mRoot);
      newObject.mSize = mSize;
      
      return newObject;
   }
   
   /**
    * Method to perform hard delete
    */
   public void collectGarbage(){
      collectGarbage(mRoot);
   }
   
   // private helper methods ----------------------------------------
   protected FHlazySTNode<E> findMin( FHlazySTNode<E> root ) 
   {
      if (root == null)
         return null;
      if (root.lftChild == null)
         return root;
      return findMin(root.lftChild);
   }
   
   protected FHlazySTNode<E> findMax( FHlazySTNode<E> root ) 
   {
      if (root == null)
         return null;
      if (root.rtChild == null)
         return root;
      return findMax(root.rtChild);
   }
   
   /**
    * helper method to perform insertion
    * @param root     the node to start
    * @param x        the value to insert
    * @return         the root node
    */
   protected FHlazySTNode<E> insert( FHlazySTNode<E> root, E x )
   {
      int compareResult;  // avoid multiple calls to compareTo()
      
      // when the node to start is null, create a new one
      if (root == null)
      {
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
      else { 
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
   protected void remove(FHlazySTNode<E> root, E x )
   {
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
      else {
          root.deleted = true;
          this.mSize--;
      }
      return;
   }
   
   /**
    * Helper method to perform hard deletion
    * @param root
    * @param x
    * @return
    */
   protected FHlazySTNode<E> removeHard(FHlazySTNode<E> root, E x)
   {
      int compareResult;  // avoid multiple calls to compareTo()
     
      if (root == null)
         return null;

      compareResult = x.compareTo(root.data); 
      if ( compareResult < 0 )
         root.lftChild = removeHard(root.lftChild, x);
      else if ( compareResult > 0 )
         root.rtChild = removeHard(root.rtChild, x);

      // found the node
      else if (root.lftChild != null && root.rtChild != null)
      {
         root.data = findMin(root.rtChild).data;
         root.rtChild = removeHard(root.rtChild, root.data);
      }
      else
      {
         root = (root.lftChild != null)? root.lftChild : root.rtChild;
         mSize--;
         mSizeHard--;
      }
      return root;
   }
   
   protected <F extends Traverser<? super E>> 
   void traverse(F func, FHlazySTNode<E> treeNode)
   {
      if (treeNode == null)
         return;

      traverse(func, treeNode.lftChild);
      
      // only print out the node when it's visible to user
      if (!treeNode.deleted)
    	  func.visit(treeNode.data);
      
      traverse(func, treeNode.rtChild);
   }
   
   protected FHlazySTNode<E> find( FHlazySTNode<E> root, E x )
   {
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
   
   protected FHlazySTNode<E> cloneSubtree(FHlazySTNode<E> root)
   {
      FHlazySTNode<E> newNode;
      if (root == null)
         return null;

      // does not set myRoot which must be done by caller
      newNode = new FHlazySTNode<E>
      (
         root.data, 
         cloneSubtree(root.lftChild), 
         cloneSubtree(root.rtChild)
      );
      return newNode;
   }
   
   protected int findHeight( FHlazySTNode<E> treeNode, int height ) 
   {
      int leftHeight, rightHeight;
      if (treeNode == null)
         return height;
      height++;
      leftHeight = findHeight(treeNode.lftChild, height);
      rightHeight = findHeight(treeNode.rtChild, height);
      return (leftHeight > rightHeight)? leftHeight : rightHeight;
   }
   
   protected FHlazySTNode<E> collectGarbage(FHlazySTNode<E> root){
      // terminate the recursion when reaching the end of tree
      if (root == null)
         return null;

      // check if the root should be deleted
      if (root.deleted)
         mRoot = removeHard(root, root.data);
      
      collectGarbage(root.rtChild);
      return root;   
   }
}

/**
 * 
 * @author Tianrong Xiao
 *
 * @param <E>
 */
class FHlazySTNode<E extends Comparable< ? super E > >
{
   // use public access so the tree or other classes can access members
   public boolean deleted;
   public FHlazySTNode<E> lftChild, rtChild;
   public E data;
   public FHlazySTNode<E> myRoot;  // needed to test for certain error

   public FHlazySTNode( E d, FHlazySTNode<E> lft, FHlazySTNode<E> rt )
   {
      deleted = false;
      lftChild = lft; 
      rtChild = rt;
      data = d;
   }
   
   public FHlazySTNode()
   {
      this(null, null, null);
   }
   
   // function stubs -- for use only with AVL Trees when we extend
   public int getHeight() { return 0; }
   boolean setHeight(int height) { return true; }
}
