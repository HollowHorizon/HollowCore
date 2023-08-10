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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;

public class PersistingWrapper {
    private static final Path STATE_PATH = Path.of("coroutine");
    private static final Path OUTPUT_PATH = Path.of("coroutine.tmp");

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
                try (Output output = new Output(Files.newOutputStream(OUTPUT_PATH))) {
                    Kryo kryo = getKryo();
                    kryo.getReferenceResolver().addWrittenObject(persistedContinuation.getContext());
                    kryo.getReferenceResolver().addWrittenObject(persistedContinuation);
                    kryo.writeClassAndObject(output, $completion);
                }
                Files.move(OUTPUT_PATH, STATE_PATH, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("persisted");
//            return Unit.INSTANCE;
        }
    }

    public static final Wrapper wrapper = new Wrapper() {
        public <T> Object invoke(
                @NotNull Function1<? super Continuation<? super T>, ?> function1,
                @NotNull Continuation<? super T> continuation
        ) {
            PersistedContinuation<T> persistedContinuation = new PersistedContinuation<>(continuation);
            if (Files.exists(STATE_PATH)) {
                try (var input = new Input(Files.newInputStream(STATE_PATH))) {
                    var kryo = getKryo();
                    var references = kryo.getReferenceResolver();
                    references.setReadObject(references.nextReadId(null), persistedContinuation.getContext());
                    references.setReadObject(references.nextReadId(null), persistedContinuation);
                    var cancellableContinuationReference = new AtomicReference<CancellableContinuation<? super Unit>>();
                    Object coroutineSuspended = CancellableContinuationKt.suspendCancellableCoroutine(
                            cancellableContinuation -> {
                                cancellableContinuationReference.set(cancellableContinuation);
                                return Unit.INSTANCE;
                            }, (Continuation<Unit>) kryo.readClassAndObject(input)
                    );
                    // Hacky way to receive COROUTINE_SUSPENDED from suspendCancellableCoroutine
                    cancellableContinuationReference.get().resume(Unit.INSTANCE, null);
                    return coroutineSuspended;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return function1.invoke(persistedContinuation);
        }
    };

    @NotNull
    private static Kryo getKryo() {
        Kryo kryo = new Kryo();
        kryo.setAutoReset(false);
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy() {
            @Override
            public ObjectInstantiator<?> newInstantiatorOf(Class type) {
                Class<?> klass = type;
                var kotlinClass = JvmClassMappingKt.getKotlinClass(klass);
                try {
                    Object objectInstance = kotlinClass.getObjectInstance();
                    if (objectInstance != null) {
                        return () -> objectInstance;
                    }
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    Constructor<?> constructor = klass.getDeclaredConstructor(Continuation.class);
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
}
