<p align="center">
  <img src="https://github.com/HollowHorizon/HollowCore/blob/1.19.2/src/main/resources/hollow_core_logo.png">
</p>
<p align="center">
  <a href="https://github.com/HollowHorizon/HollowCore/commits/"><img src="https://img.shields.io/github/commit-activity/t/HollowHorizon/HollowCore?style=for-the-badge&labelColor=7e91a6&color=80bcff" alt="Stars"></a>
  <a href="https://github.com/HollowHorizon/HollowCore/stargazers"><img src="https://img.shields.io/github/stars/HollowHorizon/HollowCore.svg?style=for-the-badge&labelColor=7e91a6&color=80bcff" alt="Stars"></a>
  <a href="https://github.com/HollowHorizon/HollowCore/graphs/contributors"><img src="https://img.shields.io/github/contributors/HollowHorizon/HollowCore.svg?style=for-the-badge&labelColor=7e91a6&color=80bcff" alt="Contributors"></a>
  <a href="https://github.com/HollowHorizon/HollowCore/releases"><img src="https://img.shields.io/github/downloads/HollowHorizon/HollowCore/total?style=for-the-badge&labelColor=7e91a6&color=80bcff" alt="Downloads"></a>
  <a href="https://discord.gg/qKpPhkwGCY"><img src="https://img.shields.io/discord/1081609215484887051?style=for-the-badge&label=Discord&logo=discord&logoColor=d9e0ee&labelColor=7e91a6&color=80bcff" alt="Discord"></a>
  <a href="https://0mods.team/docs/hollowcore/hollowcore"><img src="https://img.shields.io/badge/Docs-📖-blue?style=for-the-badge&labelColor=7e91a6&color=80bcff" alt="Documentation"></a>
</p>
HollowCore is a minecraft modding library by <a href="https://github.com/HollowHorizon" style="color: black; text-decoration: underline;text-decoration-style: dotted;">HollowHorizon</a>. It is an easy to use and powerful library for easier creation of minecraft mods for Fabric, Forge and NeoForge.

## Features
### Multi versional
HollowCore is now running on 1.21, 1.20.1 and 1.19.2 (WIP) by the Forge, Fabric and NeoForge loaders.

### Integration with ModLoaders
- [x] Universal Registration system to register blocks, items, block entities, etc. using kotlin delegates.
- [x] Universal Packet system with automatic serialization and registration using annotations.
- [x] Universal Capabilities system to storing nbt in entities, block entities and worlds. 
- [x] Universal EventBus system for all modloaders and basic events.

### Easier development
- [x] Automatic models generation for blocks and items using embed resourcepack.
- [x] NBT serialization and deserialization of any objects using Kotlinx.Serialization.
- [x] Toml Config system based on KotlinX Serialization.
- [x] Automatic sounds.json generation.

### Graphics
- [x] ImGui based framework: basic elements, items, entities, containers, slots, etc. 
- [x] Async GLTF model loader with skeletal animations, skinning, morph targets, PBR materials with Iris/Oculus support.
- [x] Effekseer particles support.
- [x] Gif textures.
- [x] MP3, OGG, WAV Sound formats support.