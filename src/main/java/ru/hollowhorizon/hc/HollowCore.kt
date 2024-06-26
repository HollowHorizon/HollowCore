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
package ru.hollowhorizon.hc

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.hollowhorizon.hc.client.HollowCoreClient
import ru.hollowhorizon.hc.common.HollowCoreCommon
import ru.hollowhorizon.hc.common.registry.HollowModProcessor.initMod
import ru.hollowhorizon.hc.common.registry.RegistryLoader.registerAll

@Mod(HollowCore.MODID)
object HollowCore {
    const val MODID: String = "hc"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger()
    const val DEBUG_MODE: Boolean = false


    init {
        LOGGER.info("Starting HollowCore...")
        if (FMLEnvironment.dist.isClient) HollowCoreClient
        HollowCoreCommon

        initMod()
        registerAll()
    }
}
