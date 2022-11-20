package org.example;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CompletableFutureCollector {

  /**
   * Transforms a <pre>{@code List<CompletableFuture<T>>}</pre> into a <pre>{@code CompletableFuture<List<T>>}</pre>
   *
   * @param <X> the computed result type
   * @param <T> some CompletableFuture
   * @return a CompletableFuture of <pre>{@code CompletableFuture<List<T>>}</pre> that is complete when all collected
   * CompletableFutures are complete.
   */
  public static <X, T extends CompletableFuture<X>> Collector<T, ?, CompletableFuture<List<X>>> collectResult() {
    return Collectors.collectingAndThen(Collectors.toList(), joinResult());
  }

  private static <X, T extends CompletableFuture<X>> Function<List<T>, CompletableFuture<List<X>>> joinResult() {
    return cfs -> allOf(cfs)
        .thenApply(v -> cfs
            .stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }

  private static <T extends CompletableFuture<?>> CompletableFuture<Void> allOf(List<T> ls) {
    return CompletableFuture.allOf(ls.toArray(new CompletableFuture[ls.size()]));
  }
}
