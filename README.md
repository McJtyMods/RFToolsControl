# RFToolsControl
RFTools Control is an addon for RFTools adding a programmable logic block

## Building

This branch targets Minecraft 26.3 on Fabric and uses Java 25. Put this checkout beside `Multi_fabric` as `RFToolsControl_fabric`. From MultiWorkspace, run `./gradlew :RFToolsControl:build` or `./gradlew :RFToolsControl:runDatagen`. The workspace client and server load this checkout along with the included McJtyLib and RFToolsBase sources.

From the mod checkout, run `./gradlew build` for a standalone build with the shared script's published dependencies.
