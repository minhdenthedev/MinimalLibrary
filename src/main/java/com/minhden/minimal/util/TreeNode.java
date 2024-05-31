package com.minhden.minimal.util;


public class TreeNode {
    Entry<Integer, Object>[] entries;
    int t;
    TreeNode[] C;
    int n;
    boolean leaf;

    @SuppressWarnings("unchecked")
    public TreeNode(int t, boolean leaf) {
        this.entries = new Entry[2 * t - 1];
        this.t = t;
        this.C = new TreeNode[2 * t];
        this.n = 0;
        this.leaf = leaf;
    }

    void insertNonFull(Entry<Integer, Object> entry) {
        int i = n - 1;
        int k = entry.getKey();
        if (leaf) {
            while (i >= 0 && entries[i].getKey() > k) {
                entries[i + 1] = entries[i];
                i--;
            }
            entries[i + 1] = entry;
            n++;
        } else {
            while (i >= 0 && entries[i].getKey() > k) {
                i--;
            }
            if (C[i + 1].n == 2 * t - 1) {
                splitChild(i + 1, C[i + 1]);
                if (entries[i + 1].getKey() < k) {
                    i++;
                }
            }
            C[i + 1].insertNonFull(entry);
        }
    }

    void splitChild(int i, TreeNode y) {
        TreeNode z = new TreeNode(y.t, y.leaf);
        z.n = t - 1;
        for (int j = 0; j < t - 1; j++) {
            z.entries[j] = y.entries[j + t];
        }
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.C[j] = y.C[j + t];
            }
        }
        y.n = t - 1;
        for (int j = n; j > i; j--) {
            C[j + 1] = C[j];
        }
        C[i + 1] = z;
        for (int j = n - 1; j >= i; j--) {
            entries[j + 1] = entries[j];
        }
        entries[i] = y.entries[t - 1];
        n++;
    }

    TreeNode search(int k) {
        int i = 0;
        while (i < n && k > entries[i].getKey()) {
            i++;
        }
        if (i < n && k == entries[i].getKey()) {
            return this;
        }
        if (leaf) {
            return null;
        }
        return C[i].search(k);
    }

    public Entry<Integer, Object>[] getEntries() {
        return entries;
    }
}
