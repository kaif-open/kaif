package io.kaif.util;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Try<T> {

  public static final class Success<T> extends Try<T> {

    private final T value;

    public Success(T value) {
      this.value = value;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public boolean isFailure() {
      return false;
    }

    @Override
    public T get() {
      return value;
    }

    @Override
    public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
      return Try.apply(() -> mapper.apply(value));
    }

    @Override
    public <U> Try<U> flatMap(Function<? super T, ? extends Try<U>> mapper) {
      try {
        return mapper.apply(value);
      } catch (RuntimeException e) {
        return new Failure<>(e);
      }
    }

    public Try<Exception> failed() {
      return new Failure<>(new UnsupportedOperationException("Success.failed"));
    }

    @Override
    public Try<T> orElse(Try<T> defaultValue) {
      return this;
    }
  }

  public static final class Failure<T> extends Try<T> {

    private final RuntimeException exception;

    public Failure(RuntimeException exception) {
      this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public boolean isFailure() {
      return true;
    }

    @Override
    public T get() {
      throw exception;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
      return (Try<U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Try<U> flatMap(Function<? super T, ? extends Try<U>> mapper) {
      return (Try<U>) this;
    }

    @Override
    public Try<Exception> failed() {
      return new Success<>(exception);
    }

    @Override
    public Try<T> orElse(Try<T> defaultValue) {
      return defaultValue;
    }
  }

  public static <T> Try<T> apply(Supplier<T> supplier) {
    try {
      return new Success<>(supplier.get());
    } catch (RuntimeException e) {
      return new Failure<>(e);
    }
  }

  private Try() {
  }

  public abstract boolean isSuccess();

  public abstract boolean isFailure();

  public abstract T get();

  public abstract <U> Try<U> map(Function<? super T, ? extends U> mapper);

  public abstract <U> Try<U> flatMap(Function<? super T, ? extends Try<U>> mapper);

  public abstract Try<Exception> failed();

  public abstract Try<T> orElse(Try<T> defaultValue);
}
