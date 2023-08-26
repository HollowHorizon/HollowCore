package ru.hollowhorizon.hc.common.scripting.coroutines;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CancellableContinuationKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objenesis.instantiator.ObjectInstantiator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PersistingWrapper {
    private static final Path STATE_PATH = Path.of("coroutine.bin");
    public static final Wrapper wrapper = new Wrapper() {
        public <T> Object invoke(
                @NotNull Function1<? super Continuation<? super T>, ?> function1,
                @NotNull Continuation<? super T> continuation
        ) {
            var persistedContinuation = new PersistedContinuation<>(continuation);
            if (Files.exists(STATE_PATH)) {
                try (var input = new Input(Files.newInputStream(STATE_PATH))) {
                    var kryo = getKryo();
                    var references = kryo.getReferenceResolver();
                    references.setReadObject(references.nextReadId(null), persistedContinuation.getContext());
                    references.setReadObject(references.nextReadId(null), persistedContinuation);
                    return new Function1<CancellableContinuation<? super Unit>, Unit>() {
                        CancellableContinuation<? super Unit> cancellableContinuation;

                        @Override
                        public Unit invoke(CancellableContinuation<? super Unit> cancellableContinuation) {
                            this.cancellableContinuation = cancellableContinuation;
                            return Unit.INSTANCE;
                        }

                        Object get() {
                            var coroutineSuspended = CancellableContinuationKt.suspendCancellableCoroutine(
                                    this, (Continuation<Unit>) kryo.readClassAndObject(input)
                            );
                            cancellableContinuation.resume(Unit.INSTANCE, null);
                            return coroutineSuspended;
                        }
                    }.get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return function1.invoke(persistedContinuation);
        }
    };

    @NotNull
    private static Kryo getKryo() {
        var kryo = new Kryo();
        kryo.setAutoReset(false);
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy() {
            @Override
            public ObjectInstantiator<?> newInstantiatorOf(Class type) {
                var klass = (Class<?>) type;
                var kotlinClass = JvmClassMappingKt.getKotlinClass(klass);
                try {
                    var objectInstance = kotlinClass.getObjectInstance();
                    if (objectInstance != null) {
                        return () -> objectInstance;
                    }
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    var constructor = klass.getDeclaredConstructor(Continuation.class);
                    return () -> {
                        try {
                            return constructor.newInstance((Continuation<?>) null);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } catch (NoSuchMethodException ignored) {
                }
                return super.newInstantiatorOf(type);
            }
        });
        return kryo;
    }

    private static class PersistedContinuation<T> implements Continuation<T> {
        private final Continuation<? super T> continuation;
        private final CoroutineContext coroutineContext;

        private PersistedContinuation(Continuation<? super T> continuation) {
            this.continuation = continuation;
            coroutineContext = new PersistingCoroutineContext<>(this).plus(continuation.getContext());
        }

        @NotNull
        @Override
        public CoroutineContext getContext() {
            return coroutineContext;
        }

        @Override
        public void resumeWith(@NotNull Object o) {
            continuation.resumeWith(o);
        }
    }

    public static class PersistingCoroutineContext<T> extends Persistor {
        private final PersistedContinuation<T> persistedContinuation;

        public PersistingCoroutineContext(PersistedContinuation<T> persistedContinuation) {
            this.persistedContinuation = persistedContinuation;
        }

        @Nullable
        @Override
        public Object persist(@NotNull Continuation<? super Unit> $completion) {
            try {
                var tempFile = Files.createTempFile(null, null);
                try (var output = new Output(Files.newOutputStream(tempFile))) {
                    var kryo = getKryo();
                    kryo.getReferenceResolver().addWrittenObject(persistedContinuation.getContext());
                    kryo.getReferenceResolver().addWrittenObject(persistedContinuation);
                    kryo.writeClassAndObject(output, $completion);
                }
                Files.move(tempFile, STATE_PATH, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Unit.INSTANCE;
        }
    }
}
