package net.funkenburg.gc.backend.fetch;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class CachingGeocacheProviderTest {

    @Test
    void test() {
        List<String> input = IntStream.range(1, 100).mapToObj(Integer::toString).toList();
        provide(input).forEach(x -> System.out.println(x));
    }

    private Stream<String> provide(List<String> input) {
        return chunk(input.stream(), 10)
                .map(
                        xs -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return "Fetch result of " + Strings.join(xs, ',');
                        });
    }

    <T> Stream<List<T>> chunk(Stream<T> stream, int size) {
        Iterator<T> iterator = stream.iterator();
        Iterator<List<T>> listIterator =
                new Iterator<>() {
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public List<T> next() {
                        List<T> result = new ArrayList<>(size);
                        for (int i = 0; i < size && iterator.hasNext(); i++) {
                            result.add(iterator.next());
                        }
                        return result;
                    }
                };
        return StreamSupport.stream(((Iterable<List<T>>) () -> listIterator).spliterator(), false);
    }
}
