import java.util.*;

/**
 * Created by Biyanta on 14/06/17.
 */
public class CustomQueue {

    private Map<String,Set<String>> inlinkSet;
    private List<Links> pqueue = new ArrayList<Links>();

    public CustomQueue(Map<String, Set<String>> inlinkSet) {

        this.inlinkSet = inlinkSet;
    }

    public void enqueue(Links links) {
        links.setWaitTime(System.currentTimeMillis());
        pqueue.add(links);


    }

    public boolean hasNext() {
        
        return (pqueue.size() > 0);
    }

    public List<Links> dequeue() {
        prioritize();
        List<Links> linkSet = new ArrayList<Links>();

        if (!pqueue.isEmpty()) {

            while (linkSet.size() != 500) {

                linkSet.add(pqueue.remove(0));
            }
            return linkSet;
        }
        return null;
    }

    private void prioritize() {
        Collections.sort(pqueue, new Comparator<Links>() {
            @Override
            public int compare(Links l1, Links l2) {

                if (l1.isRelevant() == l2.isRelevant()) {
                    if (inlinkSet.get(l1.getCanonicalizedUrl()).size() ==
                            inlinkSet.get(l2.getCanonicalizedUrl()).size()) {

                        return ((l1.getWaitTime() - l2.getWaitTime()) > 0)? 1: -1;
                    }
                    return inlinkSet.get(l2.getCanonicalizedUrl()).size() -
                            inlinkSet.get(l1.getCanonicalizedUrl()).size();
                }
                return (l2.isRelevant() - l1.isRelevant());
            }
        });

    }

    public int size() {
        return pqueue.size();
    }
}
