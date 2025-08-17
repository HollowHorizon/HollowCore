package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.KoolContext;
import de.fabmax.kool.KoolSystem;
import de.fabmax.kool.util.RenderLoopCoroutineDispatcherKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.CancellableContinuation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.utils.CoroutineHelper;

@Mixin(value = RenderLoopCoroutineDispatcherKt.class, remap = false)
public class RenderLoopCoroutineDispatcherMixin {

    @Inject(method = "delayFrames", at = @At("HEAD"), cancellable = true)
    private static void onDelayFrames(int numFrames, @NotNull Continuation<? super Unit> continuation, CallbackInfoReturnable<Object> cir) {
        if (numFrames <= 0) {
            cir.setReturnValue(Unit.INSTANCE);
            return;
        }

        final Function1<KoolContext, Unit>[] callbackHolder = new Function1[1];

        callbackHolder[0] = new Function1<KoolContext, Unit>() {
            private int counter = numFrames;
            private boolean isDone = false;

            @Override
            public Unit invoke(KoolContext ctx) {
                if (!isDone && --counter <= 0) {
                    isDone = true;
                    ctx.getOnRender().stageRemove(callbackHolder[0]);

                    CoroutineHelper.resumeUnit(continuation);
                }
                return Unit.INSTANCE;
            }
        };

        KoolSystem.INSTANCE.requireContext().getOnRender().stageAdd(callbackHolder[0], -1);

        if (continuation instanceof CancellableContinuation) {
            ((CancellableContinuation<?>) continuation).invokeOnCancellation(throwable -> {
                KoolSystem.INSTANCE.requireContext().getOnRender().stageRemove(callbackHolder[0]);
                return Unit.INSTANCE;
            });
        }

        cir.setReturnValue(kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED());
    }
}