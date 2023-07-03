import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos

int a = 1
int b = 5
def data = new BlockPos(a, 1, b)

static void info(Object data) {
    System.out.println(data)
}

data.rotate(Rotation.CLOCKWISE_90)

info(data)