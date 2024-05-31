package com.minhden.minimal.util;


public class BTree {
    public TreeNode root;
    public int t;

    public BTree(int t) {
        this.root = null;
        this.t = t;
    }

    public TreeNode search(int key) {
        if (this.root == null) {
            return null;
        } else {
            return this.root.search(key);
        }
    }

    public void insert(Entry<Integer, Object> entry) {
        if (root == null) {
            root = new TreeNode(t, true);
            root.entries[0] = entry;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                TreeNode s = new TreeNode(t, false);
                s.C[0] = root;
                s.splitChild(0, root);
                int i = 0;
                if (s.entries[0].getKey() < entry.getKey()) {
                    i++;
                }
                s.C[i].insertNonFull(entry);
                root = s;
            } else {
                root.insertNonFull(entry);
            }
        }
    }


}
