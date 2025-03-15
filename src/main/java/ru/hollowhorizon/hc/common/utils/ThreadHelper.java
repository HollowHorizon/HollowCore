/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ThreadHelper {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * A method that looks for an element in any array that inherits from {@link Collection}
     * @return true - if found, false - if not
     * @param collection - Any array that inherits from {@link Collection}
     * @param target - Element
     */
    public static <T> boolean containsOnCollection(Collection<T> collection, T target) {
        return findOnCollection(collection, target) != null;
    }

    /**
     * A method that searches for an element by means in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param predicate - Condition
     */
    public static <T> T findOnCollection(Collection<T> collection, Predicate<T> predicate) {
        return findOnCollection(collection,predicate, getFixThreads());
    }

    /**
     * A method that searches for an element by means in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param predicate - Condition
     * @param threadsCount - Number of cores to be used
     */
    public static <T> T findOnCollection(Collection<T> collection, Predicate<T> predicate, int threadsCount) {
        int th = threadsCount;
        int partSize = collection.size() / th;
        ExecutorService executor = getExecutor();
        List<Future<T>> futures = new ArrayList<>();
        List<T> list = new ArrayList<>(collection);
        if(th > collection.size())
            th = 1;

        for (int i = 0; i < th; i++) {
            int start = i * partSize;
            int end = (i == th - 1) ? collection.size() : start + partSize;
            Collection<T> subCollection = list.subList(start, end);
            futures.add(executor.submit(new SearchTaskPredicate<>(subCollection, predicate)));
        }

        executor.shutdown();

        try {
            return waitSearchTasks(futures);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A method that looks for an element in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param target - The item we are looking for
     */
    @Nullable
    public static <T> T findOnCollection(Collection<T> collection, T target) {
        return findOnCollection(collection,target, getFixThreads());
    }

    /**
     * A method that looks for an element in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param target - The item we are looking for
     * @param threadsCount - Number of cores to be used
     */
    @Nullable
    public static <T> T findOnCollection(Collection<T> collection, T target, int threadsCount) {
        int th = threadsCount;
        int partSize = collection.size() / th;
        ExecutorService executor = getExecutor();
        List<Future<T>> futures = new ArrayList<>();
        List<T> list = new ArrayList<>(collection);
        if(th > collection.size())
            th = 1;

        for (int i = 0; i < th; i++) {
            int start = i * partSize;
            int end = (i == th - 1) ? collection.size() : start + partSize;
            Collection<T> subCollection = list.subList(start, end);
            futures.add(executor.submit(new SearchTask<>(subCollection, target)));
        }

        executor.shutdown();

        try {
            return waitSearchTasks(futures);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A method that searches for an element by means in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param predicate - Condition
     * @param function
     */
    public static <T,R> R findOnCollection(Collection<T> collection, Function<T, R> function, Predicate<T> predicate) {
        return findOnCollection(collection, function, predicate, getFixThreads());
    }

    /**
     * A method that searches for an element by means in any array that inherits from {@link Collection}
     * @param collection - Any array that inherits from {@link Collection}
     * @param predicate - Condition
     * @param threadsCount - Number of cores to be used
     * @param function
     */
    public static <T, R> R findOnCollection(Collection<T> collection, Function<T, R> function, Predicate<T> predicate, int threadsCount) {
        int th = threadsCount;
        int partSize = collection.size() / th;
        ExecutorService executor = getExecutor();
        List<Future<T>> futures = new ArrayList<>();
        List<T> list = new ArrayList<>(collection);
        if(th > collection.size())
            th = 1;

        R obj = null;

        for (int i = 0; i < th; i++) {
            int start = i * partSize;
            int end = (i == th - 1) ? collection.size() : start + partSize;
            Collection<T> subCollection = list.subList(start, end);
            futures.add(executor.submit(new SearchTaskFunction<T,R>(subCollection, function, predicate, obj)));
        }

        executor.shutdown();

        try {
            waitSearchTasks(futures);
            return obj;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T findOnCollection(Collection<T> collection, Function<T, Boolean> function) {
        return findOnCollection(collection,function, getFixThreads());
    }

    public static <T> T findOnCollection(Collection<T> collection, Function<T, Boolean> function, int threadsCount) {
        int th = threadsCount;
        int partSize = collection.size() / th;
        ExecutorService executor = getExecutor();
        List<Future<T>> futures = new ArrayList<>();
        List<T> list = new ArrayList<>(collection);
        if(th > collection.size())
            th = 1;

        for (int i = 0; i < th; i++) {
            int start = i * partSize;
            int end = (i == th - 1) ? collection.size() : start + partSize;
            Collection<T> subCollection = list.subList(start, end);
            futures.add(executor.submit(new SearchTaskFunctionSecond<>(subCollection, function)));
        }

        executor.shutdown();

        try {
            return waitSearchTasks(futures);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A method that blocks the main thread until the tasks are executed
     */
    public static <T> T waitSearchTasks(List<Future<T>> tasks) throws ExecutionException, InterruptedException {
        for (Future<T> task : tasks) {
            T find = task.get();
            if(find != null) {
                return find;
            }
        }

        return null;
    }

    public static int getFixThreads(){
        int p = Runtime.getRuntime().availableProcessors();
        return p >= 2 ? p / 2 : p;
    }

    public static ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(getFixThreads());
    }

    public static ExecutorService getExecutor(int numsThread) {
        return Executors.newFixedThreadPool(numsThread);
    }

    static class SearchTask<T> implements Callable<T> {
        private final Collection<T> collection;
        private final T target;

        public SearchTask(Collection<T> collection, T target) {
            this.collection = collection;
            this.target = target;
        }

        @Override
        public T call() {
            for (T element : collection) {
                if (element.equals(target)) {
                    return element;
                }
            }
            return null;
        }
    }

    static class SearchTaskPredicate<T> implements Callable<T> {
        private final Collection<T> collection;
        private final Predicate<T> target;

        public SearchTaskPredicate(Collection<T> collection, Predicate<T> target) {
            this.collection = collection;
            this.target = target;
        }

        @Override
        public T call() {
            for (T element : collection) {
                if (target.test(element)) {
                    return element;
                }
            }
            return null;
        }
    }

    static class SearchTaskFunction<T, R> implements Callable<T> {
        private final Collection<T> collection;
        private final Function<T,R> target;
        private final Predicate<T> condition;
        private R returnValue;

        public SearchTaskFunction(Collection<T> collection, Function<T,R> target, Predicate<T> condition, R returnValue) {
            this.collection = collection;
            this.target = target;
            this.condition = condition;
            this.returnValue = returnValue;
        }

        @Override
        public T call() {
            for (T element : collection) {
                if(condition.test(element)) {
                    returnValue = target.apply(element);
                    return element;
                }
            }
            return null;
        }
    }

    static class SearchTaskFunctionSecond<T> implements Callable<T> {
        private final Collection<T> collection;
        private final Function<T,Boolean> target;

        public SearchTaskFunctionSecond(Collection<T> collection, Function<T,Boolean> target) {
            this.collection = collection;
            this.target = target;
        }

        @Override
        public T call() {
            for (T element : collection) {
                if(target.apply(element)) {
                    return element;
                }
            }
            return null;
        }
    }
}
