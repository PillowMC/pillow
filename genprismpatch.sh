#!/bin/sh
read -rp "Please input PrismLauncher path (default to ~/.local/share/PrismLauncher):" PRISM_PATH
if [ -z $PRISM_PATH ]; then
PRISM_PATH=~/.local/share/PrismLauncher
fi
if [ ! -d $PRISM_PATH/instances ]; then
echo $PRISM_PATH is not a vaild PrismLauncher path!.
exit 1
fi

read -rp "Please input classpath seprator (default to \`:\`):" SEPRATOR
if [ -z $SEPRATOR ]; then
SEPRATOR=:
fi 

MAVEN_LOCAL=~/.m2/repository
echo Building...
./gradlew build publishToMavenLocal
echo Build done.
cat > net.pillowmc.pillow.json << EOF
{
    "formatVersion": 1,
    "name": "Pillow",
    "uid": "net.pillowmc.pillow",
    "version": "0.2.3",
    "minecraftArguments": "--username \${auth_player_name} --version \${version_name} --gameDir \${game_directory} --assetsDir \${assets_root} --assetIndex \${assets_index_name} --uuid \${auth_uuid} --accessToken \${auth_access_token} --userType \${user_type} --versionType \${version_type} --fml.neoForgeVersion 20.4.196 --fml.fmlVersion 2.0.17 --fml.mcVersion 1.20.4 --fml.neoFormVersion 20231207.154220 --launchTarget pillowclient",
    "+jvmArgs": [
        "-Dfml.pluginLayerLibraries=",
        "-DlibraryDirectory=$PRISM_PATH/libraries/",
        "-DmergeModules=jna-5.10.0.jar,jna-platform-5.10.0.jar",
        "-DignoreList=securejarhandler-2.1.24.jar,asm-,bootstraplauncher-1.1.2.jar,JarJarFileSystems-0.4.0.jar,intermediary-,client-extra,neoforge-,1.20.4.jar,datafixerupper,minecraft-1.20.4-client.jar",
        "-p",
        "$PRISM_PATH/libraries/cpw/mods/bootstraplauncher/1.1.2/bootstraplauncher-1.1.2.jar$SEPRATOR$PRISM_PATH/libraries/cpw/mods/securejarhandler/2.1.24/securejarhandler-2.1.24.jar$SEPRATOR$PRISM_PATH/libraries/org/ow2/asm/asm-commons/9.6/asm-commons-9.6.jar$SEPRATOR$PRISM_PATH/libraries/org/ow2/asm/asm-util/9.6/asm-util-9.6.jar$SEPRATOR$PRISM_PATH/libraries/org/ow2/asm/asm-analysis/9.6/asm-analysis-9.6.jar$SEPRATOR$PRISM_PATH/libraries/org/ow2/asm/asm-tree/9.6/asm-tree-9.6.jar$SEPRATOR$PRISM_PATH/libraries/org/ow2/asm/asm/9.6/asm-9.6.jar",
        "--add-modules",
        "ALL-MODULE-PATH",
        "--add-opens","java.base/java.util.jar=cpw.mods.securejarhandler",
        "--add-opens","java.base/java.lang.invoke=cpw.mods.securejarhandler",
        "--add-exports","java.base/sun.security.util=cpw.mods.securejarhandler",
        "--add-exports","jdk.naming.dns/com.sun.jndi.dns=java.naming"
    ],
    "libraries": [
        {
            "name": "net.pillowmc:pillow:0.2.3",
            "url": "file://$MAVEN_LOCAL"
        },
        {
            "name": "net.pillowmc:pillow:0.2.3:langprovider",
            "url": "file://$MAVEN_LOCAL"
        },
        {
            "name": "net.pillowmc:intermediary2srg:1.20.4",
            "url": "file://$MAVEN_LOCAL"
        }
    ],
    "mainClass": "cpw.mods.bootstraplauncher.BootstrapLauncher"
}
EOF
