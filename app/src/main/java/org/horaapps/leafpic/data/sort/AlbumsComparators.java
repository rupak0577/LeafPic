package org.horaapps.leafpic.data.sort;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.util.NumericComparator;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class AlbumsComparators {


    private static Comparator<Album> getComparator(SortingMode sortingMode, Comparator<Album> base) {
        switch (sortingMode) {
            case NAME:
                return getNameComparator(base);
            case SIZE:
                return getSizeComparator(base);
            case DATE: default:
                return getDateComparator(base);
            case NUMERIC:
                return getNumericComparator(base);
        }
    }

    public static Comparator<Album> getComparator(SortingMode sortingMode, SortingOrder sortingOrder) {

        Comparator<Album> comparator = getComparator(sortingMode, getBaseComparator(sortingOrder));

        return sortingOrder == SortingOrder.ASCENDING
                ? comparator : reverse(comparator);
    }

    private static Comparator<Album> reverse(Comparator<Album> comparator) {
        return (o1, o2) -> comparator.compare(o2, o1);
    }

    private static Comparator<Album> getBaseComparator(SortingOrder sortingOrder) {
        return sortingOrder == SortingOrder.ASCENDING
                ? getPinned() : getReversedPinned();
    }

    private static Comparator<Album> getPinned() {
        return (o1, o2) -> {
            if (o1.getAlbumInfo().getPinned() == o2.getAlbumInfo().getPinned()) return 0;
            return o1.getAlbumInfo().getPinned() ? -1 : 1;
        };
    }

    private static Comparator<Album> getReversedPinned() {
        return (o1, o2) -> getPinned().compare(o2, o1);
    }

    private static Comparator<Album> getDateComparator(Comparator<Album> base){
        return (a1, a2) -> {
            int res = base.compare(a1, a2);
            if (res == 0)
                return Long.compare(a1.getAlbumInfo().getDateModified(), a2.getAlbumInfo().getDateModified());
            return res;
        };
    }

    private static Comparator<Album> getNameComparator(Comparator<Album> base) {
        return (a1, a2) -> {
            int res = base.compare(a1, a2);
            if (res == 0)
                return a1.getAlbumName().toLowerCase().compareTo(a2.getAlbumName().toLowerCase());
            return res;
        };
    }

    private static Comparator<Album> getSizeComparator(Comparator<Album> base) {
        return (a1, a2) -> {
            int res = base.compare(a1, a2);
            if (res == 0)
                return a1.getFileCount() - a2.getFileCount();
            return res;
        };
    }

    private static Comparator<Album> getNumericComparator(Comparator<Album> base) {
        return (a1, a2) -> {
            int res = base.compare(a1, a2);
            if (res == 0)
                return NumericComparator.filevercmp(a1.getAlbumName().toLowerCase(), a2.getAlbumName().toLowerCase());
            return res;
        };
    }
}
