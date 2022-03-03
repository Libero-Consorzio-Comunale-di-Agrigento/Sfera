package it.finmatica.atti.integrazioni.ricercadocumenti

/**
 * Created by esasdelli on 02/10/2017.
 */
class PagedList<T> extends ArrayList<T> {
    private final int totalCount

    PagedList (List<T> list, int total) {
        super(list)
        this.totalCount = total
    }

    int getTotalCount () {
        return totalCount
    }
}
