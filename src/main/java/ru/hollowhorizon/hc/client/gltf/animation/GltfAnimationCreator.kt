package ru.hollowhorizon.hc.client.gltf.animation

import de.javagl.jgltf.model.AccessorFloatData
import de.javagl.jgltf.model.AnimationModel
import ru.hollowhorizon.hc.HollowCore

object GltfAnimationCreator {

    @JvmStatic
    fun createGltfAnimation(animationModel: AnimationModel): List<InterpolatedChannel> {
        val channels = animationModel.channels
        val interpolatedChannels: MutableList<InterpolatedChannel> = ArrayList(channels.size)

        for (channel in channels) {
            val sampler = channel.sampler
            val input = sampler.input
            val inputData = input.accessorData
            if (inputData !is AccessorFloatData) {
                HollowCore.LOGGER.warn(
                    "Input data is not an AccessorFloatData, but "
                            + inputData.javaClass
                )
                continue
            }
            val inputFloatData = inputData
            val output = sampler.output
            val outputData = output.accessorData
            if (outputData !is AccessorFloatData) {
                HollowCore.LOGGER.warn(
                    "Output data is not an AccessorFloatData, but "
                            + outputData.javaClass
                )
                continue
            }
            val outputFloatData = outputData
            val numKeyElements = inputFloatData.numElements
            val keys = FloatArray(numKeyElements)
            for (e in 0 until numKeyElements) {
                keys[e] = inputFloatData[e]
            }
            var globalIndex = 0
            var numComponentsPerElement: Int
            var values: Array<FloatArray>
            var valuesCubic: Array<Array<FloatArray>>
            val nodeModel = channel.nodeModel

            HollowCore.LOGGER.info("node model object: {}", nodeModel)

            val path = channel.path
            var interpolation: AnimationModel.Interpolation
            when (path) {
                "translation" -> {
                    numComponentsPerElement = outputData.getNumComponentsPerElement()
                    interpolation = sampler.interpolation
                    HollowCore.LOGGER.info("translation: {}", nodeModel.translation)
                    when (interpolation) {
                        AnimationModel.Interpolation.STEP -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : StepInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var translation = nodeModel.translation
                                    if (translation == null) {
                                        translation = FloatArray(numComponentsPerElement)
                                        nodeModel.translation = translation
                                    }
                                    return translation
                                }
                            })
                        }

                        AnimationModel.Interpolation.LINEAR -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }

