package ru.hollowhorizon.hc.common.registry;

import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import ru.hollowhorizon.hc.common.world.structures.objects.HollowStructurePieces;

public class ModStructurePieces {
    public static IStructurePieceType HollowStructurePiece;

    public static void registerPieces() {
        HollowStructurePiece = IStructurePieceType.setPieceId(HollowStructurePieces.HollowPieces::new, "hollow_structures");
    }
}
