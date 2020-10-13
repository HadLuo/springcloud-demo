package com.uc.framework;

import java.io.Serializable;

/***
 * 可json序列化的pair
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年5月8日 新建
 */
public final class Pair<L, R> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3409236913009054207L;
    private L left;
    private R right;

    public static <L, R> Pair<L, R> of(L left, R right) {
        Pair<L, R> p = new Pair<L, R>();
        p.setLeft(left);
        p.setRight(right);
        return p;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "Pair [left=" + left + ", right=" + right + "]";
    }

}
