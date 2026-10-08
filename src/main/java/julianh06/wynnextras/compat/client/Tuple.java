package julianh06.wynnextras.compat.client;

/** Mutable pair replacing Minecraft's removed Tuple. */
public final class Tuple<A, B> {
    private A first;
    private B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getA() { return first; }
    public B getB() { return second; }
    public void setA(A value) { first = value; }
    public void setB(B value) { second = value; }
}
