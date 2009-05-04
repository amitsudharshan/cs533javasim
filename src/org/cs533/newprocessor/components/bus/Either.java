/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

/**
 *
 * @author brandon
 */
public final class Either<A,B> {
    public final boolean isFirst;
    public final A first;
    public final B second;

    private Either(boolean isFirst, A a, B b) {
        this.isFirst = isFirst;
        this.first = a;
        this.second = b;
    }

    public static <X,Y> Either<X,Y> Left(X a) {
        return new Either<X,Y>(true, a, null);
    }
    public static <X,Y> Either<X,Y> Right(Y b) {
        return new Either<X,Y>(false, null, b);
    }

}