                            interpolatedChannels.add(object : LinearInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var translation = nodeModel.translation
                                    if (translation == null) {
                                        translation = FloatArray(numComponentsPerElement)
                                        nodeModel.translation = translation
                                    }
                                    return translation
                                }
                            })
                        }

                        AnimationModel.Interpolation.CUBICSPLINE -> {
                            valuesCubic = Array(numKeyElements) { Array(3) { FloatArray(numComponentsPerElement) } }
                            var e = 0
                            while (e < numKeyElements) {
                                val elements = valuesCubic[e]
                                var i = 0
                                while (i < 3) {
                                    val components = elements[i]
                                    var c = 0
                                    while (c < numComponentsPerElement) {
                                        components[c] = outputFloatData[globalIndex++]
                                        c++
                                    }
                                    i++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : CubicSplineInterpolatedChannel(keys, valuesCubic) {
                                override fun getListener(): FloatArray {
                                    var translation = nodeModel.translation
                                    if (translation == null) {
                                        translation = FloatArray(numComponentsPerElement)
                                        nodeModel.translation = translation
                                    }
                                    return translation
                                }
                            })
                        }

                        else -> HollowCore.LOGGER.warn("Interpolation type not supported: $interpolation")
                    }
                }

                "rotation" -> {
                    numComponentsPerElement = outputData.getNumComponentsPerElement()
                    interpolation = sampler.interpolation
                    HollowCore.LOGGER.info("rotation: {}", nodeModel.rotation)
                    when (interpolation) {
                        AnimationModel.Interpolation.STEP -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : StepInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var rotation = nodeModel.rotation
                                    if (rotation == null) {
                                        rotation = FloatArray(numComponentsPerElement)
                                        nodeModel.rotation = rotation
                                    }
                                    return rotation
                                }
                            })
                        }

                        AnimationModel.Interpolation.LINEAR -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : SphericalLinearInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var rotation = nodeModel.rotation
                                    if (rotation == null) {
                                        rotation = FloatArray(numComponentsPerElement)
                                        nodeModel.rotation = rotation
                                    }
                                    return rotation
                                }
                            })
                        }

                        AnimationModel.Interpolation.CUBICSPLINE -> {
                            valuesCubic = Array(numKeyElements) { Array(3) { FloatArray(numComponentsPerElement) } }
                            var e = 0
                            while (e < numKeyElements) {
                                val elements = valuesCubic[e]
                                var i = 0
                                while (i < 3) {
                                    val components = elements[i]
                                    var c = 0
                                    while (c < numComponentsPerElement) {
                                        components[c] = outputFloatData[globalIndex++]
                                        c++
                                    }
                                    i++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : CubicSplineInterpolatedChannel(keys, valuesCubic) {
                                override fun getListener(): FloatArray {
                                    var rotation = nodeModel.rotation
                                    if (rotation == null) {
                                        rotation = FloatArray(numComponentsPerElement)
                                        nodeModel.rotation = rotation
                                    }
                                    return rotation
                                }
                            })
                        }

                        else -> HollowCore.LOGGER.warn("Interpolation type not supported: $interpolation")
                    }
                }

                "scale" -> {
                    numComponentsPerElement = outputData.getNumComponentsPerElement()
                    interpolation = sampler.interpolation
                    HollowCore.LOGGER.info("scale: {}", nodeModel.scale)
                    when (interpolation) {
                        AnimationModel.Interpolation.STEP -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : StepInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var scale = nodeModel.scale
                                    if (scale == null) {
                                        scale = FloatArray(numComponentsPerElement)
                                        nodeModel.scale = scale
                                    }
                                    return scale
                                }
                            })
                        }

                        AnimationModel.Interpolation.LINEAR -> {
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : LinearInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var scale = nodeModel.scale
                                    if (scale == null) {
                                        scale = FloatArray(numComponentsPerElement)
                                        nodeModel.scale = scale
                                    }
                                    return scale
                                }
                            })
                        }

                        AnimationModel.Interpolation.CUBICSPLINE -> {
                            valuesCubic = Array(numKeyElements) { Array(3) { FloatArray(numComponentsPerElement) } }
                            var e = 0
                            while (e < numKeyElements) {
                                val elements = valuesCubic[e]
                                var i = 0
                                while (i < 3) {
                                    val components = elements[i]
                                    var c = 0
                                    while (c < numComponentsPerElement) {
                                        components[c] = outputFloatData[globalIndex++]
                                        c++
                                    }
                                    i++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : CubicSplineInterpolatedChannel(keys, valuesCubic) {
                                override fun getListener(): FloatArray {
                                    var scale = nodeModel.scale
                                    if (scale == null) {
                                        scale = FloatArray(numComponentsPerElement)
                                        nodeModel.scale = scale
                                    }
                                    return scale
                                }
                            })
                        }

                        else -> HollowCore.LOGGER.warn("Interpolation type not supported: $interpolation")
                    }
                }

                "weights" -> {
                    interpolation = sampler.interpolation
                    HollowCore.LOGGER.info("weights: {}", nodeModel.weights)
                    when (interpolation) {
                        AnimationModel.Interpolation.STEP -> {
                            numComponentsPerElement = outputData.getTotalNumComponents() / numKeyElements
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : StepInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var weights = nodeModel.weights
                                    if (weights == null) {
                                        weights = FloatArray(numComponentsPerElement)
                                        nodeModel.weights = weights
                                    }
                                    return weights
                                }
                            })
                        }

                        AnimationModel.Interpolation.LINEAR -> {
                            numComponentsPerElement = outputData.getTotalNumComponents() / numKeyElements
                            values = Array(numKeyElements) { FloatArray(numComponentsPerElement) }
                            var e = 0
                            while (e < numKeyElements) {
                                val components = values[e]
                                var c = 0
                                while (c < numComponentsPerElement) {
                                    components[c] = outputFloatData[globalIndex++]
                                    c++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : LinearInterpolatedChannel(keys, values) {
                                override fun getListener(): FloatArray {
                                    var weights = nodeModel.weights
                                    if (weights == null) {
                                        weights = FloatArray(numComponentsPerElement)
                                        nodeModel.weights = weights
                                    }
                                    return weights
                                }
                            })
                        }

                        AnimationModel.Interpolation.CUBICSPLINE -> {
                            numComponentsPerElement = outputData.getTotalNumComponents() / numKeyElements / 3
                            valuesCubic = Array(numKeyElements) { Array(3) { FloatArray(numComponentsPerElement) } }
                            var e = 0
                            while (e < numKeyElements) {
                                val elements = valuesCubic[e]
                                var i = 0
                                while (i < 3) {
                                    val components = elements[i]
                                    var c = 0
                                    while (c < numComponentsPerElement) {
                                        components[c] = outputFloatData[globalIndex++]
                                        c++
                                    }
                                    i++
                                }
                                e++
                            }
                            interpolatedChannels.add(object : CubicSplineInterpolatedChannel(keys, valuesCubic) {
                                override fun getListener(): FloatArray {
                                    var weights = nodeModel.weights
                                    if (weights == null) {
                                        weights = FloatArray(numComponentsPerElement)
                                        nodeModel.weights = weights
                                    }
                                    return weights
                                }
                            })
                        }

                        else -> HollowCore.LOGGER.warn("Interpolation type not supported: $interpolation")
                    }
                }

                else -> HollowCore.LOGGER.warn(
                    "Animation channel target path must be "
                            + "\"translation\", \"rotation\", \"scale\" or  \"weights\", "
                            + "but is " + path
                )
            }
        }
        return interpolatedChannels
    }
}
