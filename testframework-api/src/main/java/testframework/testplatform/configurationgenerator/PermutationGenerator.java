/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.testplatform.configurationgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

//Source: https://stackoverflow.com/a/45143506/1663367

/**
 * Immutable class for generating permutations of a given list of lists
 *
 * @param <T> list type
 */
public class PermutationGenerator<T> implements Iterable<List<T>> {
    private final List<List<T>> elements;

    public PermutationGenerator(List<List<T>> elements) {
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<T> get(final int index) {
        int currentIndex = index;
        List<T> result = new ArrayList<>();
        for (int i = elements.size() - 1; i >= 0; i--) {
            List<T> counter = elements.get(i);
            int counterSize = counter.size();
            result.add(counter.get(currentIndex % counterSize));
            currentIndex /= counterSize;
        }
        Collections.reverse(result);
        return result;
    }

    public int size() {
        int result = 1;
        for (List<T> next : elements) result *= next.size();
        return result;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<List<T>>() {

            private final int collectionSize = size();
            private AtomicInteger position = new AtomicInteger(0);

            @Override
            public boolean hasNext() {
                return position.get() < collectionSize;
            }

            @Override
            public List<T> next() {
                int next = position.getAndIncrement();
                if (next >= collectionSize) {
                    throw new NoSuchElementException();
                }
                return get(next);
            }

        };
    }
}
